package krasa.formatter.settings;

import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.project.Project;
import krasa.formatter.Messages;
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

    private ProjectPersistentStateComponent stateComponent;
    private Project project;
    @Nullable
    private ProjectSettingsForm form;

    public MyConfigurable(ProjectPersistentStateComponent stateComponent, Project project) {
        this.stateComponent = stateComponent;
        this.project = project;
        if (project.isDefault()) {
            stateComponent.loadState(stateComponent.getSettings());
        }
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
        return form != null && (form.isModified(stateComponent.getSettings()) || (form.getDisplayedSettings() != null && !isSameId()));
    }

    private boolean isSameId() {
        return ObjectUtils.equals(form.getDisplayedSettings().getId(), stateComponent.getSettings().getId());
    }

    @Override
    public void apply() throws ConfigurationException {
        if (form != null) {
            form.validate();
            stateComponent.setState(form.exportDisplayedSettings());
            GlobalSettings.getInstance().updateSettings(stateComponent.getSettings(), project);
            ProjectUtils.applyToAllOpenedProjects(stateComponent.getSettings());
        }
    }

    @Override
    public void reset() {
        if (form != null) {
            form.importFrom(stateComponent.getSettings());
        }
    }

    @Override
    public void disposeUIResources() {
        form = null;
    }
}
