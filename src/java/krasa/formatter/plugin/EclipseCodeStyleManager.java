package krasa.formatter.plugin;

import krasa.formatter.eclipse.FileDoesNotExistsException;
import krasa.formatter.eclipse.JSCodeFormatterFacade;
import krasa.formatter.eclipse.JavaCodeFormatterFacade;
import krasa.formatter.exception.FormattingFailedException;
import krasa.formatter.settings.DisabledFileTypeSettings;
import krasa.formatter.settings.ProjectSettingsComponent;
import krasa.formatter.settings.Settings;
import krasa.formatter.utils.FileUtils;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiFile;
import com.intellij.psi.codeStyle.CodeStyleManager;
import com.intellij.psi.impl.CheckUtil;
import com.intellij.psi.util.PsiUtilBase;
import com.intellij.util.IncorrectOperationException;

/**
 * Supported operations are handled by Eclipse formatter, other by IntelliJ formatter.
 * <p/>
 * TODO proper write action thread handling
 * 
 * @author Vojtech Krasa
 * @since 30.10.20011
 */
public class EclipseCodeStyleManager extends DelegatingCodeStyleManager {

	private static final Logger LOG = Logger.getInstance(EclipseCodeStyleManager.class.getName());

	@NotNull
	private Settings settings;
	@NotNull
	private Notifier notifier;
	@NotNull
	private EclipseCodeFormatter eclipseCodeFormatterJava;
	@Nullable
	private EclipseCodeFormatter eclipseCodeFormatterJs;

	public EclipseCodeStyleManager(@NotNull CodeStyleManager original, @NotNull Settings settings,
			@NotNull Project project) {
		super(original);
		this.settings = settings;
		notifier = new Notifier(project);
		eclipseCodeFormatterJava = new EclipseCodeFormatter(settings, new JavaCodeFormatterFacade(
				settings.getJavaProperties()));
	}

	public void reformatText(@NotNull final PsiFile psiFile, final int startOffset, final int endOffset)
			throws IncorrectOperationException {
		LOG.debug("reformatText " + psiFile.getName() + " " + startOffset + " " + endOffset);
		ApplicationManager.getApplication().assertWriteAccessAllowed();
		PsiDocumentManager.getInstance(getProject()).commitAllDocuments();
		boolean formattedByIntelliJ = false;
		try {

			CheckUtil.checkWritable(psiFile);

			if (psiFile.getVirtualFile() == null) {
				LOG.debug("virtual file is null");
				Notification notification = new Notification(ProjectSettingsComponent.GROUP_DISPLAY_ID_INFO, "",
						Notifier.NO_FILE_TO_FORMAT, NotificationType.ERROR);
				notifier.showNotification(notification);
				return;
			}
			// ctrl shift enter fix
			boolean wholeFileOrSelectedText = isWholeFileOrSelectedText(psiFile, startOffset, endOffset);
			if (canReformatWithEclipse(psiFile) && wholeFileOrSelectedText) {
				formatWithEclipse(psiFile, startOffset, endOffset);
				boolean skipSuccessFormattingNotification = shouldSkipNotification(startOffset, endOffset,
						psiFile.getText());
				if (!skipSuccessFormattingNotification) {
					notifier.notifySuccessFormatting(psiFile, false);
				}
			} else {
				if (shouldSkipFormatting(psiFile, startOffset, endOffset)) {
					notifier.notifyFormattingWasDisabled(psiFile);
				} else {
					formatWithIntelliJ(psiFile, startOffset, endOffset);
					if (wholeFileOrSelectedText) {
						notifier.notifySuccessFormatting(psiFile, true);
					}
				}
			}

		} catch (final FileDoesNotExistsException e) {
			e.printStackTrace();
			LOG.debug(e);
			notifier.notifyFailedFormatting(psiFile, formattedByIntelliJ, e);
		} catch (final InvalidPropertyFile e) {
			e.printStackTrace();
			LOG.debug(e);
			notifier.notifyFailedFormatting(psiFile, formattedByIntelliJ, e);
		} catch (final ImportSorterException e) {
			LOG.error(e);
			notifier.notifyBrokenImportSorter();
		} catch (final FormattingFailedException e) {
			e.printStackTrace();
			LOG.debug("startOffset" + startOffset + ", endOffset:" + endOffset + ", length of file "
					+ psiFile.getText().length(), e);
			notifier.notifyFailedFormatting(psiFile, formattedByIntelliJ,
					"Probably due to syntax error or wrong configuration file.");
		} catch (final Exception e) {
			e.printStackTrace();
			LOG.error("startOffset" + startOffset + ", endOffset:" + endOffset + ", length of file "
					+ psiFile.getText().length(), e);
		}
	}

	private void formatWithEclipse(PsiFile psiFile, int startOffset, int endOffset) throws FileDoesNotExistsException {
		if (FileUtils.isJavaScript(psiFile)) {
			if (eclipseCodeFormatterJs == null) {
				eclipseCodeFormatterJs = new EclipseCodeFormatter(settings, new JSCodeFormatterFacade(
						settings.getJSProperties()));
			}
			eclipseCodeFormatterJs.format(psiFile, startOffset, endOffset);
		} else {
			eclipseCodeFormatterJava.format(psiFile, startOffset, endOffset);
		}
	}

	private boolean shouldSkipNotification(int startOffset, int endOffset, String text) {
		boolean isShort = endOffset - startOffset < settings.getNotifyFromTextLenght();
		return isShort && !FileUtils.isWholeFile(startOffset, endOffset, text);
	}

	private boolean shouldSkipFormatting(PsiFile psiFile, int startOffset, int endOffset) {
		VirtualFile virtualFile = psiFile.getVirtualFile();
		if (settings.isFormatSeletedTextInAllFileTypes()) {
			// when file is being edited, it is important to load text from editor, i think
			final Editor editor = PsiUtilBase.findEditor(psiFile);
			if (editor != null) {
				Document document = editor.getDocument();
				String text = document.getText();
				if (!FileUtils.isWholeFile(startOffset, endOffset, text) || isFocusInEditorAndSelectedText()) {
					return false;
				}
			}
		}
		if (settings.isFormatOtherFileTypesWithIntelliJ()) {
			return isDisabledFileType(virtualFile);
		}
		return true;
	}

	// todo rozlisit oznacenej celej file v editoru od normalniho formatovani
	private boolean isFocusInEditorAndSelectedText() {
		return false;
	}

	public boolean canReformatWithEclipse(PsiFile psiFile) {
		Project project = psiFile.getProject();
		return psiFile.getVirtualFile().isInLocalFileSystem()
				&& FileUtils.isWritable(psiFile.getVirtualFile(), project) && fileTypeIsEnabled(psiFile);
	}

	private boolean isWholeFileOrSelectedText(PsiFile psiFile, int startOffset, int endOffset) {

		final Editor editor = PsiUtilBase.findEditor(psiFile);

		if (editor == null) {
			return true;
		} else {
			Document document = editor.getDocument();
			String text = document.getText();
			boolean wholeFile = FileUtils.isWholeFile(startOffset, endOffset, text);

			return editor.getSelectionModel().hasSelection() || wholeFile;
		}
	}

	private void formatWithIntelliJ(PsiFile psiFile, int startOffset, int endOffset) {
		original.reformatText(psiFile, startOffset, endOffset);
	}

	private boolean isDisabledFileType(VirtualFile virtualFile) {
		String path = virtualFile.getPath();
		DisabledFileTypeSettings disabledFileTypeSettings = settings.geDisabledFileTypeSettings();
		return disabledFileTypeSettings.isDisabled(path);
	}

	private boolean fileTypeIsEnabled(@NotNull PsiFile psiFile) {
		return (FileUtils.isJava(psiFile) && settings.isEnableJavaFormatting())
				|| (FileUtils.isJavaScript(psiFile) && settings.isEnableJSFormatting());
	}

}
