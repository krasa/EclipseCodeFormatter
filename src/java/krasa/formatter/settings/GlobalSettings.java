package krasa.formatter.settings;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.beanutils.BeanUtils;
import org.jetbrains.annotations.NotNull;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.PathManager;
import com.intellij.openapi.components.*;
import com.intellij.openapi.project.Project;
import com.intellij.util.xmlb.XmlSerializerUtil;

import krasa.formatter.plugin.Notifier;
import krasa.formatter.utils.ProjectUtils;
import krasa.formatter.utils.StringUtils;

/**
 * @author Vojtech Krasa
 */
@State(name = "EclipseCodeFormatterSettings", storages = {
		@Storage(id = "EclipseCodeFormatterSettings", file = "$APP_CONFIG$/eclipseCodeFormatter.xml") })
public class GlobalSettings
		implements ApplicationComponent, PersistentStateComponent<GlobalSettings>, ExportableApplicationComponent {
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
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		return newSettings;
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
		ProjectUtils.notifyProjectsWhichUsesThisSettings(settings, project);
	}

}
