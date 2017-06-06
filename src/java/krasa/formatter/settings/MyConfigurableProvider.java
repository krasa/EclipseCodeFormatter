package krasa.formatter.settings;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurableProvider;
import com.intellij.openapi.project.Project;

public class MyConfigurableProvider extends ConfigurableProvider {
    private final Project myProject;

    public MyConfigurableProvider(@NotNull Project project) {
        myProject = project;
    }

    @Nullable
    @Override
    public Configurable createConfigurable() {
		return new MyConfigurable(ServiceManager.getService(myProject, ProjectSettings.class), myProject);
    }


}
