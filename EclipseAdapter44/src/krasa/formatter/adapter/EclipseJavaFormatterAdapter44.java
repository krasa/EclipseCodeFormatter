package krasa.formatter.adapter;

import com.intellij.pom.java.LanguageLevel;
import krasa.formatter.eclipse.EclipseFormatterAdapter;
import krasa.formatter.exception.FileDoesNotExistsException;
import krasa.formatter.exception.FormattingFailedException;
import krasa.formatter.settings.Settings;
import org.eclipse.jdt.core.formatter.CodeFormatter;
import org.eclipse.jdt.internal.formatter.DefaultCodeFormatter;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.text.edits.TextEdit;

import java.util.Map;

@SuppressWarnings("Duplicates")
public class EclipseJavaFormatterAdapter44 extends EclipseFormatterAdapter {

	protected DefaultCodeFormatter defaultCodeFormatter;

	@SuppressWarnings("unused")
	public EclipseJavaFormatterAdapter44(Map options) {
		defaultCodeFormatter = new DefaultCodeFormatter(options);
	}

	@Override
	public String format(String text, int startOffset, int endOffset, LanguageLevel level)
			throws FileDoesNotExistsException {

		LOG.debug("#formatInternal");
		if (endOffset > text.length()) {
			endOffset = text.length();
		}
		IDocument doc = new Document();
		try {
			doc.set(text);
			/**
			 * Format <code>source</code>, and returns a text edit that correspond to the difference between the given
			 * string and the formatted string.
			 * <p>
			 * It returns null if the given string cannot be formatted.
			 * </p>
			 * <p>
			 * If the offset position is matching a whitespace, the result can include whitespaces. It would be up to
			 * the caller to get rid of preceding whitespaces.
			 * </p>
			 *
			 * @param kind
			 *            Use to specify the kind of the code snippet to format. It can be any of these:
			 *            <ul>
			 *            <li>{@link #K_EXPRESSION}</li>
			 *            <li>{@link #K_STATEMENTS}</li>
			 *            <li>{@link #K_CLASS_BODY_DECLARATIONS}</li>
			 *            <li>{@link #K_COMPILATION_UNIT}<br>
			 *            <b>Since 3.4</b>, the comments can be formatted on the fly while using this kind of code
			 *            snippet<br>
			 *            (see {@link #F_INCLUDE_COMMENTS} for more detailed explanation on this flag)</li>
			 *            <li>{@link #K_UNKNOWN}</li>
			 *            <li>{@link #K_SINGLE_LINE_COMMENT}</li>
			 *            <li>{@link #K_MULTI_LINE_COMMENT}</li>
			 *            <li>{@link #K_JAVA_DOC}</li>
			 *            </ul>
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

			LOG.debug("#starting to format by eclipse formatter");
			TextEdit edit = defaultCodeFormatter.format(
					CodeFormatter.K_COMPILATION_UNIT | CodeFormatter.F_INCLUDE_COMMENTS, text, startOffset,
					endOffset - startOffset, 0, Settings.LINE_SEPARATOR);
			if (edit != null) {
				LOG.debug("reformatting done");
				edit.apply(doc);
			} else {
				throw new FormattingFailedException(getErrorMessage(level));
			}
			return doc.get();
		} catch (BadLocationException e) {
			throw new RuntimeException(e);
		}
	}
}
