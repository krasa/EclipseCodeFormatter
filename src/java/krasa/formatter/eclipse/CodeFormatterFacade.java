package krasa.formatter.eclipse;

import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * @author Vojtech Krasa
 */
public abstract class CodeFormatterFacade {

	@NotNull
	protected Map<String, String> toMap(Properties properties) {
		Map<String, String> options = new HashMap<String, String>();
		for (final String name : properties.stringPropertyNames()) {
			options.put(name, properties.getProperty(name));
		}
		return options;
	}

	/**
	 * @param text
	 *            to format
	 * @param startOffset
	 *            start of formatted area - this should be always start of line
	 * @param endOffset
	 */
	public abstract String format(String text, int startOffset, int endOffset, PsiFile psiFile)
			throws FileDoesNotExistsException;

}
