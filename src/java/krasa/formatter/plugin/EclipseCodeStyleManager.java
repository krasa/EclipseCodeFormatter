package krasa.formatter.plugin;

import java.util.*;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiFile;
import com.intellij.psi.codeStyle.CodeStyleManager;
import com.intellij.psi.impl.CheckUtil;
import com.intellij.psi.util.PsiUtilBase;
import com.intellij.util.IncorrectOperationException;

import krasa.formatter.eclipse.Classloaders;
import krasa.formatter.eclipse.CodeFormatterFacade;
import krasa.formatter.eclipse.JavaCodeFormatterFacade;
import krasa.formatter.exception.FileDoesNotExistsException;
import krasa.formatter.exception.FormattingFailedException;
import krasa.formatter.settings.DisabledFileTypeSettings;
import krasa.formatter.settings.ProjectComponent;
import krasa.formatter.settings.Settings;
import krasa.formatter.settings.provider.CppPropertiesProvider;
import krasa.formatter.settings.provider.JSPropertiesProvider;
import krasa.formatter.utils.FileUtils;

public class EclipseCodeStyleManager {

	private final Logger LOG = Logger.getInstance(this.getClass().getName());

	@NotNull
	protected final CodeStyleManager original;
	@NotNull
	protected volatile Settings settings;
	@NotNull
	private Notifier notifier;
	@Nullable
	private volatile EclipseCodeFormatter eclipseCodeFormatterJava;
	@Nullable
	private volatile EclipseCodeFormatter eclipseCodeFormatterJs;
	@Nullable
	private volatile EclipseCodeFormatter eclipseCodeFormatterCpp;

	public EclipseCodeStyleManager(@NotNull CodeStyleManager original, @NotNull Settings settings) {
		this.original = original;
		this.settings = settings;
		notifier = new Notifier();
	}
	       
	public void updateSettings(@NotNull Settings settings) {
		this.settings = settings;
		eclipseCodeFormatterJava = null;
		eclipseCodeFormatterJs = null;
		eclipseCodeFormatterCpp = null;
	}
	
	private final static Comparator<TextRange> RANGE_COMPARATOR = new Comparator<TextRange>() {
		@Override
		public int compare(TextRange range1, TextRange range2) {
			int startOffsetDiff = range1.getStartOffset() - range2.getStartOffset();
			return startOffsetDiff != 0 ? startOffsetDiff : range1.getEndOffset() - range2.getEndOffset();
		}
	};

	// 15
	// @Override
	public void reformatTextWithContext(@NotNull PsiFile psiFile, @NotNull Collection<TextRange> collection)
			throws IncorrectOperationException {
		if (!shouldReformatByEclipse(psiFile)) {
			original.reformatTextWithContext(psiFile, collection);
		} else {
			reformatText(psiFile, collection);
		}
	}

	// @Override
	public void reformatText(@NotNull PsiFile psiFile, @NotNull Collection<TextRange> textRanges)
			throws IncorrectOperationException {
		if (!shouldReformatByEclipse(psiFile)) {
			original.reformatText(psiFile, textRanges);
		} else {
			List<TextRange> list = new ArrayList<TextRange>(textRanges);
			Collections.sort(list, Collections.reverseOrder(RANGE_COMPARATOR));
			formatInternal(psiFile, list, Mode.ALWAYS_FORMAT);
		}
	}

	// @Override
	// todo should I even override this method?
	public void reformatText(@NotNull final PsiFile psiFile, final int startOffset, final int endOffset)
			throws IncorrectOperationException {
		if (!shouldReformatByEclipse(psiFile)) {
			original.reformatText(psiFile, startOffset, endOffset);
		} else {
			formatInternal(psiFile, Arrays.asList(new TextRange(startOffset, endOffset)), Mode.WITH_CTRL_SHIFT_ENTER_CHECK);
		}
	}

	private void formatInternal(PsiFile psiFile, List<TextRange> list, Mode mode) {
		ApplicationManager.getApplication().assertWriteAccessAllowed();
		PsiDocumentManager.getInstance(original.getProject()).commitAllDocuments();
		CheckUtil.checkWritable(psiFile);
		
		if (psiFile.getVirtualFile() == null) {
			LOG.debug("virtual file is null");
			Notification notification = ProjectComponent.GROUP_DISPLAY_ID_ERROR.createNotification(
					Notifier.NO_FILE_TO_FORMAT, NotificationType.ERROR);
			notifier.showNotification(notification, psiFile.getProject());
			return;
		}
		
		int startOffset = -1;
		int endOffset = -1;
		boolean formattedByIntelliJ = false;
		try {

			boolean wholeFileOrSelectedText = wholeFileOrSelectedText(psiFile, list);
			boolean notify = false;

			for (TextRange textRange : list) {
				startOffset = textRange.getStartOffset();
				endOffset = textRange.getEndOffset();
				LOG.debug("format " + psiFile.getName() + " " + startOffset + " " + endOffset);

				if (mode.shouldReformat(wholeFileOrSelectedText)) {
					try {
						formatWithEclipse(psiFile, startOffset, endOffset);
						notify = notify || shouldNotify(psiFile, startOffset, endOffset);
					} catch (ReformatItInIntelliJ e) {
						formattedByIntelliJ = true;
						formatWithIntelliJ(psiFile, startOffset, endOffset);
					}
				} else {
					formattedByIntelliJ = true;
					if (shouldSkipFormatting(psiFile, startOffset, endOffset)) {
						notifier.notifyFormattingWasDisabled(psiFile);
					} else {
						formatWithIntelliJ(psiFile, startOffset, endOffset);
						notify = notify || wholeFileOrSelectedText;
					}
				}
			}
			if (notify) {
				notifier.notifySuccessFormatting(psiFile, formattedByIntelliJ);
			}

		} catch (final FileDoesNotExistsException e) {
			LOG.debug(e);
			notifier.notifyFailedFormatting(psiFile, formattedByIntelliJ, e);
		} catch (final InvalidPropertyFile e) {
			LOG.debug(e);
			notifier.notifyFailedFormatting(psiFile, formattedByIntelliJ, e);
		} catch (final ImportSorterException e) {
			LOG.error(e);
			notifier.notifyBrokenImportSorter(psiFile.getProject());
		} catch (final FormattingFailedException e) {
			LOG.debug("startOffset" + startOffset + ", endOffset:" + endOffset + ", length of file "
					+ psiFile.getText().length(), e);
			notifier.notifyFailedFormatting(psiFile, formattedByIntelliJ, getReason(e));
		} catch (final Throwable e) {
			LOG.error(e);
		}
	}

	private boolean shouldNotify(PsiFile psiFile, int startOffset, int endOffset) {
		boolean isShort = endOffset - startOffset < settings.getNotifyFromTextLenght();
		boolean skipSuccessFormattingNotification = isShort
				&& !FileUtils.isWholeFile(startOffset, endOffset, psiFile.getText());
		return !skipSuccessFormattingNotification;
	}

	private boolean wholeFileOrSelectedText(PsiFile psiFile, List<TextRange> list) {
		boolean wholeFileOrSelectedText = false;
		final Editor editor = PsiUtilBase.findEditor(psiFile);
		boolean result;
		if (editor == null) {
			wholeFileOrSelectedText = true;
		} else {
			Document document = editor.getDocument();
			String text = document.getText();
			boolean hasSelection = editor.getSelectionModel().hasSelection();
			for (TextRange textRange : list) {
				boolean wholeFile = FileUtils.isWholeFile(textRange.getStartOffset(), textRange.getEndOffset(), text);
				result = hasSelection || wholeFile;
				wholeFileOrSelectedText = wholeFileOrSelectedText || result;
			}
		}
		return wholeFileOrSelectedText;
	}

	private String getReason(FormattingFailedException e) {
		if (e.isUserError() && e.getMessage() != null) {
			return "<br>" + e.getMessage();
		}
		String result = "Probably due to syntax error or wrong configuration file.";
		String message = e.getMessage();
		if (message != null) {
			result = result + "<br>" + message;
		}
		return result;
	}

	private void formatWithEclipse(PsiFile psiFile, int startOffset, int endOffset) throws FileDoesNotExistsException {
		if (FileUtils.isJavaScript(psiFile)) {
			if (eclipseCodeFormatterJs == null) {
				JSPropertiesProvider jsProperties = settings.getJSProperties();
				CodeFormatterFacade jsFormatter = Classloaders.getJsFormatter(jsProperties);
				eclipseCodeFormatterJs = new EclipseCodeFormatter(settings, jsFormatter);
			}
			eclipseCodeFormatterJs.format(psiFile, startOffset, endOffset);
		} else if (FileUtils.isCpp(psiFile)) {
			if (eclipseCodeFormatterCpp == null) {
				CppPropertiesProvider cppProperties = settings.getCppProperties();
				CodeFormatterFacade cpp = Classloaders.getCppFormatter(cppProperties);
				eclipseCodeFormatterCpp = new EclipseCodeFormatter(settings, cpp);
			}
			eclipseCodeFormatterCpp.format(psiFile, startOffset, endOffset);
		} else {
			if (eclipseCodeFormatterJava == null) {
				JavaCodeFormatterFacade facade = new JavaCodeFormatterFacade(settings.getJavaProperties(),
						settings.getEclipseVersion(), original.getProject(), settings.getPathToEclipse());
				eclipseCodeFormatterJava = new EclipseCodeFormatter(settings, facade);
			}
			eclipseCodeFormatterJava.format(psiFile, startOffset, endOffset);
		}
	}

	private boolean shouldSkipFormatting(PsiFile psiFile, int startOffset, int endOffset) {
		VirtualFile virtualFile = psiFile.getVirtualFile();
		if (settings.isFormatSeletedTextInAllFileTypes()) {
			// when file is being edited, it is important to load text from editor, i think
			final Editor editor = PsiUtilBase.findEditor(psiFile);
			if (editor != null) {
				Document document = editor.getDocument();
				String text = document.getText();
				if (!FileUtils.isWholeFile(startOffset, endOffset, text) ) {
					return false;
				}
			}
		}
		if (settings.isFormatOtherFileTypesWithIntelliJ()) {
			return isDisabledFileType(virtualFile);
		}
		return true;
	}


	public boolean shouldReformatByEclipse(PsiFile psiFile) {
		return settings.isEnabled() && fileTypeIsEnabled(psiFile) && psiFile.getVirtualFile().isInLocalFileSystem()
				&& FileUtils.isWritable(psiFile);
	}

	private void formatWithIntelliJ(PsiFile psiFile, int startOffset, int endOffset) {
		LOG.debug("formatting with IntelliJ formatter");
		original.reformatText(psiFile, startOffset, endOffset);
	}

	private boolean isDisabledFileType(VirtualFile virtualFile) {
		String path = virtualFile.getPath();
		DisabledFileTypeSettings disabledFileTypeSettings = settings.geDisabledFileTypeSettings();
		return disabledFileTypeSettings.isDisabled(path);
	}

	private boolean fileTypeIsEnabled(@NotNull PsiFile psiFile) {
		return (FileUtils.isJava(psiFile) && settings.isEnableJavaFormatting())
				|| (FileUtils.isJavaScript(psiFile) && settings.isEnableJSFormatting())
				|| (FileUtils.isCpp(psiFile) && settings.isEnableCppFormatting());
	}

	public boolean isEnabled() {
		return settings.isEnabled();
	}
}
