package krasa.formatter.plugin;

import java.io.File;

/**
 * @author Vojtech Krasa
 */
public class InvalidPropertyFile extends RuntimeException {

	public InvalidPropertyFile(String s, File file) {
		super("Property " + s + " does not exists in " + file.getAbsolutePath());
	}

	public InvalidPropertyFile(File file) {
		super("Property file does not contains any properties, " + file.getAbsolutePath());
	}

	public InvalidPropertyFile(String message, Throwable cause) {
		super(message, cause);
	}

	public InvalidPropertyFile(Throwable cause) {
		super(cause);
	}
}
