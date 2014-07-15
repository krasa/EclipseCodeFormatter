package krasa.formatter.eclipse;

import com.intellij.psi.PsiFile;

/**
 * @author Vojtech Krasa
 */
public abstract class CodeFormatterFacade {

	/**
	 * @param text
	 *            to format
	 * @param startOffset
	 *            start of formatted area - this should be always start of line
	 * @param endOffset
	 */
	public String format(String text, int startOffset, int endOffset, PsiFile psiFile) throws FileDoesNotExistsException {
		return formatInternal(text, startOffset, endOffset, psiFile);
	}

	protected abstract String formatInternal(String text, int startOffset, int endOffset, PsiFile psiFile)
			throws FileDoesNotExistsException;

}
