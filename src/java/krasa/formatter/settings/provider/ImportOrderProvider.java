package krasa.formatter.settings.provider;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import krasa.formatter.common.ModifiableFile;
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
		} else {
			order = loadImportOrder(file);
		}
		return order;
	}

	private List<String> loadImportOrder(File file) {
		List<String> fileLines = loadFile(file);
		Collections.sort(fileLines);
		return transform(fileLines);
	}

	private List<String> transform(List<String> fileLines) {
		List<String> order = new ArrayList<String>();
		for (String s : fileLines) {
			String[] split = s.split("=");
			if (split.length == 2) {
				order.add(split[1]);
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
