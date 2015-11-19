package krasa.formatter.eclipse;

import krasa.formatter.exception.FileDoesNotExistsException;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.pom.java.LanguageLevel;

public abstract class EclipseFormatterAdapter {
	protected final Logger LOG = Logger.getInstance(this.getClass().getName());
	private Project project;

	public EclipseFormatterAdapter(Project project) {
		this.project = project;
	}

	public abstract String format(String text, int startOffset, int endOffset, LanguageLevel level)
			throws FileDoesNotExistsException;

	protected String getErrorMessage(LanguageLevel effectiveLanguageLevel) {
		return "languageLevel=" + effectiveLanguageLevel;
	}
}
