/*
 * External Code Formatter Copyright (c) 2007-2009 Esko Luontola, www.orfjackal.net Licensed under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for
 * the specific language governing permissions and limitations under the License.
 */

package krasa.formatter.plugin;

import krasa.formatter.settings.Settings;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.picocontainer.MutablePicoContainer;

import com.intellij.codeInsight.actions.LayoutCodeConstants;
import com.intellij.ide.util.PropertiesComponent;
import com.intellij.openapi.actionSystem.Shortcut;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.keymap.Keymap;
import com.intellij.openapi.keymap.impl.KeymapManagerImpl;
import com.intellij.openapi.project.Project;
import com.intellij.psi.codeStyle.CodeStyleManager;

/**
 * Switches a project's {@link CodeStyleManager} to a eclipse formatter and back.
 * 
 * @author Esko Luontola
 * @author Vojtech Krasa
 * @since 2.12.2007
 */
public class ProjectCodeStyleInstaller {

	private static final String CODE_STYLE_MANAGER_KEY = CodeStyleManager.class.getName();
	private static final Logger LOG = Logger.getInstance(ProjectCodeStyleInstaller.class.getName());

	public static final String OPTIMIZE_IMPORTS_OLD_PLUGIN = "EclipseFormatterPluginOptimizeImportsAction";
	public static final String OPTIMIZE_IMPORTS_PLUGIN = "EclipseFormatterPluginOptimizeImports";
	public static final String OPTIMIZE_IMPORTS = "OptimizeImports";

	@NotNull
	private final Project project;

	public ProjectCodeStyleInstaller(@NotNull Project project) {
		this.project = project;
	}

	@NotNull
	public Project getProject() {
		return project;
	}

	public void changeFormatterTo(@Nullable Settings settings) {
		uninstallCodeFormatter();
		if (settings != null) {
			installCodeFormatter(settings);
		}
	}

	private void installCodeFormatter(@NotNull Settings settings) {
		CodeStyleManager manager = CodeStyleManager.getInstance(project);
		if (Settings.Formatter.ECLIPSE.equals(settings.getFormatter())) {
			registerCodeStyleManager(project, new EclipseCodeStyleManager(manager, settings, project));

			if (settings.isOptimizeImports()) {
				PropertiesComponent.getInstance().setValue(LayoutCodeConstants.OPTIMIZE_IMPORTS_KEY,
						Boolean.toString(false));
			}
			Keymap keyMap = getKeyMap();
			installShortcut(keyMap, OPTIMIZE_IMPORTS);
			installShortcut(keyMap, OPTIMIZE_IMPORTS_OLD_PLUGIN);
		}

	}

	private void installShortcut(Keymap keyMap, String current) {
		Shortcut[] shortcuts = keyMap.getShortcuts(current);
		for (Shortcut shortcut : shortcuts) {
			keyMap.removeShortcut(current, shortcut);
			keyMap.addShortcut(OPTIMIZE_IMPORTS_PLUGIN, shortcut);
		}
	}

	private Keymap getKeyMap() {
		Keymap keymap = KeymapManagerImpl.getInstance().getActiveKeymap().getParent();
		if (keymap == null) {
			keymap = KeymapManagerImpl.getInstance().getActiveKeymap();
		}
		return keymap;
	}

	private void uninstallCodeFormatter() {
		CodeStyleManager manager = CodeStyleManager.getInstance(project);
		while (manager instanceof EclipseCodeStyleManager) {
			manager = ((EclipseCodeStyleManager) manager).getOriginal();
			registerCodeStyleManager(project, manager);
			Keymap parent = getKeyMap();
			uninstallShortcut(parent, OPTIMIZE_IMPORTS_OLD_PLUGIN);
			uninstallShortcut(parent, OPTIMIZE_IMPORTS_PLUGIN);
		}

	}

	private void uninstallShortcut(Keymap parent, String current) {
		Shortcut[] shortcuts = parent.getShortcuts(current);
		for (Shortcut shortcut : shortcuts) {
			parent.removeShortcut(current, shortcut);
			parent.addShortcut(OPTIMIZE_IMPORTS, shortcut);
		}
	}

	private static void registerCodeStyleManager(@NotNull Project project, @NotNull CodeStyleManager manager) {
		LOG.info("Registering code style manager '" + manager + "' for project '" + project.getName() + "'");
		MutablePicoContainer container = (MutablePicoContainer) project.getPicoContainer();
		container.unregisterComponent(CODE_STYLE_MANAGER_KEY);
		container.registerComponentInstance(CODE_STYLE_MANAGER_KEY, manager);
	}

}
