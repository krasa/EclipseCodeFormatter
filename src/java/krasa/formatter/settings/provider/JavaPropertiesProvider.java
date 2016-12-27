package krasa.formatter.settings.provider;

import krasa.formatter.common.ModifiableFile;
import krasa.formatter.plugin.InvalidPropertyFile;
import krasa.formatter.settings.Settings;

import java.io.File;
import java.util.Properties;

/**
 * @author Vojtech Krasa
 */
public class JavaPropertiesProvider extends CachedPropertiesProvider {
	protected String profile;

	public JavaPropertiesProvider(Settings settings) {
		super(new ModifiableFile(settings.getPathToConfigFileJava()));
		this.profile = settings.getSelectedJavaProfile();
	}

	@Override
	protected Properties readFile(File file) throws InvalidPropertyFile {
		if (file.getName().endsWith("xml")) {
			return readXmlFile(file, profile);
		} else if (file.getName().endsWith("epf")) {
			return readConfigFromWorkspaceMechanicFile(file);
		} else {
			// properties file
			return super.readFile(file);
		}
	}

	private Properties readConfigFromWorkspaceMechanicFile(final File file) {
		Properties result = new Properties();
		Properties properties = super.readFile(file);
		final String prefix = "/instance/org.eclipse.jdt.core/";
		for (Object object : properties.keySet()) {
			String key = (String) object;
			if (key.startsWith(prefix)) {
				String value = properties.getProperty(key);
				result.put(key.substring(prefix.length()), value);
			}
		}
		return result;
	}

}
