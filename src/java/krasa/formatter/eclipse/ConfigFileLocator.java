package krasa.formatter.eclipse;

import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.ui.SortedComboBoxModel;
import krasa.formatter.exception.FileDoesNotExistsException;
import krasa.formatter.exception.ParsingFailedException;
import krasa.formatter.plugin.ProjectSettingsForm;
import krasa.formatter.utils.FileUtils;
import org.apache.commons.io.IOUtils;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;

public class ConfigFileLocator {
	private static final Logger LOG = Logger.getInstance(ConfigFileLocator.class.getName());

	private static IModuleResolverStrategy moduleResolver = new DefaultModuleResolverStrategy();
	private VirtualFile mostRecentFormatterFile = null;

	private final List<String> CONVENTIONFILENAMES = Arrays.asList(//
			".settings/org.eclipse.jdt.core.prefs",//
			".settings/mechanic-formatter.epf",//
			"mechanic-formatter.epf" //
	);

	public static ConfigFileLocator getInstance(Project project) {
		return ServiceManager.getService(project, ConfigFileLocator.class);
	}

	public String resolveConfigFilePath(String path) {
		File file = new File(path);

		if (!file.exists()) {
			throw new FileDoesNotExistsException(file);
		}

		if (file.isDirectory()) {
			File resolve = resolve(file);
			if (resolve != null) return resolve.getAbsolutePath();

			throw new FileDoesNotExistsException("Invalid config location: " + path);
		}

		return path;
	}

	public void validate(ProjectSettingsForm projectSettingsForm,
						 SortedComboBoxModel profilesModel, String path) {
		String text = path;
		File file = new File(text);
		String lowerCaseName = file.getName().toLowerCase();
		JComboBox comboBox = projectSettingsForm.javaFormatterProfile;
		comboBox.setEnabled(true);
		comboBox.setBorder(projectSettingsForm.normalBorder);

		try {
			if (!file.exists()) {
				invalid(ProjectSettingsForm.NOT_EXISTS, profilesModel, comboBox);
			}
			if (file.isDirectory()) {
				file = resolve(file);
				if (file == null) {
					invalid("invalid location", profilesModel, comboBox);
				}
			}

			if (lowerCaseName.equals(".org.eclipse.jdt.ui.prefs")) {
				processWorkspaceConfig(profilesModel, comboBox, file);
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

	@Nullable
	private File resolve(File file) {
		File mechanicFormatterEpf = org.apache.commons.io.FileUtils.getFile(file, ".settings", "mechanic-formatter.epf");
		if (mechanicFormatterEpf.exists()) {
			return mechanicFormatterEpf;
		}

		File corePrefs = org.apache.commons.io.FileUtils.getFile(file, ".settings", "org.eclipse.jdt.core.prefs");
		if (corePrefs.exists()) {
			return corePrefs;
		}

		File uiPrefs = org.apache.commons.io.FileUtils.getFile(file, ".metadata", ".plugins", "org.eclipse.core.runtime", ".settings", "org.eclipse.jdt.ui.prefs");
		if (uiPrefs.exists()) {
			return uiPrefs;
		}
		return null;
	}


	private void processWorkspaceConfig(SortedComboBoxModel profilesModel, JComboBox comboBox, File uiPrefs) throws IOException {
		Properties properties = FileUtils.readPropertiesFile(uiPrefs);
		String xml = properties.getProperty("org.eclipse.jdt.ui.formatterprofiles");
		List<String> profileNamesFromConfigXML = FileUtils.getProfileNamesFromConfigXML(IOUtils.toInputStream(xml));


		if (profileNamesFromConfigXML.isEmpty()) {
			invalid("Workspace does not contain custom formatter profiles!", profilesModel, comboBox);
		} else {
			profilesModel.addAll(profileNamesFromConfigXML);

			String formatter_profile1 = properties.getProperty("formatter_profile");
			String substring = formatter_profile1.substring(1);
			if (new HashSet<>(profileNamesFromConfigXML).contains(substring)) {
				profilesModel.setSelectedItem(substring);
			}
		}
	}

	private void processEPF(ProjectSettingsForm projectSettingsForm, SortedComboBoxModel profilesModel, File file, JComboBox comboBox) {
		if (isValidEPF(file)) {
			valid("valid config", projectSettingsForm, profilesModel, comboBox);
		} else {
			invalid("Invalid config, should contain 100+ org.eclipse.jdt.core properties", profilesModel, comboBox);
		}
	}

	private boolean isValidEPF(File file) {
		Properties properties = FileUtils.readPropertiesFile(file);
		Properties result = FileUtils.convertEPF(properties, new Properties());
		return result.size() > 100;
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
		if (isValidCorePrefs(file)) {
			valid("valid config", projectSettingsForm, profilesModel, comboBox);
		} else {
			invalid("Enable 'Project Specific Settings' in Eclipse!", profilesModel, comboBox);
		}
	}

	private boolean isValidCorePrefs(File file) {
		Properties properties = FileUtils.readPropertiesFile(file);
		return properties.size() > 100;
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


	@Nullable
	VirtualFile traverseToFindConfigurationFileByConvention(PsiFile psiFile, Project project) {
		VirtualFile moduleFileDir = getModuleDirForFile(psiFile.getVirtualFile(), project);

		while (moduleFileDir != null) {
			for (String conventionFileName : CONVENTIONFILENAMES) {
				VirtualFile fileByRelativePath = moduleFileDir.findFileByRelativePath(conventionFileName);
				if (fileByRelativePath != null && fileByRelativePath.exists()) {
					return fileByRelativePath;
				}
			}
			moduleFileDir = getNextParentModuleDirectory(moduleFileDir, project);
		}
		return null;
	}

	private VirtualFile getModuleDirForFile(VirtualFile virtualFile, Project project) {
		// delegate to a strategy which can be overriden in unit tests
		return moduleResolver.getModuleDirForFile(virtualFile, project);
	}

	private VirtualFile getNextParentModuleDirectory(VirtualFile currentModuleDir, Project project) {
		//Jump outside the current project
		VirtualFile parent = currentModuleDir.getParent();
		if (parent != null && parent.exists()) {
			//the file/dir outside the project may be within another loaded module
			// NOTE all modules must be loaded for detecting the parent module of the current one
			VirtualFile dirOfParentModule = getModuleDirForFile(parent, project);
			if (dirOfParentModule != null) {
				return dirOfParentModule;
			}
		}
		return null;
	}

	public interface IModuleResolverStrategy {
		VirtualFile getModuleDirForFile(VirtualFile virtualFile, Project project);
	}

	static class DefaultModuleResolverStrategy implements IModuleResolverStrategy {
		@Override
		public VirtualFile getModuleDirForFile(VirtualFile virtualFile, Project project) {
			Module moduleForFile = ModuleUtil.findModuleForFile(virtualFile, project);
			if (moduleForFile != null) {
				VirtualFile moduleFile = moduleForFile.getModuleFile();
				if (moduleFile != null) {
					return moduleFile.getParent();
				}
			}
			return null;
		}
	}

	/**
	 * VisibleForTesting
	 */
	@Deprecated
	public static void TESTING_setModuleResolver(IModuleResolverStrategy otherModuleResolver) {
		moduleResolver = otherModuleResolver;
	}

	/**
	 * VisibleForTesting
	 */
	@Deprecated
	public static IModuleResolverStrategy TESTING_getModuleResolver() {
		return moduleResolver;
	}

	/**
	 * VisibleForTesting
	 */
	@Deprecated
	public VirtualFile TESTING_getMostRecentFormatterFile() {
		return mostRecentFormatterFile;
	}
}
