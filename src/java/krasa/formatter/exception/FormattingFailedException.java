package krasa.formatter.exception;

/**
 * @author Vojtech Krasa
 */
public class FormattingFailedException extends RuntimeException {
	private boolean userError;

	public FormattingFailedException(String s) {
		super(s);
	}

	public FormattingFailedException(String s, boolean userError) {
		super(s);
		this.userError = userError;
	}

	public boolean isUserError() {

		return userError;
	}

	public FormattingFailedException() {
	}
}
