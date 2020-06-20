package krasa.formatter.settings;

import com.intellij.ui.SortedComboBoxModel;
import krasa.formatter.exception.FileDoesNotExistsException;
import krasa.formatter.exception.ParsingFailedException;
import krasa.formatter.plugin.ProjectSettingsForm;
import krasa.formatter.utils.FileUtils;
import org.apache.commons.io.IOUtils;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;

public class ConfigFileLocator {
	private String path;

	public ConfigFileLocator(String text) {
		path = text;
	}

	public String resolveConfigFilePath() {
		File file = new File(path);

		if (!file.exists()) {
			throw new FileDoesNotExistsException(file);
		}

		if (file.isDirectory()) {
			File uiPrefs = org.apache.commons.io.FileUtils.getFile(file, ".metadata", ".plugins", "org.eclipse.core.runtime", ".settings", "org.eclipse.jdt.ui.prefs");
			if (uiPrefs.exists()) {
				return uiPrefs.getAbsolutePath();
			}

			File corePrefs = org.apache.commons.io.FileUtils.getFile(file, ".settings", "org.eclipse.jdt.core.prefs");
			if (corePrefs.exists()) {
				return corePrefs.getAbsolutePath();
			}
			throw new FileDoesNotExistsException("Invalid config location: " + path);
		}

		return path;
	}

	public void validate(ProjectSettingsForm projectSettingsForm,
						 SortedComboBoxModel profilesModel) {
		String text = path;
		final File file = new File(text);
		String lowerCaseName = file.getName().toLowerCase();
		JComboBox comboBox = projectSettingsForm.javaFormatterProfile;
		comboBox.setEnabled(true);
		comboBox.setBorder(projectSettingsForm.normalBorder);

		try {
			if (!file.exists()) {
				invalid(ProjectSettingsForm.NOT_EXISTS, profilesModel, comboBox);
			} else if (file.isDirectory()) {
				resolveDirectory(projectSettingsForm, profilesModel, file, comboBox);
			} else if (lowerCaseName.endsWith(".prefs")) {
				processPrefs(projectSettingsForm, profilesModel, comboBox, file);
			} else if (lowerCaseName.endsWith(".epf")) {
				processEPF(projectSettingsForm, profilesModel, file, comboBox);
			} else if (lowerCaseName.endsWith(".xml")) {
				processXml(profilesModel, file, comboBox);
			} else {
				//lets assume it is properties
				processPrefs(projectSettingsForm, profilesModel, comboBox, file);
			}
		} catch (IOException e) {
			invalid("Plugin error:" + e.toString(), profilesModel, comboBox);
			throw new RuntimeException(e);
		}

	}

	private void processEPF(ProjectSettingsForm projectSettingsForm, SortedComboBoxModel profilesModel, File file, JComboBox comboBox) {
		Properties properties = FileUtils.readPropertiesFile(file);
		Properties result = FileUtils.convertEPF(properties, new Properties());
		if (result.size() > 100) {
			valid("valid config", projectSettingsForm, profilesModel, comboBox);
		} else {
			invalid("Invalid config, should contain 100+ org.eclipse.jdt.core properties", profilesModel, comboBox);
		}
	}

	private void resolveDirectory(ProjectSettingsForm projectSettingsForm, SortedComboBoxModel profilesModel, File file, JComboBox comboBox) throws IOException {
		File uiPrefs = org.apache.commons.io.FileUtils.getFile(file, ".metadata", ".plugins", "org.eclipse.core.runtime", ".settings", "org.eclipse.jdt.ui.prefs");
		File corePrefs = org.apache.commons.io.FileUtils.getFile(file, ".settings", "org.eclipse.jdt.core.prefs");

		if (uiPrefs != null && uiPrefs.exists()) {
			String s1 = org.apache.commons.io.FileUtils.readFileToString(uiPrefs, "UTF-8");
			Properties formatterOptions = new Properties();
			formatterOptions.load(new StringReader(s1));

			String formatter_profile1 = formatterOptions.getProperty("formatter_profile");
			String xml = formatterOptions.getProperty("org.eclipse.jdt.ui.formatterprofiles");

			List<String> profileNamesFromConfigXML = FileUtils.getProfileNamesFromConfigXML(IOUtils.toInputStream(xml));
			if (profileNamesFromConfigXML.isEmpty()) {
				invalid("Workspace does not contain custom formatter profiles!", profilesModel, comboBox);
			} else {
				profilesModel.addAll(profileNamesFromConfigXML);

				String substring = formatter_profile1.substring(1);
				if (new HashSet<>(profileNamesFromConfigXML).contains(substring)) {
					profilesModel.setSelectedItem(substring);
				}
			}
		} else if (corePrefs != null && corePrefs.exists()) {
			processPrefs(projectSettingsForm, profilesModel, comboBox, corePrefs);
		} else {
			invalid("invalid location", profilesModel, comboBox);
		}
	}

	private void processXml(SortedComboBoxModel profilesModel, File file, JComboBox comboBox) {
		try {
			profilesModel.addAll(FileUtils.getProfileNamesFromConfigXML(file));
			if (profilesModel.getSize() == 0) {
				invalid(ProjectSettingsForm.CONTAINS_NO_PROFILES, profilesModel, comboBox);
			}
		} catch (ParsingFailedException e) {
			invalid(ProjectSettingsForm.PARSING_FAILED, profilesModel, comboBox);
		}
	}

	private void processPrefs(ProjectSettingsForm projectSettingsForm, SortedComboBoxModel profilesModel, JComboBox comboBox, File file) {
		Properties properties = FileUtils.readPropertiesFile(file);
		if (properties.size() > 100) {
			valid("valid config", projectSettingsForm, profilesModel, comboBox);
		} else {
			invalid("Enable 'Project Specific Settings' in Eclipse!", profilesModel, comboBox);
		}
	}

	private void valid(String valid_config, ProjectSettingsForm projectSettingsForm, SortedComboBoxModel profilesModel, JComboBox comboBox) {
		profilesModel.add(valid_config);
		comboBox.setEnabled(false);
		comboBox.setBorder(projectSettingsForm.normalBorder);
	}

	private void invalid(String text, SortedComboBoxModel profilesModel, JComboBox comboBox) {
		profilesModel.add(text);
		comboBox.setEnabled(false);
		comboBox.setBorder(ProjectSettingsForm.ERROR_BORDER);
	}


}
