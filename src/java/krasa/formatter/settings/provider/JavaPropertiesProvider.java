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
		} else {
			// properties file
			return super.readFile(file);
		}
	}

}
