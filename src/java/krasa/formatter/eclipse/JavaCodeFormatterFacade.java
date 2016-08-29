package krasa.formatter.eclipse;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import krasa.formatter.common.ModifiableFile;
import krasa.formatter.exception.FileDoesNotExistsException;
import krasa.formatter.exception.FormattingFailedException;
import krasa.formatter.plugin.Notifier;
import krasa.formatter.settings.Settings;
import krasa.formatter.settings.provider.JavaPropertiesProvider;

import org.jetbrains.annotations.NotNull;

import com.intellij.openapi.command.impl.DummyProject;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.SystemInfo;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.pom.java.LanguageLevel;
import com.intellij.psi.PsiFile;
import com.intellij.psi.impl.JavaPsiImplementationHelper;

/**
 * @author Vojtech Krasa
 */
public class JavaCodeFormatterFacade extends CodeFormatterFacade {

	private static final Logger LOG = Logger.getInstance(JavaCodeFormatterFacade.class.getName());

	private Settings.FormatterVersion version;
	private Project project;
	private String pathToEclipse;
	protected EclipseFormatterAdapter codeFormatter;
	private LanguageLevel effectiveLanguageLevel;
	private JavaPropertiesProvider javaPropertiesProvider;
	protected ModifiableFile.Monitor lastState;

	public JavaCodeFormatterFacade(JavaPropertiesProvider javaPropertiesProvider, Settings.FormatterVersion version,
								   Project project, String pathToEclipse) {
		this.javaPropertiesProvider = javaPropertiesProvider;
		this.version = version;
		this.project = project;
		this.pathToEclipse = pathToEclipse;
	}

	@Override
	public String format(String text, int startOffset, int endOffset, PsiFile psiFile)
			throws FileDoesNotExistsException {
		LanguageLevel languageLevel = getLanguageLevel(psiFile);
		return getCodeFormatter(languageLevel).format(text, startOffset, endOffset, languageLevel);
	}

	private EclipseFormatterAdapter getCodeFormatter(LanguageLevel level) throws FileDoesNotExistsException {
		if (codeFormatter == null || javaPropertiesProvider.wasChanged(lastState)
				|| this.effectiveLanguageLevel != level) {
			return newCodeFormatter(level);
		}
		return codeFormatter;
	}

	private EclipseFormatterAdapter newCodeFormatter(LanguageLevel level) {
		lastState = javaPropertiesProvider.getModifiedMonitor();
		Properties options = javaPropertiesProvider.get();
		String substring = level.name().replace("_", ".").substring(4);
		// test
		new Double(substring);
		options.setProperty("org.eclipse.jdt.core.compiler.source", substring);
		options.setProperty("org.eclipse.jdt.core.compiler.codegen.targetPlatform", substring);
		options.setProperty("org.eclipse.jdt.core.compiler.compliance", substring);
		this.effectiveLanguageLevel = level;

		try {
			Class<?> aClass;
			if (version == Settings.FormatterVersion.ECLIPSE_44) {
				aClass = getAdapter44();
			} else {
				if (version == Settings.FormatterVersion.CUSTOM && SystemInfo.isJavaVersionAtLeast("1.7")) {
					aClass = getCustomAdapter(pathToEclipse);
				} else if (SystemInfo.isJavaVersionAtLeast("1.8")) {
					aClass = getAdapter();
				} else {
					aClass = getAdapter44();
					Notifier.notifyOldJRE(project);
				}
			}
			Constructor<?> constructor = aClass.getConstructor(Map.class);
			codeFormatter = (EclipseFormatterAdapter) constructor.newInstance(toMap(options));
		} catch (FormattingFailedException e) {
			throw e;
		} catch (Throwable e) {
			// rethrow to have this plugin as a cause of the error report
			throw new RuntimeException(e);
		}
		return codeFormatter;
	}

	/**
	 * TODO CACHE BETWEEN PROJECTS
	 */
	private Class<?> getCustomAdapter(String pathToEclipse) throws ClassNotFoundException {
		ConfigurableEclipseLocation configurableEclipseLocation = new ConfigurableEclipseLocation();
		List<URL> urlList = configurableEclipseLocation.run(pathToEclipse.trim());
		if (urlList.isEmpty()) {
			throw new FormattingFailedException("Invalid path to Eclipse, no jars found in '" + pathToEclipse + "'", true);
		}
		ClassLoader classLoader = Classloaders.getCustomClassloader(urlList);
		Class<?> aClass = Class.forName("krasa.formatter.adapter.EclipseJavaFormatterAdapter", true, classLoader);
		return aClass;
	}

	private Class<?> getAdapter44() throws ClassNotFoundException {
		ClassLoader classLoader = Classloaders.getEclipse44();
		Class<?> aClass = Class.forName("krasa.formatter.adapter.EclipseJavaFormatterAdapter44", true, classLoader);
		return aClass;
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
		LanguageLevel languageLevel = null;
		try {
			Method getClassesLanguageLevel = instance.getClass().getMethod("getClassesLanguageLevel",
					VirtualFile.class);
			languageLevel = (LanguageLevel) getClassesLanguageLevel.invoke(instance, psiFile.getVirtualFile());
		} catch (Exception e) {
			try {
				Method getClassesLanguageLevel = instance.getClass().getMethod("getEffectiveLanguageLevel",
						VirtualFile.class);
				languageLevel = (LanguageLevel) getClassesLanguageLevel.invoke(instance, psiFile.getVirtualFile());
			} catch (Exception e1) {
				LOG.error("Please report this", e);
				LOG.error("Please report this", e1);
			}
		}
		if (languageLevel == null) {
			languageLevel = LanguageLevel.JDK_1_7;
		}
		return languageLevel;
	}

}
