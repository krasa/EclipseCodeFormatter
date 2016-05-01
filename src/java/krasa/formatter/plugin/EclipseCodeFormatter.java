package krasa.formatter.plugin;

import com.intellij.codeInsight.template.impl.TemplateManagerImpl;
import com.intellij.codeInsight.template.impl.TemplateState;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.*;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiUtilBase;
import krasa.formatter.eclipse.Classloaders;
import krasa.formatter.eclipse.CodeFormatterFacade;
import krasa.formatter.exception.FileDoesNotExistsException;
import krasa.formatter.processor.Processor;
import krasa.formatter.settings.Settings;
import krasa.formatter.utils.FileUtils;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Vojtech Krasa
 */
public class EclipseCodeFormatter {

	private static final Logger LOG = Logger.getInstance(EclipseCodeFormatter.class.getName());

	private Settings settings;
	@NotNull
	protected final CodeFormatterFacade codeFormatterFacade;

	private List<Processor> postProcessors;

	public EclipseCodeFormatter(@NotNull Settings settings, @NotNull CodeFormatterFacade codeFormatterFacade1) {
		this.settings = settings;
		codeFormatterFacade = codeFormatterFacade1;
		postProcessors = new ArrayList<Processor>();
		postProcessors.add(Classloaders.getGWTProcessor(settings));
		postProcessors.add(Classloaders.getJSCommentsFormatterProcessor(settings));
	}

	public void format(PsiFile psiFile, int startOffset, int endOffset) throws FileDoesNotExistsException {
		LOG.debug("#format " + startOffset + "-" + endOffset);
		boolean wholeFile = FileUtils.isWholeFile(startOffset, endOffset, psiFile.getText());
		Range range = new Range(startOffset, endOffset, wholeFile);

		final Editor editor = PsiUtilBase.findEditor(psiFile);
		if (editor != null) {
			TemplateState templateState = TemplateManagerImpl.getTemplateState(editor);
			if (templateState != null && !settings.isUseForLiveTemplates()) {
				throw new ReformatItInIntelliJ();
			}
			formatWhenEditorIsOpen(editor, range, psiFile);
		} else {
			formatWhenEditorIsClosed(range, psiFile);
		}

	}

	private void formatWhenEditorIsClosed(Range range, PsiFile psiFile) throws FileDoesNotExistsException {
		LOG.debug("#formatWhenEditorIsClosed " + psiFile.getName());

		VirtualFile virtualFile = psiFile.getVirtualFile();
		FileDocumentManager fileDocumentManager = FileDocumentManager.getInstance();
		Document document = fileDocumentManager.getDocument(virtualFile);
		fileDocumentManager.saveDocument(document); // when file is edited and editor is closed, it is needed to save
		// the text
		String text = document.getText();
		String reformat = reformat(range.getStartOffset(), range.getEndOffset(), text, psiFile);

		document.setText(reformat);
		postProcess(document, psiFile, range, fileDocumentManager);
	}

	/* when file is being edited, it is important to load text from editor, i think */
	private void formatWhenEditorIsOpen(Editor editor, Range range, PsiFile file) throws FileDoesNotExistsException {
		LOG.debug("#formatWhenEditorIsOpen " + file.getName());
		int visualColumnToRestore = getVisualColumnToRestore(editor);

		Document document = editor.getDocument();
		// http://code.google.com/p/eclipse-code-formatter-intellij-plugin/issues/detail?id=7
		PsiDocumentManager.getInstance(editor.getProject()).doPostponedOperationsAndUnblockDocument(document);

		int caretOffset = editor.getCaretModel().getOffset();
		RangeMarker rangeMarker = document.createRangeMarker(caretOffset, caretOffset);

		String text = document.getText();
		String reformat = reformat(range.getStartOffset(), range.getEndOffset(), text, file);
		document.setText(reformat);
		postProcess(document, file, range, FileDocumentManager.getInstance());

		restoreVisualColumn(editor, visualColumnToRestore, rangeMarker);
		rangeMarker.dispose();

		LOG.debug("#formatWhenEditorIsOpen done");
	}

	private void postProcess(Document document, PsiFile file, Range range, FileDocumentManager fileDocumentManager) {
		for (Processor postProcessor : postProcessors) {
			postProcessor.process(document, file, range);
		}
		// updates psi, so comments from import statements does not get duplicated
		final PsiDocumentManager manager = PsiDocumentManager.getInstance(file.getProject());
		manager.commitDocument(document);
		fileDocumentManager.saveDocument(document);
	}

	private String reformat(int startOffset, int endOffset, String text, PsiFile psiFile)
			throws FileDoesNotExistsException {
		return codeFormatterFacade.format(text, getLineStartOffset(startOffset, text), endOffset, psiFile);
	}

	/**
	 * start offset must be on the start of line
	 */
	private int getLineStartOffset(int startOffset, String text) {
		if (startOffset == 0) {
			return 0;
		}
		return text.substring(0, startOffset).lastIndexOf(Settings.LINE_SEPARATOR) + 1;
	}

	private void restoreVisualColumn(Editor editor, int visualColumnToRestore, RangeMarker rangeMarker) {
		// CaretImpl.updateCaretPosition() contains some magic which moves the caret on bad position, this should
		// restore it on a better place
		int endOffset = rangeMarker.getEndOffset();
		if (endOffset < editor.getDocument().getTextLength()) {
			editor.getCaretModel().moveToOffset(endOffset);
		}

		if (visualColumnToRestore < 0) {
		} else {
			CaretModel caretModel = editor.getCaretModel();
			VisualPosition position = caretModel.getVisualPosition();
			if (visualColumnToRestore != position.column) {
				caretModel.moveToVisualPosition(new VisualPosition(position.line, visualColumnToRestore));
			}
		}
	}

	// There is a possible case that cursor is located at the end of the line that contains only white spaces. For
	// example:
	// public void foo() {
	// <caret>
	// }
	// Formatter removes such white spaces, i.e. keeps only line feed symbol. But we want to preserve caret position
	// then.
	// So, we check if it should be preserved and restore it after formatting if necessary
	/** copypaste from intellij, todo update it from IJ 15 - not compatible with IJ 13 */
	private int getVisualColumnToRestore(Editor editor) {
		int visualColumnToRestore = -1;

		if (editor != null) {
			Document document1 = editor.getDocument();
			int caretOffset = editor.getCaretModel().getOffset();
			caretOffset = Math.max(Math.min(caretOffset, document1.getTextLength() - 1), 0);
			CharSequence text1 = document1.getCharsSequence();
			int caretLine = document1.getLineNumber(caretOffset);
			int lineStartOffset = document1.getLineStartOffset(caretLine);
			int lineEndOffset = document1.getLineEndOffset(caretLine);
			boolean fixCaretPosition = true;
			for (int i = lineStartOffset; i < lineEndOffset; i++) {
				char c = text1.charAt(i);
				if (c != ' ' && c != '\t' && c != '\n') {
					fixCaretPosition = false;
					break;
				}
			}
			if (fixCaretPosition) {
				visualColumnToRestore = editor.getCaretModel().getVisualPosition().column;
			}
		}
		return visualColumnToRestore;
	}

}
