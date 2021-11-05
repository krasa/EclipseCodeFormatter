package krasa.formatter.eclipse;

import com.intellij.openapi.command.impl.DummyProject;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.pom.java.LanguageLevel;
import com.intellij.psi.PsiFile;
import com.intellij.psi.impl.JavaPsiImplementationHelper;
import krasa.formatter.exception.FileDoesNotExistsException;
import krasa.formatter.exception.FormattingFailedException;
import krasa.formatter.exception.InvalidSettingsException;
import krasa.formatter.plugin.InvalidPropertyFile;
import krasa.formatter.settings.GlobalSettings;
import krasa.formatter.settings.Settings;
import krasa.formatter.settings.provider.JavaPropertiesProvider;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * @author Vojtech Krasa
 */
public class JavaCodeFormatterFacade extends CodeFormatterFacade {

	private static final Logger LOG = Logger.getInstance(JavaCodeFormatterFacade.class.getName());

	private final Settings settings;
	private Project project;

	protected EclipseFormatterAdapter codeFormatter;

	private LanguageLevel effectiveLanguageLevel;

	private ConfigFileLocator configFileLocator = new ConfigFileLocator();
	@Nullable
	private JavaPropertiesProvider javaPropertiesProvider;

	public JavaCodeFormatterFacade(Settings settings, Project project) {
		this.settings = settings;
		this.project = project;
	}

	@Override
	public String format(String text, int startOffset, int endOffset, PsiFile psiFile)
			throws FileDoesNotExistsException {
		LanguageLevel languageLevel = getLanguageLevel(psiFile);
		return getCodeFormatter(languageLevel, psiFile).format(text, startOffset, endOffset, languageLevel);
	}


	private EclipseFormatterAdapter getCodeFormatter(LanguageLevel level, PsiFile psiFile) throws FileDoesNotExistsException {
		if (settings.getConfigType() == Settings.ConfigType.RESOLVE) {

			long start = System.currentTimeMillis();
			VirtualFile configFile = configFileLocator.traverseToFindConfigurationFileByConvention(psiFile, project);
			LOG.debug("config located in " + (System.currentTimeMillis() - start) + "ms (" + configFile + ")");

			if (configFile == null) {
				throw new FormattingFailedException("Formatter config file not resolved.", true);
			}
			if (javaPropertiesProvider == null || !javaPropertiesProvider.isSameFile(configFile) || javaPropertiesProvider.wasChanged()) {
				javaPropertiesProvider = new JavaPropertiesProvider(configFile.getCanonicalPath(), "");
				return newCodeFormatter(level, configFile);
			}
			return codeFormatter;
		}

		if (codeFormatter == null || configFileRefresh() || this.effectiveLanguageLevel != level) {
			return newCodeFormatter(level, null);
		}

		return codeFormatter;
	}

	private boolean configFileRefresh() {
		return settings.getConfigType() == Settings.ConfigType.CUSTOM && (javaPropertiesProvider == null || javaPropertiesProvider.wasChanged());
	}

	private EclipseFormatterAdapter newCodeFormatter(LanguageLevel level, VirtualFile configFile) {
		long start = System.currentTimeMillis();

		try {
			ClassLoader classLoader;
			// if (settings.getEclipseVersion() == Settings.FormatterVersion.CUSTOM) {
			classLoader = getCustomClassloader(GlobalSettings.getInstance().getPathToEclipse());
				// } else {
				// classLoader = Classloaders.getEclipse();
				// }

			Map<String, String> options = getEclipseProfileOptions(level, configFile, classLoader);

			Class<?> aClass1 = Class.forName("krasa.formatter.adapter.EclipseJavaFormatterAdapter", true, classLoader);
			Constructor<?> constructor = aClass1.getConstructor(Map.class);
			codeFormatter = (EclipseFormatterAdapter) constructor.newInstance(options);
		} catch (FormattingFailedException e) {
			throw e;
		} catch (InvalidPropertyFile e) {
			throw e;
		} catch (FileDoesNotExistsException e) {
			throw e;
		} catch (Throwable e) {
			// rethrow to have this plugin as a cause of the error report
			throw new RuntimeException(e);
		}
		LOG.info("newCodeFormatter in " + (System.currentTimeMillis() - start) + "ms, (" + configFile + ")");
		return codeFormatter;
	}

	@NotNull
	private Map<String, String> getEclipseProfileOptions(LanguageLevel level, VirtualFile configFile, ClassLoader classLoader) throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {
		Map<String, String> options = getEclipseProfileOptions(configFile, classLoader);

		if (options.size() < 100) {
			throw new FormattingFailedException("Invalid config file, it should contain 100+ properties.", true);
		}
		String languageLevel = toEclipseLanguageLevel(level);
		options.put("org.eclipse.jdt.core.compiler.source", languageLevel);
		options.put("org.eclipse.jdt.core.compiler.codegen.targetPlatform", languageLevel);
		options.put("org.eclipse.jdt.core.compiler.compliance", languageLevel);
		this.effectiveLanguageLevel = level;
		return options;
	}

	private Map<String, String> getEclipseProfileOptions(VirtualFile configFile, ClassLoader classLoader) throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {
		Class<?> aClass1 = Class.forName("org.eclipse.jdt.internal.formatter.DefaultCodeFormatterOptions", true, classLoader);
		Method getMap = aClass1.getMethod("getMap");
		Properties options;

		switch (settings.getConfigType()) {

			case RESOLVE:
				javaPropertiesProvider = new JavaPropertiesProvider(configFile.getPath(), null);
				options = javaPropertiesProvider.get();
				return toMap(options);
			case ECLIPSE:
				return (Map<String, String>) getMap.invoke(aClass1.getDeclaredMethod("getEclipseDefaultSettings").invoke(null));
			case ECLIPSE_2_1:
				return (Map<String, String>) getMap.invoke(aClass1.getDeclaredMethod("getDefaultSettings").invoke(null));
			case JAVA_CONVENTIONS:
				return (Map<String, String>) getMap.invoke(aClass1.getDeclaredMethod("getJavaConventionsSettings").invoke(null));
			case CUSTOM:
				javaPropertiesProvider = settings.getJavaProperties();
				options = javaPropertiesProvider.get();
				return toMap(options);
		}
		throw new IllegalStateException();
	}

	@NotNull
	protected static String toEclipseLanguageLevel(LanguageLevel level) {
		int feature = level.toJavaVersion().feature;
		if (feature < 10) {
			return "1." + feature;
		}
		return String.valueOf(feature);
	}

	/**
	 * TODO CACHE BETWEEN PROJECTS
	 *
	 * @return
	 */
	private ClassLoader getCustomClassloader(String pathToEclipse) throws ClassNotFoundException {
		if (StringUtils.isBlank(pathToEclipse)) {
			throw new InvalidSettingsException("Please set Eclipse installation location.", true);
		}
		ConfigurableEclipseLocation configurableEclipseLocation = new ConfigurableEclipseLocation();
		List<URL> urlList = configurableEclipseLocation.run(pathToEclipse.trim());
		if (urlList.isEmpty()) {
			throw new InvalidSettingsException("Invalid path to Eclipse, no jars found in '" + pathToEclipse + "'",
					true);
		}
		ClassLoader classLoader = Classloaders.getCustomClassloader(urlList);
		return classLoader;
	}


	@NotNull
	protected LanguageLevel getLanguageLevel(@NotNull PsiFile psiFile) {
		if (DummyProject.getInstance() == project) {
			return LanguageLevel.JDK_1_7; // tests hack
		}
		JavaPsiImplementationHelper instance = JavaPsiImplementationHelper.getInstance(project);
		LanguageLevel languageLevel = instance.getEffectiveLanguageLevel(psiFile.getVirtualFile());

		return languageLevel;
	}

	public ConfigFileLocator getConfigFileLocator() {
		return configFileLocator;
	}
}
