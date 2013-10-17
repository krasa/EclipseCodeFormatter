package krasa.formatter.exception;

/**
 * @author Vojtech Krasa
 */
public class FormattingFailedException extends RuntimeException {
	public FormattingFailedException(String s) {
		super(s);
	}

	public FormattingFailedException() {
	}
}
