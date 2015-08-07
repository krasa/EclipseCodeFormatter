package krasa.formatter.plugin;

import java.util.ArrayList;
import java.util.List;

import com.intellij.lang.Language;
import com.intellij.openapi.editor.*;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.codeStyle.CodeStyleSettings;
import com.intellij.psi.codeStyle.CodeStyleSettingsManager;
import com.intellij.psi.codeStyle.CommonCodeStyleSettings;
import krasa.formatter.eclipse.CodeFormatterFacade;
import krasa.formatter.eclipse.FileDoesNotExistsException;
import krasa.formatter.plugin.processor.GWTProcessor;
import krasa.formatter.plugin.processor.JSCommentsFormatterProcessor;
import krasa.formatter.plugin.processor.Processor;
import krasa.formatter.settings.Settings;
import krasa.formatter.utils.FileUtils;

import org.jetbrains.annotations.NotNull;

import com.intellij.codeInsight.template.impl.TemplateManagerImpl;
import com.intellij.codeInsight.template.impl.TemplateState;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiUtilBase;

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
		postProcess(document, psiFile, range);
		fileDocumentManager.saveDocument(document);
	}

	/* when file is being edited, it is important to load text from editor, i think */
	private void formatWhenEditorIsOpen(Editor editor, Range range, PsiFile file) throws FileDoesNotExistsException {
		LOG.debug("#formatWhenEditorIsOpen " + file.getName());
		CaretPositionKeeper intelliJCaretKeeper = new CaretPositionKeeper(editor, CodeStyleSettingsManager.getSettings(editor.getProject()), file.getLanguage());

		Document document = editor.getDocument();
		// http://code.google.com/p/eclipse-code-formatter-intellij-plugin/issues/detail?id=7
		PsiDocumentManager.getInstance(editor.getProject()).doPostponedOperationsAndUnblockDocument(document);

		final CaretModel caretModel = editor.getCaretModel();
		final int caretOffset = caretModel.getOffset();
		final RangeMarker myCaretKeeper = document.createRangeMarker(caretOffset, caretOffset);

		String text = document.getText();
		String reformat = reformat(range.getStartOffset(), range.getEndOffset(), text, file);
		document.setText(reformat);
		postProcess(document, file, range);

		editor.getCaretModel().moveToOffset(myCaretKeeper.getEndOffset());
		myCaretKeeper.dispose();
		intelliJCaretKeeper.restoreCaretPosition();
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


	/** copypaste from com.intellij.psi.impl.source.codeStyle.CodeStyleManagerImpl.CaretPositionKeeper*/
	// There is a possible case that cursor is located at the end of the line that contains only white spaces. For example:
	//     public void foo() {
	//         <caret>
	//     }
	// Formatter removes such white spaces, i.e. keeps only line feed symbol. But we want to preserve caret position then.
	// So, if 'virtual space in editor' is enabled, we save target visual column. Caret indent is ensured otherwise
	private static class CaretPositionKeeper {
		Editor myEditor;
		Document myDocument;
		CaretModel myCaretModel;
		RangeMarker myBeforeCaretRangeMarker;
		String myCaretIndentToRestore;
		int myVisualColumnToRestore = -1;
		boolean myBlankLineIndentPreserved = true;

		CaretPositionKeeper(@NotNull Editor editor, @NotNull CodeStyleSettings settings, @NotNull Language language) {
			myEditor = editor;
			myCaretModel = editor.getCaretModel();
			myDocument = editor.getDocument();
			myBlankLineIndentPreserved = isBlankLineIndentPreserved(settings, language);

			int caretOffset = getCaretOffset();
			int lineStartOffset = getLineStartOffsetByTotalOffset(caretOffset);
			int lineEndOffset = getLineEndOffsetByTotalOffset(caretOffset);
			boolean shouldFixCaretPosition = rangeHasWhiteSpaceSymbolsOnly(myDocument.getCharsSequence(), lineStartOffset, lineEndOffset);

			if (shouldFixCaretPosition) {
				initRestoreInfo(caretOffset);
			}
		}

		private static boolean isBlankLineIndentPreserved(@NotNull CodeStyleSettings settings, @NotNull Language language) {
			CommonCodeStyleSettings langSettings = settings.getCommonSettings(language);
			if (langSettings != null) {
				CommonCodeStyleSettings.IndentOptions indentOptions = langSettings.getIndentOptions();
				return indentOptions != null && indentOptions.KEEP_INDENTS_ON_EMPTY_LINES;
			}
			return false;
		}

		private void initRestoreInfo(int caretOffset) {
			int lineStartOffset = getLineStartOffsetByTotalOffset(caretOffset);

			myVisualColumnToRestore = myCaretModel.getVisualPosition().column;
			myCaretIndentToRestore = myDocument.getText(TextRange.create(lineStartOffset, caretOffset));
			myBeforeCaretRangeMarker = myDocument.createRangeMarker(0, lineStartOffset);
		}

		public void restoreCaretPosition() {
			if (isVirtualSpaceEnabled()) {
				restoreVisualPosition();
			}
			else {
				restorePositionByIndentInsertion();
			}
		}

		private void restorePositionByIndentInsertion() {
			if (myBeforeCaretRangeMarker == null ||
					!myBeforeCaretRangeMarker.isValid() ||
					myCaretIndentToRestore == null ||
					myBlankLineIndentPreserved) {
				return;
			}
			int newCaretLineStartOffset = myBeforeCaretRangeMarker.getEndOffset();
			myBeforeCaretRangeMarker.dispose();
			if (myCaretModel.getVisualPosition().column == myVisualColumnToRestore) {
				return;
			}
			insertWhiteSpaceIndentIfNeeded(newCaretLineStartOffset);
		}

		private void restoreVisualPosition() {
			if (myVisualColumnToRestore < 0) {
				myEditor.getScrollingModel().scrollToCaret(ScrollType.RELATIVE);
				return;
			}
			VisualPosition position = myCaretModel.getVisualPosition();
			if (myVisualColumnToRestore != position.column) {
				myCaretModel.moveToVisualPosition(new VisualPosition(position.line, myVisualColumnToRestore));
			}
		}

		private void insertWhiteSpaceIndentIfNeeded(int caretLineOffset) {
			int lineToInsertIndent = myDocument.getLineNumber(caretLineOffset);
			if (!lineContainsWhiteSpaceSymbolsOnly(lineToInsertIndent))
				return;

			int lineToInsertStartOffset = myDocument.getLineStartOffset(lineToInsertIndent);

			if (lineToInsertIndent != getCurrentCaretLine()) {
				myCaretModel.moveToOffset(lineToInsertStartOffset);
			}
			myDocument.replaceString(lineToInsertStartOffset, caretLineOffset, myCaretIndentToRestore);
		}

		private boolean rangeHasWhiteSpaceSymbolsOnly(CharSequence text, int lineStartOffset, int lineEndOffset) {
			for (int i = lineStartOffset; i < lineEndOffset; i++) {
				char c = text.charAt(i);
				if (c != ' ' && c != '\t' && c != '\n') {
					return false;
				}
			}
			return true;
		}

		private boolean isVirtualSpaceEnabled() {
			return myEditor.getSettings().isVirtualSpace();
		}

		private int getLineStartOffsetByTotalOffset(int offset) {
			int line = myDocument.getLineNumber(offset);
			return myDocument.getLineStartOffset(line);
		}

		private int getLineEndOffsetByTotalOffset(int offset) {
			int line = myDocument.getLineNumber(offset);
			return myDocument.getLineEndOffset(line);
		}

		private int getCaretOffset() {
			int caretOffset = myCaretModel.getOffset();
			caretOffset = Math.max(Math.min(caretOffset, myDocument.getTextLength() - 1), 0);
			return caretOffset;
		}

		private boolean lineContainsWhiteSpaceSymbolsOnly(int lineNumber) {
			int startOffset = myDocument.getLineStartOffset(lineNumber);
			int endOffset = myDocument.getLineEndOffset(lineNumber);
			return rangeHasWhiteSpaceSymbolsOnly(myDocument.getCharsSequence(), startOffset, endOffset);
		}

		private int getCurrentCaretLine() {
			return myDocument.getLineNumber(myCaretModel.getOffset());
		}
	}
}
