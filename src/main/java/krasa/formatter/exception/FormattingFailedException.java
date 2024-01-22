package krasa.formatter.exception;

import com.intellij.openapi.diagnostic.Logger;
import krasa.formatter.plugin.InvalidPropertyFile;

/**
 * @author Vojtech Krasa
 */
public class FormattingFailedException extends RuntimeException {
	protected final Logger LOG = Logger.getInstance(this.getClass().getName());

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

	public FormattingFailedException(Exception e, String errorMessage) {
		super(errorMessage, e);
		//todo hack
		LOG.debug(e);
	}

	public boolean isUserError() {

		return userError;
	}

	public FormattingFailedException() {
	}
}
