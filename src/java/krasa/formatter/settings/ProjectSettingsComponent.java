package krasa.formatter.settings;/*
 * External Code Formatter Copyright (c) 2007-2009 Esko Luontola, www.orfjackal.net Licensed under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for
 * the specific language governing permissions and limitations under the License.
 */


import com.intellij.notification.NotificationDisplayType;
import com.intellij.notification.NotificationGroup;
import com.intellij.openapi.components.ProjectComponent;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;
import krasa.formatter.plugin.EclipseCodeStyleManager;
import krasa.formatter.plugin.ProjectCodeStyleInstaller;
import org.jetbrains.annotations.NotNull;

/**
 * Takes care of initializing a project's CodeFormatter and disposing of it when the project is closed. Updates the
 * formatter whenever the plugin stateComponent.getState() are changed.
 *
 * @author Esko Luontola
 * @since 4.12.2007
 */
public class ProjectSettingsComponent implements ProjectComponent {

	private static final Logger LOG = Logger.getInstance(ProjectSettingsComponent.class.getName());
	public static final NotificationGroup GROUP_DISPLAY_ID_ERROR = new NotificationGroup("Eclipse code formatter error",
	 NotificationDisplayType.BALLOON, true);
	public static final NotificationGroup GROUP_DISPLAY_ID_INFO = new NotificationGroup("Eclipse code formatter info",
	 NotificationDisplayType.NONE, true);

	@NotNull
	private final ProjectCodeStyleInstaller projectCodeStyle;
	@NotNull
	protected Project project;
	@NotNull
	private ProjectPersistentStateComponent stateComponent;
	private EclipseCodeStyleManager eclipseCodeStyleManager;

	public ProjectSettingsComponent(@NotNull Project project, @NotNull ProjectPersistentStateComponent stateComponent) {
		this.projectCodeStyle = new ProjectCodeStyleInstaller(project);
		this.project = project;
		this.stateComponent = stateComponent;
	}

	public static Settings getSettings(PsiFile psiFile) {
		return getInstance(psiFile.getProject()).getSettings();
	}

	@Override
	public void initComponent() {
	}


	@Override
	public void disposeComponent() {
	}

	@Override
	@NotNull
	public String getComponentName() {
		return "ProjectSettingsComponent";
	}

	public void settingsUpdatedFromOtherProject(Settings updatedSettings) {
		stateComponent.settingsUpdatedFromOtherProject(updatedSettings);
		installOrUpdate(stateComponent.getSettings());
	}

	@Override
	public void projectOpened() {
		installOrUpdate(stateComponent.getSettings());
	}

	public void installOrUpdate(@NotNull Settings settings) {
		if (eclipseCodeStyleManager == null) {
			eclipseCodeStyleManager = projectCodeStyle.install(settings);
		} else {
			eclipseCodeStyleManager.updateSettings(settings);
		}
	}

	@Override
	public void projectClosed() {
	}

	public static ProjectSettingsComponent getInstance(Project project) {
		return project.getComponent(ProjectSettingsComponent.class);
	}

	@NotNull
	public Project getProject() {
		return project;
	}

	@NotNull
	public Settings getSettings() {
		return stateComponent.getSettings();
	}

	public void loadState(Settings defaultSettings) {
		stateComponent.loadState(defaultSettings);
	}
}
