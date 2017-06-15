package krasa.formatter.settings;

import com.intellij.openapi.components.*;
import com.intellij.openapi.project.Project;
import com.intellij.util.xmlb.XmlSerializerUtil;
import com.intellij.util.xmlb.annotations.Transient;
import krasa.formatter.plugin.Notifier;
import org.jetbrains.annotations.NotNull;

@State(name = "EclipseCodeFormatterProjectSettings", storages = {
		@Storage(file = StoragePathMacros.PROJECT_FILE),
		@Storage(file = StoragePathMacros.PROJECT_CONFIG_DIR + "/eclipseCodeFormatter.xml", scheme = StorageScheme.DIRECTORY_BASED)})
public class ProjectSettings implements PersistentStateComponent<ProjectSettings> {

	private ProjectSpecificProfile projectSpecificProfile = new ProjectSpecificProfile();
	private Settings selectedGlobalProfile;
	@Transient
	private transient Project project;

	public ProjectSettings() {
	}

	public ProjectSettings(@NotNull Project project) {
		this.project = project;
	}

	@NotNull
	public ProjectSpecificProfile getProjectSpecificProfile() {
		return projectSpecificProfile;
	}

	public void setProjectSpecificProfile(ProjectSpecificProfile projectSpecificProfile) {
		this.projectSpecificProfile = projectSpecificProfile;
	}

	public Settings getSelectedGlobalProfile() {
		return selectedGlobalProfile;
	}

	public void setSelectedGlobalProfile(Settings selectedGlobalProfile) {
		this.selectedGlobalProfile = selectedGlobalProfile;
	}

	@Override
	@NotNull
	public ProjectSettings getState() {
		return this;
	}

	@Override
	public void loadState(ProjectSettings state) {
		XmlSerializerUtil.copyBean(state, this);
	}

	public static ProjectSettings getInstance(Project project) {
		return ServiceManager.getService(project, ProjectSettings.class);
	}

	public Settings getSelectedProfile() {
		Settings selectedGlobalProfile = getSelectedGlobalProfile();
		if (selectedGlobalProfile != null) {
			return selectedGlobalProfile;
		}
		return getProjectSpecificProfile();
	}

	public void setProfile(Settings profile) {
		if (profile.isProjectSpecific()) {
			this.setProjectSpecificProfile((ProjectSpecificProfile) profile);
			this.setSelectedGlobalProfile(null);
		} else {
			this.setSelectedGlobalProfile(profile);
		}
	}

	public void globalProfileUpdated(Settings updatedGlobalProfile) {
		final Settings.Formatter formatter = getSelectedProfile().getFormatter();
		setProfile(GlobalSettings.clone(updatedGlobalProfile));
		getSelectedProfile().setFormatter(formatter);
	}

	public void projectOpened() {
		Settings selectedProfile = ServiceManager.getService(project,
				ProjectPersistentStateComponent_Old.class).migrateSettings();
		if (selectedProfile != null) {
			this.setSelectedGlobalProfile(selectedProfile);
		}
		syncGlobalProfile();
	}

	private void syncGlobalProfile() {
		Settings selectedGlobalProfile = getSelectedGlobalProfile();
		if (selectedGlobalProfile != null) {
			Settings.Formatter formatter = selectedGlobalProfile.getFormatter();
			Settings clone = null;
			try {
				clone = GlobalSettings.clone(
						GlobalSettings.getInstance().getGlobalProfile(selectedGlobalProfile, project));
				clone.setFormatter(formatter);
			} catch (DeletedProfileException e) {
				Notifier.notifyDeletedSettings(project);
			}
			setSelectedGlobalProfile(clone);
		}
	}
}
