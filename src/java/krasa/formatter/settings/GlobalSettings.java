package krasa.formatter.settings;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.ApplicationComponent;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.project.Project;
import com.intellij.util.xmlb.XmlSerializerUtil;
import krasa.formatter.plugin.Notifier;
import krasa.formatter.utils.ProjectUtils;
import krasa.formatter.utils.StringUtils;
import org.apache.commons.beanutils.BeanUtils;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.apache.commons.lang3.StringUtils.isBlank;

/**
 * @author Vojtech Krasa
 */
@State(name = "EclipseCodeFormatterSettings", storages = {@Storage("eclipseCodeFormatter.xml")})
public class GlobalSettings
		implements ApplicationComponent, PersistentStateComponent<GlobalSettings> {
	private List<Settings> settingsList = new ArrayList<Settings>();
	private List<Long> deletedSettingsId = new ArrayList<Long>();
	private String pathToEclipse = "";

	public static GlobalSettings getInstance() {
		return ApplicationManager.getApplication().getComponent(GlobalSettings.class);
	}

	@Override
	public GlobalSettings getState() {
		return this;
	}

	@Override
	public void loadState(GlobalSettings state) {
		XmlSerializerUtil.copyBean(state, this);

		migrateSettings();
	}


	public List<Settings> getSettingsList() {
		return settingsList;
	}

	public String getPathToEclipse() {
		return pathToEclipse;
	}

	public void setPathToEclipse(String pathToEclipse) {
		this.pathToEclipse = pathToEclipse;
	}

	public void setSettingsList(List<Settings> settingsList) {
		this.settingsList = settingsList;
	}

	public Settings newSettings() {
		String name = StringUtils.generateName(settingsList, 1, "new");
		Settings aNew = new Settings(generateId(), name);
		settingsList.add(aNew);
		return aNew;
	}

	public Settings copySettings(Project project, Settings settings) {
		Settings newSettings = clone(settings);
		if (settings.isProjectSpecific()) {
			newSettings.setName(StringUtils.generateName(settingsList, 1, project.getName(), project.getName()));
		} else {
			newSettings.setName(settings.getName() + " copy");
		}
		newSettings.setId(generateId());
		settingsList.add(newSettings);
		return newSettings;
	}

	public static Settings clone(Settings settings) {
		Settings newSettings = new Settings();
		try {
			BeanUtils.copyProperties(newSettings, settings);
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
		return newSettings;
	}

	public void updateSettings(Settings settings, Project project) {
		if (settings.getId() == null) {
			addToGlobalSettings(settings, project);
		} else {
			for (Settings settings1 : settingsList) {
				if (settings1.getId().equals(settings.getId())) {
					XmlSerializerUtil.copyBean(settings, settings1);
				}
			}
		}
	}

	private void addToGlobalSettings(@NotNull Settings newSettings, @NotNull Project project) {
		if (newSettings.getId() == null) {
			newSettings.setId(generateId());
		}
		if (newSettings.getName() == null) {
			String name = StringUtils.generateName(settingsList, 1, project.getName(), project.getName());
			newSettings.setName(name);
		}
		settingsList.add(newSettings);
	}

	private Long generateId() {
		long newId = new Date().getTime();
		for (Settings settings : settingsList) {
			if (settings.getId().equals(newId)) {
				newId = generateId();
			}
		}
		return newId;
	}

	@NotNull
	public Settings getGlobalProfile(@NotNull Settings state, @NotNull Project project) throws DeletedProfileException {
		if (state.isNotSaved()) {
			addToGlobalSettings(state, project);
			return state;
		} else {
			for (Settings settings : settingsList) {
				if (settings.getId().equals(state.getId())) {
					return settings;
				}
			}
			for (Settings settings : settingsList) {
				if (settings.getName().equals(state.getName())) {
					return settings;
				}
			}
			if (deletedSettingsId.contains(state.getId())) {
				Notifier.notifyDeletedSettings(project);
				throw new DeletedProfileException();
			}
			addToGlobalSettings(state, project);
			return state;
		}
	}

	@Override
	public void initComponent() {
	}

	@Override
	public void disposeComponent() {
	}

	@NotNull
	@Override
	public String getComponentName() {
		return "EclipseCodeFormatterGlobalSettings";
	}


	public void delete(Settings settings, Project project) {
		settingsList.remove(settings);
		deletedSettingsId.add(settings.getId());
		ProjectUtils.notifyProjectsWhichUsesThisSettings(settings, project);
	}

	private void migrateSettings() {
		if (isBlank(pathToEclipse)) {
			for (Settings settings : settingsList) {
				pathToEclipse = settings.getPathToEclipse();
				if (!isBlank(pathToEclipse)) {
					break;
				}
			}
		}

		for (Settings settings : settingsList) {
			settings.setPathToEclipse("");
		}

	}

	public void migrateSettings(ProjectSettings projectSettings) {
		if (isBlank(pathToEclipse)) {
			Settings selectedProfile = projectSettings.getSelectedProfile();
			pathToEclipse = selectedProfile.getPathToEclipse();
		}

		Settings selectedGlobalProfile = projectSettings.getSelectedGlobalProfile();
		if (selectedGlobalProfile != null) {
			selectedGlobalProfile.setPathToEclipse("");
		}

		ProjectSpecificProfile projectSpecificProfile = projectSettings.getProjectSpecificProfile();
		projectSpecificProfile.setPathToEclipse("");
	}
}
