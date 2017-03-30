package krasa.formatter.utils;

import krasa.formatter.settings.Settings;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Vojtech Krasa
 */
public class StringUtils {

	public static String betterMatching(String order1, String order2, String anImport) {
		if (order1.equals(order2)) {
			throw new IllegalArgumentException("orders are same");
		}
		for (int i = 0; i < anImport.length() - 1; i++) {
			if (order1.length() - 1 == i && order2.length() - 1 != i) {
				return order2;
			}
			if (order2.length() - 1 == i && order1.length() - 1 != i) {
				return order1;
			}
			char orderChar1 = order1.length() != 0 ? order1.charAt(i) : ' ';
			char orderChar2 = order2.length() != 0 ? order2.charAt(i) : ' ';
			char importChar = anImport.charAt(i);

			if (importChar == orderChar1 && importChar != orderChar2) {
				return order1;
			} else if (importChar != orderChar1 && importChar == orderChar2) {
				return order2;
			}

		}
		return null;
	}

	public static List<String> trimToList(String importOrder1) {
		ArrayList<String> strings = new ArrayList<String>();
		String[] split = importOrder1.split(";");
		for (String s : split) {
			String trim = s.trim();
			strings.add(trim);
		}
		return strings;
	}

	public static List<String> trimImports(String imports) {
		String[] split = imports.split("\n");
		ArrayList<String> strings = new ArrayList<String>();
		for (int i = 0; i < split.length; i++) {
			String s = split[i];
			if (s.startsWith("import ")) {
				s = s.substring(7, s.indexOf(";"));
				strings.add(s);
			}
		}
		return strings;
	}

	public static List<String> trimImports(List<String> imports) {
		ArrayList<String> strings = new ArrayList<String>();
		for (int i = 0; i < imports.size(); i++) {
			String s = imports.get(i);
			if (s.startsWith("import ")) {
				s = s.substring(7, s.indexOf(";"));
				strings.add(s.trim());
			} else {
				strings.add(s.trim());
			} 
		}
		return strings;
	}


	public static String generateName(List<Settings> settingsList, int i, String name, String resultName) {
		if (resultName == null) {
			resultName = name;
		}
		for (Settings settings : settingsList) {
			if (resultName.equals(settings.getName())) {
				resultName = name + " (" + i + ")";
				resultName = generateName(settingsList, ++i, name, resultName);
			}
		}
		return resultName;
	}

	public static String generateName(List<Settings> settingsList, int i, String name) {
		return generateName(settingsList, i, name, name);

	}

	public static String getSimpleName(String s) {
		int lastDot = s.lastIndexOf(".");
		if (lastDot == -1) {
			return s;
		}
		return s.substring(lastDot + 1, s.length());
	}

	public static String getPackage(String s) {
		int lastDot = s.lastIndexOf(".");
		if (lastDot == -1) {
			return "";
		}
		return s.substring(0, lastDot);
	}
}
