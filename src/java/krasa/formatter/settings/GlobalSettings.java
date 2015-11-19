package krasa.formatter.settings;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import krasa.formatter.plugin.Notifier;
import krasa.formatter.utils.ProjectUtils;
import krasa.formatter.utils.StringUtils;

import org.jetbrains.annotations.NotNull;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.PathManager;
import com.intellij.openapi.components.*;
import com.intellij.openapi.project.Project;
import com.intellij.util.xmlb.XmlSerializerUtil;

/**
 * @author Vojtech Krasa
 */
@State(name = "EclipseCodeFormatterSettings", storages = { @Storage(id = "EclipseCodeFormatterSettings", file = "$APP_CONFIG$/eclipseCodeFormatter.xml") })
public class GlobalSettings implements ApplicationComponent, PersistentStateComponent<GlobalSettings>,
		ExportableApplicationComponent {
	private List<Settings> settingsList = new ArrayList<Settings>();
	private List<Long> deletedSettingsId = new ArrayList<Long>();

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
	}

	public List<Settings> getSettingsList() {
		return settingsList;
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

	public Settings copySettings(Settings settings) {
		Settings newSettings = new Settings();
		XmlSerializerUtil.copyBean(settings, newSettings);
		newSettings.setName(settings.getName() + " copy");
		newSettings.setId(generateId());
		newSettings.setDefaultSettings(false);
		settingsList.add(newSettings);
		return newSettings;
	}

	public Settings clone(Settings settings) {
		Settings newSettings = new Settings();
		XmlSerializerUtil.copyBean(settings, newSettings);
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

	private void addToGlobalSettings(Settings newSettings, Project project) {
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
	public Settings getSettings(@NotNull Settings state, @NotNull Project project) {
		Settings.Formatter formatter = state.getFormatter();
		Settings clone = clone(getSettingsFromGlobal(state, project));
		if (!state.isNotSaved()) {
			clone.setFormatter(formatter);
		}
		return clone;
	}

	private Settings getSettingsFromGlobal(Settings state, Project project) {
		if (state.isNotSaved()) {
			// Settings duplicateSettings = getDuplicateSettings(state);
			if (isSameAsDefault(state)) {
				return getDefaultSettings();
			}
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
				Settings defaultSettings = getDefaultSettings();
				Notifier.notifyDeletedSettings(project);
				return defaultSettings;
			}
			addToGlobalSettings(state, project);
			return state;
		}
	}

	private boolean isSameAsDefault(Settings state) {
		return new Settings().equalsContent(state) || getDefaultSettings().equalsContent(state);
	}

	public Settings getDefaultSettings() {
		for (Settings settings : settingsList) {
			if (settings.isDefaultSettings()) {
				return settings;
			}
		}
		Settings aDefault = createDefaultSettings();
		settingsList.add(aDefault);
		return aDefault;
	}

	private Settings createDefaultSettings() {
		String name = StringUtils.generateName(settingsList, 1, "default");
		Settings aDefault = new Settings(generateId(), name);
		aDefault.setDefaultSettings(true);
		return aDefault;
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

	@NotNull
	@Override
	public File[] getExportFiles() {
		return new File[] { PathManager.getOptionsFile("eclipseCodeFormatter") };
	}

	@NotNull
	@Override
	public String getPresentableName() {
		return "Eclipse Code Formatter";
	}

	public void delete(Settings settings, Project project) {
		settingsList.remove(settings);
		deletedSettingsId.add(settings.getId());
		Settings defaultSettings = getDefaultSettings();// to create default setting when it was deleted
		ProjectUtils.notifyProjectsWhichUsesThisSettings(settings, project, defaultSettings);
	}

}
