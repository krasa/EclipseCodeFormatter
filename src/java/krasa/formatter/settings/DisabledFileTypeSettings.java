package krasa.formatter.settings;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Vojtech Krasa
 */
public class DisabledFileTypeSettings {

	private List<String> disabledTypes = new ArrayList<String>();

	public DisabledFileTypeSettings(String disabledFileTypes) {
		for (String group : disabledFileTypes.split(";")) {
			if (group.isEmpty()) {
				continue;
			}
			disabledTypes.add(group.trim());
		}
	}

	public boolean isDisabled(String path) {
		for (String disabledType : disabledTypes) {
			boolean b = path.endsWith(disabledType);
			if (b) {
				return true;
			}
		}
		return false;
	}

}
