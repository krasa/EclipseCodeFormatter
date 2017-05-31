package krasa.formatter.plugin;

/**
 * @author Vojtech Krasa
 */
public enum Mode {
	ALWAYS_FORMAT,
	WITH_CTRL_SHIFT_ENTER_CHECK;

	boolean shouldReformat(boolean wholeFileOrSelectedText) {
		switch (this) {
		/* when formatting only vcs changes, this is needed. */
		case ALWAYS_FORMAT:
			return true;
		/* live templates gets broken without that */
		case WITH_CTRL_SHIFT_ENTER_CHECK:
			return wholeFileOrSelectedText;
		}
		return true;
	}
}
