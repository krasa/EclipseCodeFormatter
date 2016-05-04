package krasa.formatter.eclipse;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.PathManager;
import com.intellij.openapi.diagnostic.Logger;
import krasa.formatter.processor.Processor;
import krasa.formatter.settings.Settings;
import krasa.formatter.settings.provider.CppPropertiesProvider;
import krasa.formatter.settings.provider.JSPropertiesProvider;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.lang.reflect.Constructor;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;

public class Classloaders {
	private static final Logger LOG = Logger.getInstance(Classloaders.class.getName());

	private static ClassLoader eclipse44;
	private static ClassLoader newEclipse;

	public static ClassLoader getEclipse44() {
		if (eclipse44 == null) {
			eclipse44 = classLoader(getPluginLibHomeEclipse44(),
					"adapter44.jar",
					"eclipse44.jar"
			);
		}
		return eclipse44;
	}

	public static ClassLoader getEclipse45() {
		if (newEclipse == null) {
			newEclipse = classLoader(getPluginLibHomeEclipse45(),
					"adapter45.jar",
					"eclipse45.jar");
		}
		return newEclipse;
	}

	public static ClassLoader getCustomClassloader(List<URL> jars) {
		try {
			jars.add(new File(getPluginLibHomeEclipse45(), "adapter45.jar").toURI().toURL());
			for (URL jar : jars) {
				if (!new File(jar.toURI()).exists()) {
					throw new IllegalStateException("Plugin jar file not found: " + jar.toURI());
				}
			}
			URL[] a = jars.toArray(new URL[jars.size()]);
			LOG.info("Creating classloader for " + Arrays.toString(a));
			return new ParentLastURLClassLoader(Classloaders.class.getClassLoader(), a);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@NotNull
	private static File getPluginLibHomeEclipse45() {
		File pluginHome;
		if (isUnitTest()) {
			pluginHome = new File("../lib/eclipse45");
		} else {
			pluginHome = new File(PathManager.getPluginsPath(), "EclipseFormatter/lib/");
		}
		return pluginHome;
	}

	private static boolean isUnitTest() {
		return ApplicationManager.getApplication() == null || ApplicationManager.getApplication().isUnitTestMode();
	}

	@NotNull
	private static File getPluginLibHomeEclipse44() {
		File pluginHome;
		if (isUnitTest()) {
			pluginHome = new File("../lib/eclipse44/");
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
