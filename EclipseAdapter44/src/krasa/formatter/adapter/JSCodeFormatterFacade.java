package krasa.formatter.adapter;

import krasa.formatter.common.ModifiableFile;
import krasa.formatter.eclipse.CodeFormatterFacade;
import krasa.formatter.exception.FileDoesNotExistsException;
import krasa.formatter.exception.FormattingFailedException;
import krasa.formatter.plugin.InvalidPropertyFile;
import krasa.formatter.settings.Settings;
import krasa.formatter.settings.provider.JSPropertiesProvider;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.text.edits.TextEdit;
import org.eclipse.wst.jsdt.core.formatter.CodeFormatter;

import com.intellij.psi.PsiFile;

/**
 * @author Vojtech Krasa
 */
public class JSCodeFormatterFacade extends CodeFormatterFacade {
	protected org.eclipse.wst.jsdt.core.formatter.CodeFormatter codeFormatter;

	private JSPropertiesProvider propertiesProvider;
	protected ModifiableFile.Monitor modifiedMonitor;

	public JSCodeFormatterFacade(JSPropertiesProvider propertiesProvider) {
		this.propertiesProvider = propertiesProvider;
	}

	private org.eclipse.wst.jsdt.core.formatter.CodeFormatter getCodeFormatter() throws FileDoesNotExistsException {
		if (codeFormatter == null || propertiesProvider.wasChanged(modifiedMonitor)) {
			return newCodeFormatter();
		}
		return codeFormatter;
	}

	private org.eclipse.wst.jsdt.core.formatter.CodeFormatter newCodeFormatter() throws InvalidPropertyFile {
		modifiedMonitor = propertiesProvider.getModifiedMonitor();
		codeFormatter = org.eclipse.wst.jsdt.core.ToolFactory.createCodeFormatter(propertiesProvider.get());
		return codeFormatter;
	}

	@Override
	public String format(String text, int startOffset, int endOffset, PsiFile psiFile)
			throws FileDoesNotExistsException {
		IDocument doc = new Document();
		try {
			// format the file (the meat and potatoes)
			doc.set(text);
			/**
			 * Format <code>source</code>, and returns a text edit that correspond to the difference between the given
			 * string and the formatted string.
			 * <p>
			 * It returns null if the given string cannot be formatted.
			 * </p>
			 *
			 * <p>
			 * If the offset position is matching a whitespace, the result can include whitespaces. It would be up to
			 * the caller to get rid of preceeding whitespaces.
			 * </p>
			 *
			 * @param kind
			 *            Use to specify the kind of the code snippet to format. It can be any of these: K_EXPRESSION,
			 *            K_STATEMENTS, K_CLASS_BODY_DECLARATIONS, K_JAVASCRIPT_UNIT, K_UNKNOWN, K_SINGLE_LINE_COMMENT,
			 *            K_MULTI_LINE_COMMENT, K_JAVA_DOC
			 * @param source
			 *            the source to format
			 * @param offset
			 *            the given offset to start recording the edits (inclusive).
			 * @param length
			 *            the given length to stop recording the edits (exclusive).
			 * @param indentationLevel
			 *            the initial indentation level, used to shift left/right the entire source fragment. An initial
			 *            indentation level of zero or below has no effect.
			 * @param lineSeparator
			 *            the line separator to use in formatted source, if set to <code>null</code>, then the platform
			 *            default one will be used.
			 * @return the text edit
			 * @throws IllegalArgumentException
			 *             if offset is lower than 0, length is lower than 0 or length is greater than source length.
			 */
			TextEdit edit = getCodeFormatter().format(CodeFormatter.K_JAVASCRIPT_UNIT, text, startOffset,
					endOffset - startOffset, 0, Settings.LINE_SEPARATOR);
			if (edit != null) {
				edit.apply(doc);
			} else {
				throw new FormattingFailedException();
			}

			return doc.get();
		} catch (BadLocationException e) {
			throw new RuntimeException(e);
		}
	}

	public String format(String text, int startOffset, int endOffset, int kJavascriptUnit)
			throws FileDoesNotExistsException {
		IDocument doc = new Document();
		try {
			// format the file (the meat and potatoes)
			doc.set(text);
			/**
			 * Format <code>source</code>, and returns a text edit that correspond to the difference between the given
			 * string and the formatted string.
			 * <p>
			 * It returns null if the given string cannot be formatted.
			 * </p>
			 *
			 * <p>
			 * If the offset position is matching a whitespace, the result can include whitespaces. It would be up to
			 * the caller to get rid of preceeding whitespaces.
			 * </p>
			 *
			 * @param kind
			 *            Use to specify the kind of the code snippet to format. It can be any of these: K_EXPRESSION,
			 *            K_STATEMENTS, K_CLASS_BODY_DECLARATIONS, K_JAVASCRIPT_UNIT, K_UNKNOWN, K_SINGLE_LINE_COMMENT,
			 *            K_MULTI_LINE_COMMENT, K_JAVA_DOC
			 * @param source
			 *            the source to format
			 * @param offset
			 *            the given offset to start recording the edits (inclusive).
			 * @param length
			 *            the given length to stop recording the edits (exclusive).
			 * @param indentationLevel
			 *            the initial indentation level, used to shift left/right the entire source fragment. An initial
			 *            indentation level of zero or below has no effect.
			 * @param lineSeparator
			 *            the line separator to use in formatted source, if set to <code>null</code>, then the platform
			 *            default one will be used.
			 * @return the text edit
			 * @throws IllegalArgumentException
			 *             if offset is lower than 0, length is lower than 0 or length is greater than source length.
			 */
			TextEdit edit = getCodeFormatter().format(kJavascriptUnit, text, startOffset, endOffset - startOffset, 0,
					Settings.LINE_SEPARATOR);
			if (edit != null) {
				edit.apply(doc);
			} else {
				throw new FormattingFailedException();
			}

			return doc.get();
		} catch (BadLocationException e) {
			throw new RuntimeException(e);
		}
	}

	public TextEdit format(int kind, String source, int offset, int length, int indentationLevel,
						   String lineSeparator) {
		return getCodeFormatter().format(kind, source, offset, length, indentationLevel, lineSeparator);
	}
}
