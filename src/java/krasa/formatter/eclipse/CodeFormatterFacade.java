package krasa.formatter.eclipse;

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
	 *            end of formatted area
	 */
	public String format(String text, int startOffset, int endOffset) throws FileDoesNotExistsException {
		return formatInternal(text, startOffset, endOffset);
	}

	protected abstract String formatInternal(String text, int startOffset, int endOffset)
			throws FileDoesNotExistsException;

}
