package krasa.formatter.eclipse;

import com.intellij.openapi.diagnostic.Logger;
import krasa.formatter.exception.FileDoesNotExistsException;

public abstract class EclipseFormatterAdapter {
	protected final Logger LOG = Logger.getInstance(this.getClass().getName());

	public abstract String format(int kind, String text, int startOffset, int length, int indentationLevel, String lineSeparator, String languageLevel)
			throws FileDoesNotExistsException;

	protected String getErrorMessage(String effectiveLanguageLevel) {
		return "languageLevel=" + effectiveLanguageLevel;
	}
}
