package krasa.formatter.plugin;

/**
 * @author Vojtech Krasa
 */
public class ImportSorterException extends RuntimeException {
	public ImportSorterException() {
	}

	public ImportSorterException(String message) {
		super(message);
	}

	public ImportSorterException(String message, Throwable cause) {
		super(message, cause);
	}

	public ImportSorterException(Throwable cause) {
		super(cause);
	}
}
