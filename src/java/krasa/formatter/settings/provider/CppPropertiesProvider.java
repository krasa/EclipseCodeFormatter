package krasa.formatter.settings.provider;

import krasa.formatter.common.ModifiableFile;
import krasa.formatter.plugin.InvalidPropertyFile;
import krasa.formatter.settings.Settings;

import java.io.File;
import java.util.*;

/**
 * @author Vojtech Krasa
 */
public class CppPropertiesProvider extends CachedPropertiesProvider {
	protected String profile;

	public CppPropertiesProvider(Settings settings) {
		super(new ModifiableFile(settings.getPathToConfigFileCpp()));
		this.profile = settings.getSelectedCppProfile();
	}

	@Override
	protected Properties createDefaultConfig() {
		return new Properties();
	}

	@Override
	protected void validateConfig(Properties config, File file) {
		super.validateConfig(config, file);
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
