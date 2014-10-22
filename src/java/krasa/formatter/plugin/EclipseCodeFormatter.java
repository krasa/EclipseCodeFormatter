package krasa.formatter.plugin;

import com.intellij.codeInsight.template.impl.*;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.*;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.intellij.psi.util.PsiUtilBase;
import krasa.formatter.eclipse.*;
import krasa.formatter.plugin.processor.*;
import krasa.formatter.settings.Settings;
import krasa.formatter.utils.FileUtils;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * @author Vojtech Krasa
 */
public class EclipseCodeFormatter {

	private static final Logger LOG = Logger.getInstance(EclipseCodeFormatter.class.getName());

	@NotNull
	protected final CodeFormatterFacade codeFormatterFacade;

	private List<Processor> postProcessors;

	public EclipseCodeFormatter(@NotNull Settings settings, @NotNull CodeFormatterFacade codeFormatterFacade1) {
		codeFormatterFacade = codeFormatterFacade1;
		postProcessors = new ArrayList<Processor>();
		postProcessors.add(new GWTProcessor(settings));
		postProcessors.add(new JSCommentsFormatterProcessor(settings));
	}

	public void format(PsiFile psiFile, int startOffset, int endOffset) throws FileDoesNotExistsException {
		LOG.debug("#format " + startOffset + "-" + endOffset);
		boolean wholeFile = FileUtils.isWholeFile(startOffset, endOffset, psiFile.getText());
		Range range = new Range(startOffset, endOffset, wholeFile);

		final Editor editor = PsiUtilBase.findEditor(psiFile);
		if (editor != null) {
			TemplateState templateState = TemplateManagerImpl.getTemplateState(editor);
			if (templateState != null) {
				throw new ReformatItInIntelliJ();
			}
			formatWhenEditorIsOpen(range, psiFile);
		} else {
			formatWhenEditorIsClosed(psiFile);
		}

	}

	private void formatWhenEditorIsClosed(PsiFile psiFile) throws FileDoesNotExistsException {
		LOG.debug("#formatWhenEditorIsClosed " + psiFile.getName());

		VirtualFile virtualFile = psiFile.getVirtualFile();
		FileDocumentManager fileDocumentManager = FileDocumentManager.getInstance();
		Document document = fileDocumentManager.getDocument(virtualFile);
		fileDocumentManager.saveDocument(document); // when file is edited and editor is closed, it is needed to save
		// the text
		String text = document.getText();
		String reformat = reformat(0, text.length(), text, psiFile);
		document.setText(reformat);
		postProcess(document, psiFile, new Range(-1, -1, true));
		fileDocumentManager.saveDocument(document);
	}

	/* when file is being edited, it is important to load text from editor, i think */
	private void formatWhenEditorIsOpen(Range range, PsiFile file) throws FileDoesNotExistsException {
		LOG.debug("#formatWhenEditorIsOpen " + file.getName());
		final Editor editor = PsiUtilBase.findEditor(file);
		int visualColumnToRestore = getVisualColumnToRestore(editor);

		Document document = editor.getDocument();
		// http://code.google.com/p/eclipse-code-formatter-intellij-plugin/issues/detail?id=7
		PsiDocumentManager.getInstance(editor.getProject()).doPostponedOperationsAndUnblockDocument(document);

		String text = document.getText();
		String reformat = reformat(range.getStartOffset(), range.getEndOffset(), text, file);
		document.setText(reformat);
		postProcess(document, file, range);

		restoreVisualColumn(editor, visualColumnToRestore);
		LOG.debug("#formatWhenEditorIsOpen done");
	}

	private void postProcess(Document document, PsiFile file, Range range) {
		for (Processor postProcessor : postProcessors) {
			postProcessor.process(document, file, range);
		}
		// updates psi, so comments from import statements does not get duplicated
		final PsiDocumentManager manager = PsiDocumentManager.getInstance(file.getProject());
		manager.commitDocument(document);
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

	private void restoreVisualColumn(Editor editor, int visualColumnToRestore) {
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
