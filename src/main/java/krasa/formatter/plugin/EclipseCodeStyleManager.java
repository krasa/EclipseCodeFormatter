package krasa.formatter.plugin;

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
import krasa.formatter.eclipse.JavaCodeFormatterFacade;
import krasa.formatter.exception.FileDoesNotExistsException;
import krasa.formatter.exception.FormattingFailedException;
import krasa.formatter.settings.DisabledFileTypeSettings;
import krasa.formatter.settings.ProjectComponent;
import krasa.formatter.settings.Settings;
import krasa.formatter.utils.FileUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class EclipseCodeStyleManager {

	private final Logger LOG = Logger.getInstance(this.getClass().getName());

	@NotNull
	protected final CodeStyleManager original;
	@NotNull
	protected volatile Settings settings;
	@NotNull
	protected Notifier notifier;
	@Nullable
	private volatile EclipseCodeFormatter eclipseCodeFormatterJava;

	public EclipseCodeStyleManager(@NotNull CodeStyleManager original, @NotNull Settings settings) {
		this.original = original;
		this.settings = settings;
		notifier = new Notifier();
	}

	public void updateSettings(@NotNull Settings settings) {
		this.settings = settings;
		eclipseCodeFormatterJava = null;
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
	public void reformatTextWithContext(@NotNull PsiFile psiFile, @NotNull Collection<? extends TextRange> textRanges) throws IncorrectOperationException {
		if (shouldReformatByEclipse(psiFile)) {
			reformatText(psiFile, textRanges);
		} else if (shouldSkipFormatting(psiFile, textRanges)) {
			notifier.notifyFormattingWasDisabled(psiFile);
		} else {
			original.reformatTextWithContext(psiFile, textRanges);
		}
	}

	// @Override
	public void reformatText(@NotNull PsiFile psiFile, @NotNull Collection<? extends TextRange> textRanges) throws IncorrectOperationException {
		if (shouldReformatByEclipse(psiFile)) {
			List<TextRange> list = new ArrayList<TextRange>(textRanges);
			Collections.sort(list, Collections.reverseOrder(RANGE_COMPARATOR));
			formatInternal(psiFile, list, Mode.ALWAYS_FORMAT);
		} else if (shouldSkipFormatting(psiFile, textRanges)) {
			notifier.notifyFormattingWasDisabled(psiFile);
		} else {
			original.reformatText(psiFile, textRanges);
		}
	}

	// @Override
	public void reformatText(@NotNull PsiFile psiFile, @NotNull Collection<? extends TextRange> textRanges, boolean processChangedTextOnly) throws IncorrectOperationException {
		if (shouldReformatByEclipse(psiFile)) {
			List<TextRange> list = new ArrayList<TextRange>(textRanges);
			Collections.sort(list, Collections.reverseOrder(RANGE_COMPARATOR));
			formatInternal(psiFile, list, Mode.ALWAYS_FORMAT);
		} else if (shouldSkipFormatting(psiFile, textRanges)) {
			notifier.notifyFormattingWasDisabled(psiFile);
		} else {
			//since IJ 241
			original.reformatText(psiFile, textRanges, processChangedTextOnly);
		}
	}

	// @Override
	// todo should I even override this method?
	public void reformatText(@NotNull final PsiFile psiFile, final int startOffset, final int endOffset) throws IncorrectOperationException {
		List<TextRange> textRanges = Collections.singletonList(new TextRange(startOffset, endOffset));

		if (shouldReformatByEclipse(psiFile)) {
			formatInternal(psiFile, textRanges, Mode.WITH_CTRL_SHIFT_ENTER_CHECK);
		} else if (shouldSkipFormatting(psiFile, textRanges)) {
			notifier.notifyFormattingWasDisabled(psiFile);
		} else {
			original.reformatText(psiFile, startOffset, endOffset);
		}
	}

	private void formatInternal(PsiFile psiFile, List<? extends TextRange> list, Mode mode) {
		ApplicationManager.getApplication().assertWriteAccessAllowed();
		PsiDocumentManager.getInstance(original.getProject()).commitAllDocuments();
		CheckUtil.checkWritable(psiFile);

		if (psiFile.getVirtualFile() == null) {
			LOG.debug("virtual file is null");
			Notification notification = ProjectComponent.getNotificationGroupError().createNotification(
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
						//for live templates -> selection gets messed up with incompatible code style between eclipse and intellij
						formattedByIntelliJ = formatWithIntelliJ(psiFile, startOffset, endOffset);
					}
				} else {
					formattedByIntelliJ = formatWithIntelliJ(psiFile, startOffset, endOffset);
					notify = notify || wholeFileOrSelectedText;
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
			notifier.notifyFailedFormatting(psiFile, formattedByIntelliJ, e, getReason(e));
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

	private boolean wholeFileOrSelectedText(PsiFile psiFile, List<? extends TextRange> list) {
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
		String result = "Probably due to a syntax error or a wrong configuration file.";
		String message = e.getMessage();
		if (message != null) {
			result = result + "<br>" + message;
		}
		return result;
	}

	private void formatWithEclipse(PsiFile psiFile, int startOffset, int endOffset) throws FileDoesNotExistsException {
			if (eclipseCodeFormatterJava == null) {
				JavaCodeFormatterFacade facade = new JavaCodeFormatterFacade(settings, original.getProject());
				eclipseCodeFormatterJava = new EclipseCodeFormatter(settings, facade);
			}
			eclipseCodeFormatterJava.format(psiFile, startOffset, endOffset);
	}


	protected boolean shouldSkipFormatting(PsiFile psiFile, Collection<? extends TextRange> textRanges) {

		if (settings.isFormatSeletedTextInAllFileTypes()) {             
			// when file is being edited, it is important to load text from editor, i think
			final Editor editor = PsiUtilBase.findEditor(psiFile);
			if (editor != null) {
				Document document = editor.getDocument();
				String text = document.getText();
				if (!FileUtils.isWholeFile(textRanges, text)) {
					return false;
				}
			}
		}
		//not else
		if (settings.isFormatOtherFileTypesWithIntelliJ()) {
			return isDisabledFileType(psiFile.getVirtualFile());
		}
		return true;
	}


	public boolean shouldReformatByEclipse(PsiFile psiFile) {
		return settings.isEnabled() && fileTypeIsEnabled(psiFile)
				&& psiFile.getVirtualFile() != null
				&& psiFile.getVirtualFile().isInLocalFileSystem()     // not sure why that is here
				&& FileUtils.isWritable(psiFile);
	}

	private boolean formatWithIntelliJ(PsiFile psiFile, int startOffset, int endOffset) {
		LOG.debug("formatting with IntelliJ formatter");
		original.reformatText(psiFile, startOffset, endOffset);
		return true;
	}

	private boolean isDisabledFileType(VirtualFile virtualFile) {
		//https://github.com/krasa/EclipseCodeFormatter/issues/227
		if (virtualFile == null) {
			return false;
		}
		String path = virtualFile.getPath();
		DisabledFileTypeSettings disabledFileTypeSettings = settings.getDisabledFileTypeSettings();
		return disabledFileTypeSettings.isDisabled(path);
	}

	private boolean fileTypeIsEnabled(@NotNull PsiFile psiFile) {
		return (FileUtils.isJava(psiFile) && settings.isEnableJavaFormatting());
	}

	public boolean isEnabled() {
		return settings.isEnabled();
	}
}
