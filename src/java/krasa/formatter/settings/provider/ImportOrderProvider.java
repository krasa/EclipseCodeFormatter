package krasa.formatter.settings.provider;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import krasa.formatter.common.ModifiableFile;
import krasa.formatter.exception.ParsingFailedException;
import krasa.formatter.settings.Settings;
import krasa.formatter.utils.FileUtils;
import krasa.formatter.utils.StringUtils;

/**
 * @author Vojtech Krasa
 */
public class ImportOrderProvider extends CachedProvider<List<String>> {

	public ImportOrderProvider(Settings settings) {
		super(new ModifiableFile(settings.getImportOrderConfigFilePath()));
	}

	@Override
	protected List<String> readFile(File file) {
		Properties properties = FileUtils.readPropertiesFile(file);
		String property = properties.getProperty("org.eclipse.jdt.ui.importorder");
		List<String> order;
		if (property != null) {
			order = StringUtils.trimToList(property);
		} else if (property == null && file.getName().endsWith(".prefs")) {
			throw new ParsingFailedException(
					"File is missing a property 'org.eclipse.jdt.ui.importorder', see instructions.");
		} else if (file.getName().endsWith(".importorder")) {
			order = loadImportOrderFile(file);
		} else {
			throw new ParsingFailedException(
					"You must provide either *.importorder file or 'org.eclipse.jdt.ui.prefs' file, see instructions.");
		}
		return order;
	}

	private List<String> loadImportOrderFile(File file) {
		List<String> fileLines = loadFile(file);
		Collections.sort(fileLines);
		return toImportOrder(fileLines);
	}

	private List<String> toImportOrder(List<String> fileLines) {
		List<String> order = new ArrayList<String>();
		for (String s : fileLines) {
			if (s.contains("=")) {
				order.add(s.substring(s.indexOf("=") + 1));
			}
		}
		return order;
	}

	private List<String> loadFile(File file) {
		List<String> stringList;
		try {
			stringList = org.apache.commons.io.FileUtils.readLines(file);
		} catch (IOException e) {
			throw new RuntimeException("Loading of import order from file failed", e);
		}
		return stringList;
	}

}
