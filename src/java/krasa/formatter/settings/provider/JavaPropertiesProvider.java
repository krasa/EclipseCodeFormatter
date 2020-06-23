package krasa.formatter.settings.provider;

import krasa.formatter.common.ModifiableFile;
import krasa.formatter.plugin.InvalidPropertyFile;
import krasa.formatter.settings.ConfigFileLocator;
import krasa.formatter.settings.Settings;
import krasa.formatter.utils.FileUtils;

import java.io.File;
import java.util.Properties;

/**
 * @author Vojtech Krasa
 */
public class JavaPropertiesProvider extends CachedPropertiesProvider {
	protected String profile;


	public JavaPropertiesProvider(Settings settings) {
		this(settings.getPathToConfigFileJava(), settings.getSelectedJavaProfile());
	}

	public JavaPropertiesProvider(String pathToConfigFileJava, String selectedJavaProfile) {
		super(new ModifiableFile(new ConfigFileLocator(pathToConfigFileJava).resolveConfigFilePath()));
		this.profile = selectedJavaProfile;
	}

	@Override
	protected Properties readFile(File file) throws InvalidPropertyFile {
		if (file.getName().toLowerCase().endsWith("xml")) {
			return readXmlFile(file, profile);
		} else if (file.getName().toLowerCase().endsWith("epf")) {
			return readWorkspaceMechanicFile(file);
		} else if (file.getName().toLowerCase().equals("org.eclipse.jdt.ui.prefs")) {
			return readWorkspaceFile(file);
		} else {
			// org.eclipse.jdt.core.prefs
			return super.readFile(file);
		}
	}

	private Properties readWorkspaceFile(File file) {
		Properties properties = FileUtils.readPropertiesFile(file);
		String xml = properties.getProperty("org.eclipse.jdt.ui.formatterprofiles");
		Properties result = FileUtils.readXmlJavaSettingsFile(xml, properties, profile);
		trimTrailingWhitespaceFromConfigValues(result);
		validateConfig(result, file);
		return result;
	}

	private Properties readWorkspaceMechanicFile(final File file) {
		Properties properties = FileUtils.readPropertiesFile(file);
		Properties result = FileUtils.convertEPF(properties, createDefaultConfig());
		validateConfig(result, file);
		return result;
	}

}
