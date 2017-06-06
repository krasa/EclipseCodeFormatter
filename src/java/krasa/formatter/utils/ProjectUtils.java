package krasa.formatter.utils;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.impl.ProjectManagerImpl;

import krasa.formatter.plugin.Notifier;
import krasa.formatter.settings.ProjectComponent;
import krasa.formatter.settings.Settings;

/**
 * @author Vojtech Krasa
 */
public class ProjectUtils {

	public static void notifyProjectsWhichUsesThisSettings(Settings deletedSettings, Project project) {
		Project[] openProjects = ProjectManagerImpl.getInstance().getOpenProjects();
		for (Project openProject : openProjects) {
			ProjectComponent component = openProject.getComponent(ProjectComponent.class);
			if (component != null) {
				Settings state = component.getSelectedProfile();
				if (deletedSettings.getId().equals(state.getId())) {
					component.getProjectSettings().getState().setSelectedGlobalProfile(null);
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
			ProjectComponent component = openProject.getComponent(ProjectComponent.class);
			if (component != null) {
				Settings state = component.getSelectedProfile();
				if (updatedSettings.getId().equals(state.getId())) {
					component.globalProfileUpdated(updatedSettings);
				}
			}
		}
	}
}
