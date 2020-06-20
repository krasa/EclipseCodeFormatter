package krasa.formatter.exception;

import krasa.formatter.plugin.InvalidPropertyFile;

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

	public FormattingFailedException(InvalidPropertyFile e) {
		super(e);
	}

	public boolean isUserError() {

		return userError;
	}

	public FormattingFailedException() {
	}
}
