package krasa.formatter.exception;

/**
 * @author Vojtech Krasa
 */
public class ParsingFailedException extends RuntimeException {
	public ParsingFailedException(Exception e) {
		super(e);
	}

	public ParsingFailedException() {
	}

	public ParsingFailedException(String s) {
		super(s);
	}

	public ParsingFailedException(String message, Throwable cause) {
		super(message, cause);
	}

	public ParsingFailedException(Throwable cause) {
		super(cause);
	}
}
