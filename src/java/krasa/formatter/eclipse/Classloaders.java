package krasa.formatter.eclipse;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.PathManager;
import com.intellij.openapi.diagnostic.Logger;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;

public class Classloaders {
	private static final Logger LOG = Logger.getInstance(Classloaders.class.getName());

	private static ClassLoader eclipse44;
	private static ClassLoader newEclipse;

	public static ClassLoader getEclipse44() {
		if (eclipse44 == null) {
			eclipse44 = classLoader(getPluginLibHomeEclipse44(),
					"org.eclipse.jdt.core_3.10.0.v20140902-0626.jar",
					"org.eclipse.core.contenttype_3.4.200.v20140207-1251.jar",
					"org.eclipse.core.jobs_3.6.0.v20140424-0053.jar",
					"org.eclipse.core.runtime_3.10.0.v20140318-2214.jar",
					"org.eclipse.equinox.registry_3.5.400.v20140428-1507.jar",//js formatter dependency
					"JsniUtils.jar",
					"com.google.gwt.eclipse.core_2.6.1.v201205091048-rel-r36.zip",
					"gwt-dev.jar",
					"org.eclipse.wst.jsdt.core_1.3.200.v201409111605.jar");
		}
		return eclipse44;
	}

	public static ClassLoader getNewEclipse() {
		if (newEclipse == null) {
			newEclipse = classLoader(getPluginLibsHome(),
					"org.eclipse.jdt.core_3.11.0.v20150602-1242.jar",
					"org.eclipse.core.contenttype_3.5.0.v20150421-2214.jar",
					"org.eclipse.core.jobs_3.7.0.v20150330-2103.jar",
					"org.eclipse.core.runtime_3.11.0.v20150405-1723.jar"

			);
		}
		return newEclipse;
	}

	@NotNull
	private static File getPluginLibsHome() {
		File pluginHome;
		if (ApplicationManager.getApplication().isUnitTestMode()) {
			pluginHome = new File("./lib/");
		} else {
			pluginHome = new File(PathManager.getPluginsPath(), "EclipseFormatter/lib/");
		}
		return pluginHome;
	}

	@NotNull
	private static File getPluginLibHomeEclipse44() {
		File pluginHome;
		if (ApplicationManager.getApplication() == null || ApplicationManager.getApplication().isUnitTestMode()) {
			pluginHome = new File("./lib/eclipse44/");
		} else {
			pluginHome = new File(PathManager.getPluginsPath(), "EclipseFormatter/lib/");
		}
		return pluginHome;
	}

	@NotNull
	private static ClassLoader classLoader(File parent, String... jarFiles) {
		URL[] urls = new URL[jarFiles.length];
		for (int i = 0; i < jarFiles.length; i++) {
			String jar = jarFiles[i];
			File jarFile = new File(parent, jar);
			if (!jarFile.exists()) {
				throw new IllegalStateException("Plugin jar file not found: " + jarFile.getAbsolutePath());
			}
			try {
				urls[i] = jarFile.toURI().toURL();
			} catch (MalformedURLException e) {
				throw new RuntimeException(e);
			}
		}
		try {
			// return UrlClassLoader.classLoader().urls(jarFile).useCache().get();
			LOG.info("Creating classloader for " + Arrays.toString(urls));
			return new ParentLastURLClassLoader(Classloaders.class.getClassLoader(), urls);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

}
