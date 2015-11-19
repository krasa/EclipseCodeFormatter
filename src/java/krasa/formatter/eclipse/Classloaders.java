package krasa.formatter.eclipse;

import java.io.File;
import java.lang.reflect.Constructor;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;

import krasa.formatter.processor.Processor;
import krasa.formatter.settings.Settings;
import krasa.formatter.settings.provider.CppPropertiesProvider;
import krasa.formatter.settings.provider.JSPropertiesProvider;

import org.jetbrains.annotations.NotNull;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.PathManager;
import com.intellij.openapi.diagnostic.Logger;

public class Classloaders {
	private static final Logger LOG = Logger.getInstance(Classloaders.class.getName());

	private static ClassLoader eclipse44;
	private static ClassLoader newEclipse;

	public static ClassLoader getEclipse44() {
		if (eclipse44 == null) {
			eclipse44 = classLoader(getPluginLibHomeEclipse44(),
                    "adapter44.jar",
					"org.eclipse.jdt.core_3.10.0.v20140902-0626.jar",
					"org.eclipse.core.contenttype_3.4.200.v20140207-1251.jar",
					"org.eclipse.core.jobs_3.6.0.v20140424-0053.jar",
					"org.eclipse.core.runtime_3.10.0.v20140318-2214.jar",
					"org.eclipse.equinox.registry_3.5.400.v20140428-1507.jar",//js formatter dependency
					"com.google.gwt.eclipse.core_2.6.1.v201205091048-rel-r36.zip",
					"gwt-dev.jar",
					"org.eclipse.wst.jsdt.core_1.3.200.v201409111605.jar", "org.eclipse.osgi_3.10.1.v20140909-1633.jar",
					"org.eclipse.text_3.5.300.v20130515-1451.jar",
					"org.eclipse.equinox.common_3.6.200.v20130402-1505.jar",
					"org.eclipse.equinox.app_1.3.200.v20130910-1609.jar",
					"org.eclipse.core.resources_3.9.1.v20140825-1431.jar",
					"org.eclipse.core.resources.win32.x86_3.5.100.v20140124-1940.jar");
		}
		return eclipse44;
	}

	public static ClassLoader getEclipse45() {
		if (newEclipse == null) {
			newEclipse = classLoader(getPluginLibHomeEclipse45(), "adapter45.jar",
					"org.eclipse.core.contenttype_3.5.0.v20150421-2214.jar",
					"org.eclipse.core.jobs_3.7.0.v20150330-2103.jar",
					"org.eclipse.core.resources.win32.x86_3.5.100.v20140124-1940.jar",
					"org.eclipse.core.resources_3.10.1.v20150725-1910.jar",
					"org.eclipse.core.runtime_3.11.1.v20150903-1804.jar",
					"org.eclipse.equinox.app_1.3.300.v20150423-1356.jar",
					"org.eclipse.equinox.common_3.7.0.v20150402-1709.jar",
					"org.eclipse.jdt.core_3.11.1.v20150902-1521.jar", "org.eclipse.osgi_3.10.101.v20150820-1432.jar",
					"org.eclipse.text_3.5.400.v20150505-1044.jar",
					"org.eclipse.equinox.preferences_3.5.300.v20150408-1437.jar",
					"org.eclipse.cdt.core_5.10.0.201506070905.jar"
			);
		}
		return newEclipse;
	}

	@NotNull
	private static File getPluginLibHomeEclipse45() {
		File pluginHome;
		if (ApplicationManager.getApplication().isUnitTestMode()) {
			pluginHome = new File("./lib/eclipse45");
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

	@NotNull
	public static CodeFormatterFacade getCppFormatter(CppPropertiesProvider cppProperties) {
		Object o = null;
		try {
			ClassLoader classLoader = getEclipse45();
			Class<?> aClass = Class.forName("krasa.formatter.adapter.CppCodeFormatterFacade", true, classLoader);
			Constructor<?> constructor = aClass.getConstructor(CppPropertiesProvider.class);
			o = constructor.newInstance(cppProperties);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		return (CodeFormatterFacade) o;
	}

	@NotNull
	public static CodeFormatterFacade getJsFormatter(JSPropertiesProvider propertiesProvider) {
		Object o = null;
		try {
			ClassLoader classLoader = getEclipse44();
			Class<?> aClass = Class.forName("krasa.formatter.adapter.JSCodeFormatterFacade", true, classLoader);
			Constructor<?> constructor = aClass.getConstructor(JSPropertiesProvider.class);
			o = constructor.newInstance(propertiesProvider);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		return (CodeFormatterFacade) o;
	}

	public static Processor getGWTProcessor(Settings settings) {
		Object o = null;
		try {
			ClassLoader classLoader = getEclipse44();
			Class<?> aClass = Class.forName("krasa.formatter.adapter.processor.GWTProcessor", true, classLoader);
			Constructor<?> constructor = aClass.getConstructor(Settings.class);
			o = constructor.newInstance(settings);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		return (Processor) o;
	}

	public static Processor getJSCommentsFormatterProcessor(Settings settings) {
		Object o = null;
		try {
			ClassLoader classLoader = getEclipse44();
			Class<?> aClass = Class.forName("krasa.formatter.adapter.processor.JSCommentsFormatterProcessor", true,
					classLoader);
			Constructor<?> constructor = aClass.getConstructor(Settings.class);
			o = constructor.newInstance(settings);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		return (Processor) o;
	}
}
