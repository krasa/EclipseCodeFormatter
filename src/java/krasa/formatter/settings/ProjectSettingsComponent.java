/*
 * External Code Formatter Copyright (c) 2007-2009 Esko Luontola, www.orfjackal.net Licensed under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for
 * the specific language governing permissions and limitations under the License.
 */

package krasa.formatter.settings;

import javax.swing.*;

import krasa.formatter.Messages;
import krasa.formatter.Resources;
import krasa.formatter.plugin.ProjectCodeStyleInstaller;
import krasa.formatter.plugin.ProjectSettingsForm;
import krasa.formatter.utils.ProjectUtils;

import org.apache.commons.lang.ObjectUtils;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.intellij.notification.NotificationDisplayType;
import com.intellij.notification.NotificationGroup;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.ProjectComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;

/**
 * Takes care of initializing a project's CodeFormatter and disposing of it when the project is closed. Updates the
 * formatter whenever the plugin settings are changed.
 *
 * @author Esko Luontola
 * @since 4.12.2007
 */
@State(name = "EclipseCodeFormatter", storages = { @Storage(id = "other", file = "$PROJECT_FILE$") })
public class ProjectSettingsComponent implements ProjectComponent, PersistentStateComponent<Settings> {

	private static final Logger LOG = Logger.getInstance(ProjectSettingsComponent.class.getName());
	public static final NotificationGroup GROUP_DISPLAY_ID_ERROR = new NotificationGroup("Eclipse code formatter error",
			NotificationDisplayType.BALLOON, true);
	public static final NotificationGroup GROUP_DISPLAY_ID_INFO = new NotificationGroup("Eclipse code formatter info",
			NotificationDisplayType.NONE, true);

	@NotNull
	private final ProjectCodeStyleInstaller projectCodeStyle;
	@NotNull
	private Settings settings = new Settings();
	@Nullable
	private ImageIcon icon;
	@NotNull
	private Project project;

	public ProjectSettingsComponent(@NotNull Project project) {
		this.projectCodeStyle = new ProjectCodeStyleInstaller(project);
		this.project = project;
	}

	public static Settings getSettings(PsiFile psiFile) {
		return getInstance(psiFile.getProject()).getSettings();
	}

	public void install(@NotNull Settings settings) {
		projectCodeStyle.changeFormatterTo(settings);
	}

	private void uninstall() {
		projectCodeStyle.changeFormatterTo(null);
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
		final Settings.Formatter formatter = settings.getFormatter();
		settings = GlobalSettings.getInstance().getSettings(updatedSettings, project);
		settings.setFormatter(formatter);
		install(settings);
	}

	@Override
	public void projectOpened() {
		settings = GlobalSettings.getInstance().getSettings(settings, project);
		install(settings);
	}

	@Override
	public void projectClosed() {
		uninstall();
	}

	// implements Configurable
	public class MyConfigurable implements Configurable {
        
		@Nullable
		private ProjectSettingsForm form;

		@Override
		@Nls
		public String getDisplayName() {
			return Messages.message("action.pluginSettings");
		}

		@Nullable
		public Icon getIcon() {
			if (icon == null) {
				icon = new ImageIcon(Resources.PROGRAM_LOGO_32);
			}
			return icon;
		}

		@Override
		@Nullable
		@NonNls
		public String getHelpTopic() {
			return "EclipseCodeFormatter.Configuration";
		}

		@Override
		@NotNull
		public JComponent createComponent() {
			if (form == null) {
				form = new ProjectSettingsForm(project, this);
			}
			return form.getRootComponent();
		}

		@Override
		public boolean isModified() {
			return form != null && (form.isModified(settings) || (form.getDisplayedSettings() != null && !isSameId()));
		}

		private boolean isSameId() {
			return ObjectUtils.equals(form.getDisplayedSettings().getId(), settings.getId());
		}

		@Override
		public void apply() throws ConfigurationException {
			if (form != null) {
				form.validate();
				settings = form.exportDisplayedSettings();
				GlobalSettings.getInstance().updateSettings(settings, project);
				ProjectUtils.applyToAllOpenedProjects(settings);
			}
		}

		@Override
		public void reset() {
			if (form != null) {
				form.importFrom(settings);
			}
		}

		@Override
		public void disposeUIResources() {
			form = null;
		}
	}

	// implements PersistentStateComponent

	@Override
	@NotNull
	public Settings getState() {
		return settings;
	}

	/**
	 * sets profile for this project
	 */
	@Override
	public void loadState(@NotNull Settings state) {
		settings = state;
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
		return settings;
	}

}
