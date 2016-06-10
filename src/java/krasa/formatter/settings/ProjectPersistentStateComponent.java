package krasa.formatter.settings;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

@State(name = "EclipseCodeFormatter", storages = {@Storage(id = "other", file = "$PROJECT_FILE$")})
public class ProjectPersistentStateComponent implements PersistentStateComponent<Settings> {

    @NotNull
    protected Settings settings;
    private Project project;
    public GlobalSettings globalSettings;

    public ProjectPersistentStateComponent(@NotNull Project project) {
        this.project = project;
        this.globalSettings = GlobalSettings.getInstance();
        this.settings = globalSettings.getDefaultSettings();
    }

    @Override
    @NotNull
    public Settings getState() {
        return settings;
    }

    @Override
    public void loadState(@NotNull Settings state) {
        settings = globalSettings.getSettings(state, project);
    }

    public void settingsUpdatedFromOtherProject(Settings updatedSettings) {
        final Settings.Formatter formatter = settings.getFormatter();
        loadState(updatedSettings);
        settings.setFormatter(formatter);
    }

    public void setState(Settings state) {
        this.settings = state;
    }


}
