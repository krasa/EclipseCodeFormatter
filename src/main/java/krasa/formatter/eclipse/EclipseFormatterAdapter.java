package krasa.formatter.eclipse;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.pom.java.LanguageLevel;
import krasa.formatter.exception.FileDoesNotExistsException;

public abstract class EclipseFormatterAdapter {
	protected final Logger LOG = Logger.getInstance(this.getClass().getName());

	public abstract String format(String text, int startOffset, int endOffset, LanguageLevel level)
			throws FileDoesNotExistsException;

	protected String getErrorMessage(LanguageLevel effectiveLanguageLevel) {
		return "languageLevel=" + effectiveLanguageLevel;
	}
}
