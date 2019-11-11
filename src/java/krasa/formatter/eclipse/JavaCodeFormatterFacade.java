package krasa.formatter.eclipse;

import com.intellij.openapi.command.impl.DummyProject;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.pom.java.LanguageLevel;
import com.intellij.psi.PsiFile;
import com.intellij.psi.codeStyle.CodeStyleManager;
import com.intellij.psi.impl.JavaPsiImplementationHelper;
import krasa.formatter.common.ModifiableFile;
import krasa.formatter.exception.FileDoesNotExistsException;
import krasa.formatter.exception.FormattingFailedException;
import krasa.formatter.plugin.InvalidPropertyFile;
import krasa.formatter.settings.Settings;
import krasa.formatter.settings.provider.JavaPropertiesProvider;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * @author Vojtech Krasa
 */
public class JavaCodeFormatterFacade extends CodeFormatterFacade {

	private static final Logger LOG = Logger.getInstance(JavaCodeFormatterFacade.class.getName());
	private static IModuleResolverStrategy moduleResolver = new DefaultModuleResolverStrategy();
	private static VirtualFile mostRecentFormatterFile = null;

	private final List<String> CONVENTIONFILENAMES = Arrays.asList(//
			".settings/org.eclipse.jdt.core.prefs",//
			".settings/mechanic-formatter.epf",//
			"mechanic-formatter.epf" //
	);
	protected EclipseFormatterAdapter codeFormatter;
	protected ModifiableFile.Monitor lastState;
	private Settings.FormatterVersion version;
	private Project project;
	private String pathToEclipse;
	private LanguageLevel effectiveLanguageLevel;
	private Settings.ProfileScheme profileScheme;
	private JavaPropertiesProvider javaPropertiesProvider;

	public JavaCodeFormatterFacade(Settings.ProfileScheme profileScheme, JavaPropertiesProvider javaPropertiesProvider, Settings.FormatterVersion version,
								   Project project, String pathToEclipse) {
		this.profileScheme = profileScheme;
		this.javaPropertiesProvider = javaPropertiesProvider;
		this.version = version;
		this.project = project;
		this.pathToEclipse = pathToEclipse;
	}

	public JavaCodeFormatterFacade(Settings settings, CodeStyleManager original) {
		this(settings.getProfileScheme(), settings.getJavaProperties(), settings.getEclipseVersion(), original.getProject(), settings.getPathToEclipse());
	}

	/**
	 * VisibleForTesting
	 */
	@Deprecated
	public static void TESTING_setModuleResolver(IModuleResolverStrategy otherModuleResolver) {
		moduleResolver = otherModuleResolver;
	}

	/**
	 * VisibleForTesting
	 */
	@Deprecated
	public static IModuleResolverStrategy TESTING_getModuleResolver() {
		return moduleResolver;
	}

	/**
	 * VisibleForTesting
	 */
	@Deprecated
	public static VirtualFile TESTING_getMostRecentFormatterFile() {
		return mostRecentFormatterFile;
	}

	@Override
	public String format(String text, int startOffset, int endOffset, PsiFile psiFile)
			throws FileDoesNotExistsException {
		LanguageLevel languageLevel = getLanguageLevel(psiFile);
		VirtualFile formatterFile = traverseToFindConfigurationFileByConvention(psiFile);
		mostRecentFormatterFile = formatterFile;

		return getCodeFormatter(languageLevel, formatterFile).format(text, startOffset, endOffset, languageLevel);
	}

	@Nullable
	private VirtualFile traverseToFindConfigurationFileByConvention(PsiFile psiFile) {

		VirtualFile moduleFileDir = getModuleDirForFile(psiFile.getVirtualFile(), project);

		while (moduleFileDir != null) {

			for (String conventionFileName : CONVENTIONFILENAMES) {
				VirtualFile fileByRelativePath = moduleFileDir.findFileByRelativePath(conventionFileName);
				if (fileByRelativePath != null && fileByRelativePath.exists()) {
					return fileByRelativePath;
				}
			}
			moduleFileDir = getNextParentModuleDirectory(moduleFileDir);
		}
		return null;
	}

	private VirtualFile getModuleDirForFile(VirtualFile virtualFile, Project project) {
		// delegate to a strategy which can be overriden in unit tests
		return moduleResolver.getModuleDirForFile(virtualFile, project);
	}

	private VirtualFile getNextParentModuleDirectory(VirtualFile currentModuleDir) {
		//Jump outside the current project
		VirtualFile parent = currentModuleDir.getParent();
		if (parent != null && parent.exists()) {
			//the file/dir outside the project may be within another loaded module
			// NOTE all modules must be loaded for detecting the parent module of the current one
			VirtualFile dirOfParentModule = getModuleDirForFile(parent, project);
			if (dirOfParentModule != null) {
				return dirOfParentModule;
			}
		}
		return null;
	}

	private EclipseFormatterAdapter getCodeFormatter(LanguageLevel level, VirtualFile formatterFileByConventionOverConfiguration) throws FileDoesNotExistsException {

		if (null != formatterFileByConventionOverConfiguration && !isSameFile(javaPropertiesProvider.getModifiableFile(), formatterFileByConventionOverConfiguration)) {
			javaPropertiesProvider = new JavaPropertiesProvider(formatterFileByConventionOverConfiguration.getCanonicalPath(), "");
			return newCodeFormatter(level);
		}

		if (codeFormatter == null || configFileRefresh() || this.effectiveLanguageLevel != level) {
			return newCodeFormatter(level);
		}

		return codeFormatter;
	}

	private boolean configFileRefresh() {
		return profileScheme == Settings.ProfileScheme.FILE && javaPropertiesProvider.wasChanged(lastState);
	}

	private boolean isSameFile(ModifiableFile fileA, VirtualFile fileB) {
		try {
			return fileA.getCanonicalPath().equals(fileB.getCanonicalPath());
		} catch (IOException e) {
			return false;
		}
	}

	private EclipseFormatterAdapter newCodeFormatter(LanguageLevel level) {
		try {
			ClassLoader classLoader;
			if (version == Settings.FormatterVersion.CUSTOM) {
				classLoader = getCustomClassloader(pathToEclipse);
			} else {
				classLoader = Classloaders.getEclipse();
			}

			Map<String, String> options = getEclipseProfileOptions(classLoader);
			String languageLevel = toEclipseLanguageLevel(level);
			options.put("org.eclipse.jdt.core.compiler.source", languageLevel);
			options.put("org.eclipse.jdt.core.compiler.codegen.targetPlatform", languageLevel);
			options.put("org.eclipse.jdt.core.compiler.compliance", languageLevel);
			this.effectiveLanguageLevel = level;

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
		return codeFormatter;
	}

	private Map<String, String> getEclipseProfileOptions(ClassLoader classLoader) throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {
		Class<?> aClass1 = Class.forName("org.eclipse.jdt.internal.formatter.DefaultCodeFormatterOptions", true, classLoader);
		Method getMap = aClass1.getMethod("getMap");
		switch (profileScheme) {

			case PROJECT_SPECIFIC:
				//TODO
				break;
			case ECLIPSE:
				return (Map<String, String>) getMap.invoke(aClass1.getDeclaredMethod("getEclipseDefaultSettings").invoke(null));
			case ECLIPSE_2_1:
				return (Map<String, String>) getMap.invoke(aClass1.getDeclaredMethod("getDefaultSettings").invoke(null));
			case JAVA_CONVENTIONS:
				return (Map<String, String>) getMap.invoke(aClass1.getDeclaredMethod("getJavaConventionsSettings").invoke(null));
			case FILE:
				lastState = javaPropertiesProvider.getModifiedMonitor();
				Properties options = javaPropertiesProvider.get();
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
		ConfigurableEclipseLocation configurableEclipseLocation = new ConfigurableEclipseLocation();
		List<URL> urlList = configurableEclipseLocation.run(pathToEclipse.trim());
		if (urlList.isEmpty()) {
			throw new FormattingFailedException("Invalid path to Eclipse, no jars found in '" + pathToEclipse + "'", true);
		}
		ClassLoader classLoader = Classloaders.getCustomClassloader(urlList);
		return classLoader;
	}

	private Class<?> getAdapter() throws ClassNotFoundException {
		ClassLoader classLoader = Classloaders.getEclipse();
		Class<?> aClass = Class.forName("krasa.formatter.adapter.EclipseJavaFormatterAdapter", true, classLoader);
		return aClass;
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


	public interface IModuleResolverStrategy {
		VirtualFile getModuleDirForFile(VirtualFile virtualFile, Project project);
	}

	static class DefaultModuleResolverStrategy implements IModuleResolverStrategy {
		@Override
		public VirtualFile getModuleDirForFile(VirtualFile virtualFile, Project project) {
			Module moduleForFile = ModuleUtil.findModuleForFile(virtualFile, project);
			if (moduleForFile != null) {
				return moduleForFile.getModuleFile().getParent();
			} else {
				return null;
			}
		}
	}

}
