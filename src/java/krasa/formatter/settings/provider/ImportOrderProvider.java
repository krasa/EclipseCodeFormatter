package krasa.formatter.settings.provider;

import krasa.formatter.common.ModifiableFile;
import krasa.formatter.exception.ParsingFailedException;
import krasa.formatter.settings.Settings;
import krasa.formatter.utils.FileUtils;
import krasa.formatter.utils.StringUtils;

import java.io.File;
import java.util.*;

/**
 * @author Vojtech Krasa
 */
public class ImportOrderProvider extends CachedProvider<List<String>> {

	public ImportOrderProvider(Settings settings) {
		super(new ModifiableFile(settings.getImportOrderConfigFilePath()));
	}

	public static List<String> toList(String importOrder) {
		return StringUtils.trimToList(importOrder);
	}

	@Override
	protected List<String> readFile(File file) {
		Properties properties = FileUtils.readPropertiesFile(file);
		String property = properties.getProperty("org.eclipse.jdt.ui.importorder");
		List<String> order;
		if (property != null) {
			order = toList(property);
		} else if (property == null && file.getName().endsWith(".prefs")) {
			throw new ParsingFailedException(
					"File is missing a property 'org.eclipse.jdt.ui.importorder', see instructions.");
		} else if (file.getName().endsWith(".importorder")) {
			order = loadImportOrderFile(properties);
		} else {
			throw new ParsingFailedException(
					"You must provide either *.importorder file or 'org.eclipse.jdt.ui.prefs' file, see instructions.");
		}
		return order;
	}

	private List<String> loadImportOrderFile(Properties file) {
		TreeMap treeMap = new TreeMap(new Comparator<String>() {
			@Override
			public int compare(String o1, String o2) {
				return Integer.parseInt(o1) - Integer.parseInt(o2);
			}
		});
		treeMap.putAll(file);
		return new ArrayList<String>(treeMap.values());
	}



}
