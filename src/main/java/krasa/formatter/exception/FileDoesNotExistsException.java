package krasa.formatter.exception;

import java.io.File;

/**
 * @author Vojtech Krasa
 */
public class FileDoesNotExistsException extends RuntimeException {

	public FileDoesNotExistsException(File file) {
		super("Configured settings file does not exist, path=\"" + file.getPath() + "\"");
	}

	public FileDoesNotExistsException(String message) {
		super(message);
	}
}
