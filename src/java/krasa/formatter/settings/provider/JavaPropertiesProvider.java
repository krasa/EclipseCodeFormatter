package krasa.formatter.settings.provider;

import java.io.File;
import java.util.Properties;

import krasa.formatter.common.ModifiableFile;
import krasa.formatter.plugin.InvalidPropertyFile;
import krasa.formatter.settings.Settings;

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
	protected Properties createDefaultConfig() {
		Properties defaultConfig = new Properties();
		// TODO: Ideally, the IntelliJ project's language level should be the default value.
		defaultConfig.setProperty("org.eclipse.jdt.core.compiler.source", "1.7");
		return defaultConfig;
	}

	@Override
	protected void validateConfig(Properties config, File file) {
		super.validateConfig(config, file);
		String sourceVersionString = config.getProperty("org.eclipse.jdt.core.compiler.source");
		if (sourceVersionString != null) {
			float sourceVersion = 0;
			try {
				sourceVersion = Float.parseFloat(sourceVersionString);
			} catch (NumberFormatException e) {
				throw new RuntimeException("Illegal value for org.eclipse.jdt.core.compiler.source property ("
						+ sourceVersionString + ") - supported Java source versions are 1.5, 1.6, 1.7, or 1.8.");
			}
			if (sourceVersion < 1.5) {
				throw new RuntimeException("Illegal value for org.eclipse.jdt.core.compiler.source property ("
						+ sourceVersionString + ") - Eclipse formatter requires a Java source version >= 1.5.");
			}
		}
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
