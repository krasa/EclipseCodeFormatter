package krasa.formatter.eclipse;

import krasa.formatter.common.ModifiableFile;
import krasa.formatter.exception.FormattingFailedException;
import krasa.formatter.settings.Settings;
import krasa.formatter.settings.provider.JavaPropertiesProvider;

import org.eclipse.jdt.core.formatter.CodeFormatter;
import org.eclipse.jdt.internal.formatter.DefaultCodeFormatter;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.text.edits.TextEdit;

import com.intellij.openapi.diagnostic.Logger;

/**
 * @author Vojtech Krasa
 */
public class JavaCodeFormatterFacade extends CodeFormatterFacade {
	private static final Logger LOG = Logger.getInstance(JavaCodeFormatterFacade.class.getName());

	protected CodeFormatter codeFormatter;
	private JavaPropertiesProvider javaPropertiesProvider;
	protected ModifiableFile.Monitor lastState;

	public JavaCodeFormatterFacade(JavaPropertiesProvider javaPropertiesProvider) {
		this.javaPropertiesProvider = javaPropertiesProvider;
	}

	private CodeFormatter getCodeFormatter() throws FileDoesNotExistsException {
		if (codeFormatter == null || javaPropertiesProvider.wasChanged(lastState)) {
			return newCodeFormatter();
		}
		return codeFormatter;
	}

	private CodeFormatter newCodeFormatter() {
		lastState = javaPropertiesProvider.getModifiedMonitor();
		codeFormatter = new DefaultCodeFormatter(javaPropertiesProvider.get());
		return codeFormatter;
	}

	protected String formatInternal(String text, int startOffset, int endOffset) throws FileDoesNotExistsException {
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
			TextEdit edit = getCodeFormatter().format(
					CodeFormatter.K_COMPILATION_UNIT | CodeFormatter.F_INCLUDE_COMMENTS, text, startOffset,
					endOffset - startOffset, 0, Settings.LINE_SEPARATOR);
			if (edit != null) {
				LOG.debug("reformatting done");
				edit.apply(doc);
			} else {
				throw new FormattingFailedException(getErrorMessage());
			}
			return doc.get();
		} catch (BadLocationException e) {
			throw new RuntimeException(e);
		}
	}

	private String getErrorMessage() {
		String compliance = javaPropertiesProvider.get().getProperty("org.eclipse.jdt.core.compiler.compliance");
		String targetPlatform = javaPropertiesProvider.get().getProperty(
				"org.eclipse.jdt.core.compiler.codegen.targetPlatform");
		String source = javaPropertiesProvider.get().getProperty("org.eclipse.jdt.core.compiler.source");
		return "Make sure these properties from configuration file matches your source code level: org.eclipse.jdt.core.compiler.source="
				+ source + ", org.eclipse.jdt.core.compiler.codegen.targetPlatform" + targetPlatform
				+ ", org.eclipse.jdt.core.compiler.compliance" + compliance;
	}

}
