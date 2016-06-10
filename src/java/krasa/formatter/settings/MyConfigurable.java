package krasa.formatter.settings;

import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.project.Project;
import krasa.formatter.Messages;
import krasa.formatter.Resources;
import krasa.formatter.plugin.ProjectSettingsForm;
import krasa.formatter.utils.ProjectUtils;
import org.apache.commons.lang.ObjectUtils;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

// implements Configurable
public class MyConfigurable implements Configurable {

    private ProjectSettingsComponent projectSettingsComponent;
    @Nullable
    private ProjectSettingsForm form;

    public MyConfigurable(@NotNull Project project) {
        this.projectSettingsComponent = ProjectSettingsComponent.getInstance(project);
    }

    @Override
    @Nls
    public String getDisplayName() {
        return Messages.message("action.pluginSettings");
    }

    @Nullable
    public Icon getIcon() {
        if (projectSettingsComponent.icon == null) {
            projectSettingsComponent.icon = new ImageIcon(Resources.PROGRAM_LOGO_32);
        }
        return projectSettingsComponent.icon;
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
            form = new ProjectSettingsForm(projectSettingsComponent.project, this);
        }
        return form.getRootComponent();
    }

    @Override
    public boolean isModified() {
        return form != null && (form.isModified(projectSettingsComponent.settings) || (form.getDisplayedSettings() != null && !isSameId()));
    }

    private boolean isSameId() {
        return ObjectUtils.equals(form.getDisplayedSettings().getId(), projectSettingsComponent.settings.getId());
    }

    @Override
    public void apply() throws ConfigurationException {
        if (form != null) {
            form.validate();
            projectSettingsComponent.settings = form.exportDisplayedSettings();
            GlobalSettings.getInstance().updateSettings(projectSettingsComponent.settings, projectSettingsComponent.project);
            ProjectUtils.applyToAllOpenedProjects(projectSettingsComponent.settings);
        }
    }

    @Override
    public void reset() {
        if (form != null) {
            form.importFrom(projectSettingsComponent.settings);
        }
    }

    @Override
    public void disposeUIResources() {
        form = null;
    }
}
