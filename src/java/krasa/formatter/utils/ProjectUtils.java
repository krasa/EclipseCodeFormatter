package krasa.formatter.utils;

import krasa.formatter.plugin.Notifier;
import krasa.formatter.settings.ProjectSettingsComponent;
import krasa.formatter.settings.Settings;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.impl.ProjectManagerImpl;

/**
 * @author Vojtech Krasa
 */
public class ProjectUtils {

	public static void notifyProjectsWhichUsesThisSettings(Settings deletedSettings, Project project,
			Settings defaultSettings) {
		Project[] openProjects = ProjectManagerImpl.getInstance().getOpenProjects();
		for (Project openProject : openProjects) {
			ProjectSettingsComponent component = openProject.getComponent(ProjectSettingsComponent.class);
			if (component != null) {
				Settings state = component.getSettings();
				if (deletedSettings.getId().equals(state.getId())) {
					component.loadState(defaultSettings);
					if (project != openProject) {
						Notifier.notifyDeletedSettings(component.getProject());
					}
				}
			}
		}
	}

	public static void applyToAllOpenedProjects(Settings updatedSettings) {
		Project[] openProjects = ProjectManagerImpl.getInstance().getOpenProjects();
		for (Project openProject : openProjects) {
			ProjectSettingsComponent component = openProject.getComponent(ProjectSettingsComponent.class);
			if (component != null) {
				Settings state = component.getSettings();
				if (updatedSettings.getId().equals(state.getId())) {
					component.install(updatedSettings);
				}
			}
		}
	}
}
