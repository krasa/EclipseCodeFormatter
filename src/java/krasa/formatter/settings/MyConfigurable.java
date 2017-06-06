package krasa.formatter.settings;

import javax.swing.*;

import org.apache.commons.lang.ObjectUtils;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.project.Project;

import krasa.formatter.Messages;
import krasa.formatter.plugin.ProjectSettingsForm;
import krasa.formatter.utils.ProjectUtils;

// implements Configurable
public class MyConfigurable implements Configurable {

	private final ProjectSettings projectSettings;
	private Project project;
	@Nullable
	private ProjectSettingsForm form;

	public MyConfigurable(ProjectSettings projectSettings, Project project) {
		this.projectSettings = projectSettings;
		this.project = project;
	}

	@Override
	@Nls
	public String getDisplayName() {
		return Messages.message("action.pluginSettings");
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
		return form != null && (form.isModified(projectSettings.getSelectedProfile())
				|| (form.getDisplayedSettings() != null && !isSameId()));
	}

	private boolean isSameId() {
		return ObjectUtils.equals(form.getDisplayedSettings().getId(), projectSettings.getSelectedProfile().getId());
	}

	@Override
	public void apply() throws ConfigurationException {
		if (form != null) {
			form.validate();
			Settings profile = form.exportDisplayedSettings();

			projectSettings.setProfile(profile);

			if (!project.isDefault()) {
				if (profile.isProjectSpecific()) {
					ProjectComponent.getInstance(project).installOrUpdate(profile);
				} else {
					ProjectUtils.applyToAllOpenedProjects(profile);
				}
			}
		}
	}

	@Override
	public void reset() {
		if (form != null) {
			form.importFrom(projectSettings.getSelectedProfile());
		}
	}

	@Override
	public void disposeUIResources() {
		form = null;
	}
}
