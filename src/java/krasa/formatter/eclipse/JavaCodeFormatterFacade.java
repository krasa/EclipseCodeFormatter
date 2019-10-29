package krasa.formatter.eclipse;

import com.intellij.openapi.command.impl.DummyProject;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.pom.java.LanguageLevel;
import com.intellij.psi.PsiFile;
import com.intellij.psi.impl.JavaPsiImplementationHelper;
import krasa.formatter.common.ModifiableFile;
import krasa.formatter.exception.FileDoesNotExistsException;
import krasa.formatter.exception.FormattingFailedException;
import krasa.formatter.settings.Settings;
import krasa.formatter.settings.provider.JavaPropertiesProvider;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.lang.reflect.Constructor;
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
		VirtualFile formatterFile = traverseToFindConfigurationFilesByConvention(psiFile);
		System.out.println("found Formatter at = " + formatterFile);

		return getCodeFormatter(languageLevel, formatterFile).format(text, startOffset, endOffset, languageLevel);
	}

	@Nullable
	private VirtualFile traverseToFindConfigurationFilesByConvention(PsiFile psiFile) {

		VirtualFile currentDirectory = psiFile.getVirtualFile().getParent();
		VirtualFile dir = currentDirectory;
		VirtualFile moduleFile = ProjectRootManager.getInstance(psiFile.getProject()).getFileIndex().getModuleForFile(psiFile.getVirtualFile()).getModuleFile().getParent();

		for (; ; ) {
			if (dir == null || !dir.exists()) {
				//no more parent directory to traverse 
				break;
			}
			List<String> conventionFileNames = Arrays.asList(".settings/org.eclipse.jdt.core.prefs", "mechanic-formatter.epf", ".settings/mechanic-formatter.epf");
			for (String conventionFileName : conventionFileNames) {
				VirtualFile fileByRelativePath = dir.findFileByRelativePath(conventionFileName);
				if (fileByRelativePath != null && fileByRelativePath.exists()) {
					return fileByRelativePath;
				}
			}
			if (dir.equals(moduleFile)) {
				//Abort traversing at module root directory
				break;
			}
			dir = dir.getParent();
		}
		return null;
	}

	private EclipseFormatterAdapter getCodeFormatter(LanguageLevel level, VirtualFile formatterFileByConventionOverConfiguration) throws FileDoesNotExistsException {

		if (null != formatterFileByConventionOverConfiguration && !isSameFile(javaPropertiesProvider.getModifiableFile(), formatterFileByConventionOverConfiguration)) {
			javaPropertiesProvider = new JavaPropertiesProvider(formatterFileByConventionOverConfiguration.getCanonicalPath(), "");
			return newCodeFormatter(level);
		}

		if (codeFormatter == null || javaPropertiesProvider.wasChanged(lastState)
				|| this.effectiveLanguageLevel != level ) {
			return newCodeFormatter(level);
		}
		
		return codeFormatter;
	}

	private boolean isSameFile(ModifiableFile fileA, VirtualFile fileB) {
		try {
			return fileA.getCanonicalPath().equals(fileB.getCanonicalPath());
		} catch (IOException e) {
			return false;
		}
	}

	private EclipseFormatterAdapter newCodeFormatter(LanguageLevel level) {
		lastState = javaPropertiesProvider.getModifiedMonitor();
		Properties options = javaPropertiesProvider.get();
		String languageLevel = toEclipseLanguageLevel(level);
		options.setProperty("org.eclipse.jdt.core.compiler.source", languageLevel);
		options.setProperty("org.eclipse.jdt.core.compiler.codegen.targetPlatform", languageLevel);
		options.setProperty("org.eclipse.jdt.core.compiler.compliance", languageLevel);
		this.effectiveLanguageLevel = level;

		try {
			Class<?> aClass;
			if (version == Settings.FormatterVersion.CUSTOM) {
				aClass = getCustomAdapter(pathToEclipse);
			} else {
				aClass = getAdapter();
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

}
