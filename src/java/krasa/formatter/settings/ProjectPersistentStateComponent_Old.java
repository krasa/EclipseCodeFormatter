package krasa.formatter.settings;

import org.jetbrains.annotations.Nullable;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;

@Deprecated
@State(name = "EclipseCodeFormatter", storages = { @Storage(id = "other", file = "$PROJECT_FILE$") })
public class ProjectPersistentStateComponent_Old implements PersistentStateComponent<Settings> {

	protected Settings settings;

	@Override
	@Nullable
	public Settings getState() {
		return settings;
	}

	@Override
	public void loadState(Settings state) {
		settings = state;
	}

	public Settings migrateSettings() {
		if (settings != null) {
			Settings settings = this.settings;
			this.settings = new Settings();
			return settings;
		}
		return null;
	}

}
