package krasa.formatter.eclipse;

import java.io.File;

/**
 * @author Vojtech Krasa
 */
public class FileDoesNotExistsException extends RuntimeException {

	public FileDoesNotExistsException(File file) {
		super("File does not exists: " + file.getAbsolutePath());
	}

}
