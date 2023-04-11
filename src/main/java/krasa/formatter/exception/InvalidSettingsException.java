package krasa.formatter.exception;

import krasa.formatter.plugin.InvalidPropertyFile;

public class InvalidSettingsException extends FormattingFailedException {
	public InvalidSettingsException(String s) {
		super(s,true);
	}

	public InvalidSettingsException(String s, boolean userError) {
		super(s, userError);
	}

	public InvalidSettingsException(InvalidPropertyFile e) {
		super(e);
	}

	public InvalidSettingsException() {
	}
}
