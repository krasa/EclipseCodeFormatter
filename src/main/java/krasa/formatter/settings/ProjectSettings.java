package krasa.formatter.settings;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.project.Project;
import com.intellij.util.xmlb.XmlSerializerUtil;
import com.intellij.util.xmlb.annotations.Transient;
import krasa.formatter.plugin.Notifier;
import org.jetbrains.annotations.NotNull;

@State(name = "EclipseCodeFormatterProjectSettings", storages = {@Storage("eclipseCodeFormatter.xml")})
public class ProjectSettings implements PersistentStateComponent<ProjectSettings> {

    private ProjectSpecificProfile projectSpecificProfile = new ProjectSpecificProfile();
    private Settings selectedGlobalProfile;
    private GlobalProfileReference selectedGlobalProfileReference;
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
        if (this.selectedGlobalProfile != null && !this.selectedGlobalProfile.isBackupToProjectConfigFile()) {
            ProjectSettings projectSettings = new ProjectSettings();
            projectSettings.projectSpecificProfile = this.projectSpecificProfile;
            projectSettings.selectedGlobalProfileReference = new GlobalProfileReference(this.selectedGlobalProfile.getId(), selectedGlobalProfile.getName());
            return projectSettings;
        } else {
            return this;
        }
    }

    @Override
    public void loadState(ProjectSettings state) {
        XmlSerializerUtil.copyBean(state, this);
    }

    public static ProjectSettings getInstance(Project project) {
        return project.getService(ProjectSettings.class);
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
            this.setSelectedGlobalProfileReference(null);
        } else {
            this.setSelectedGlobalProfile(profile);
            this.setSelectedGlobalProfileReference(null);
        }
    }

    public void globalProfileUpdated(Settings updatedGlobalProfile) {
        final Settings.Formatter formatter = getSelectedProfile().getFormatter();
        setProfile(GlobalSettings.clone(updatedGlobalProfile));
        getSelectedProfile().setFormatter(formatter);
    }

    public void projectOpened() {
        syncGlobalProfile();
        GlobalSettings.getInstance().migrateSettings(this);
    }

    private void syncGlobalProfile() {
        Settings selectedGlobalProfile = this.selectedGlobalProfile;
        if (selectedGlobalProfile != null) {
            Settings.Formatter formatter = selectedGlobalProfile.getFormatter();
            Settings clone = null;
            try {
                Settings globalProfile = GlobalSettings.getInstance().findGlobalProfile(selectedGlobalProfile, project);
                clone = GlobalSettings.clone(globalProfile);
                clone.setFormatter(formatter);
            } catch (DeletedProfileException e) {
                Notifier.notifyDeletedSettings(project);
            }
            this.selectedGlobalProfile = clone;
        } else if (selectedGlobalProfileReference != null) {
            try {
                Settings globalProfile = GlobalSettings.getInstance().findGlobalProfile(selectedGlobalProfileReference, project);
                if (globalProfile != null) {
                    this.selectedGlobalProfile = GlobalSettings.clone(globalProfile);
                } else {
                    Notifier.notifyProfileDoesNotExist(project);
                }
            } catch (DeletedProfileException e) {
                Notifier.notifyDeletedSettings(project);
            }
        }
    }

    public GlobalProfileReference getSelectedGlobalProfileReference() {
        return selectedGlobalProfileReference;
    }

    public void setSelectedGlobalProfileReference(GlobalProfileReference selectedGlobalProfileReference) {
        this.selectedGlobalProfileReference = selectedGlobalProfileReference;
    }
}
