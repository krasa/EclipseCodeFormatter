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

import com.intellij.openapi.diagnostic.Logger;
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
		String canonicalName = manager.getClass().getCanonicalName();
		if (!canonicalName.startsWith("com.intellij") && !canonicalName.startsWith("krasa")) {
			throw new RuntimeException("CodeStyleManager conflict, another formatter plugin is probably installed: " + canonicalName);
		}
		if (Settings.Formatter.ECLIPSE.equals(settings.getFormatter())) {
			registerCodeStyleManager(project, new EclipseCodeStyleManager(manager, settings));
		}
	}

	private void uninstallCodeFormatter() {
		CodeStyleManager manager = CodeStyleManager.getInstance(project);
		while (manager instanceof EclipseCodeStyleManager) {
			manager = ((EclipseCodeStyleManager) manager).getOriginal();
			registerCodeStyleManager(project, manager);
		}

	}

	/**
	 * Dmitry Jemerov in unrelated discussion: "Trying to replace IDEA's core components with your custom
	 * implementations is something that we consider a very bad idea, and it's pretty much guaranteed to break in future
	 * versions of IntelliJ IDEA. I certainly hope that you won't stomp on any other plugins doing that, because no one
	 * else is doing it. It would be better to find another approach to solving your problem."
	 * 
	 * LoL
	 */
	private static void registerCodeStyleManager(@NotNull Project project, @NotNull CodeStyleManager manager) {
		LOG.info("Registering code style manager '" + manager + "' for project '" + project.getName() + "'");
		MutablePicoContainer container = (MutablePicoContainer) project.getPicoContainer();
		container.unregisterComponent(CODE_STYLE_MANAGER_KEY);
		container.registerComponentInstance(CODE_STYLE_MANAGER_KEY, manager);
	}

}
