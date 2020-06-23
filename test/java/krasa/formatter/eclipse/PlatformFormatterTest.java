package krasa.formatter.eclipse;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.CommandProcessor;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.module.ModuleType;
import com.intellij.openapi.module.StdModuleTypes;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.codeStyle.CodeStyleManager;
import com.intellij.psi.codeStyle.JavaCodeStyleSettings;
import com.intellij.psi.formatter.FormatterTestCase;
import com.intellij.testFramework.IdeaTestUtil;
import com.intellij.testFramework.UsefulTestCase;
import com.intellij.util.IncorrectOperationException;
import krasa.formatter.settings.ProjectComponent;
import krasa.formatter.settings.Settings;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.Arrays;

public class PlatformFormatterTest extends FormatterTestCase {

	private static final String BASE_PATH = "../testProject";
	ConfigFileLocator.IModuleResolverStrategy previousResolver;

	@Override
	public void setUp() throws Exception {
		super.setUp();
		Settings settings = new Settings();
		settings.setFormatter(Settings.Formatter.ECLIPSE);
		settings.setProfileScheme(Settings.ProfileScheme.PROJECT_SPECIFIC);
		ProjectComponent.getInstance(getProject()).installOrUpdate(settings);
		previousResolver = ConfigFileLocator.TESTING_getModuleResolver();
	}

	public void testFormatting() {


		ConfigFileLocator.TESTING_setModuleResolver(new ConfigFileLocator.IModuleResolverStrategy() {
			@Override
			public VirtualFile getModuleDirForFile(VirtualFile virtualFile, Project project) {

				if (virtualFile.getPath().contains("/testProject/submodule-a")) {
					return UsefulTestCase.refreshAndFindFile(new File("../testProject/submodule-a"));
				}
				if (virtualFile.getPath().contains("/testProject/submodule-b")) {
					return UsefulTestCase.refreshAndFindFile(new File("../testProject/submodule-b"));
				}
				if (virtualFile.getPath().contains("/testProject")) {
					return UsefulTestCase.refreshAndFindFile(new File("../testProject"));
				}
				throw new UnsupportedOperationException("Not expected " + virtualFile);
			}
		});

		{
			VirtualFile virtualFile = refreshAndFindFile(new File("testProject/submodule-a/src/main/java/aaa/XAAA.java"));

			PsiFile psiFile = PsiManager.getInstance(getProject()).findFile(virtualFile);

			CommandProcessor.getInstance().executeCommand(getProject(), () -> ApplicationManager.getApplication().runWriteAction(() -> performFormatting(psiFile)), "",
					"");

			Document document = getDocument(psiFile);
			String fileText = document.getText();
			assertEquals("package aaa;\n" +
					"\n" +
					"public class XAAA {\n" +
					"\tpublic static void aaa() {\n" +
					"\n" +
					"\t}\n" +
					"}\n", fileText);
			//submodule-a has no own formatter configuration, so it uses the one of its parent
			assertFormatterFile("testProject/.settings/org.eclipse.jdt.core.prefs");

		}

		{
			VirtualFile virtualFile = refreshAndFindFile(new File("../testProject/submodule-b/src/main/java/bbb/XBBB.java"));

			PsiFile psiFile = PsiManager.getInstance(getProject()).findFile(virtualFile);

			CommandProcessor.getInstance().executeCommand(getProject(), () -> ApplicationManager.getApplication().runWriteAction(() -> performFormatting(psiFile)), "",
					"");

			assertFormatterFile("testProject/submodule-b/mechanic-formatter.epf");
			Document document = getDocument(psiFile);
			String fileText = document.getText();
			assertEquals("package bbb;\n" +
					"\n" +
					"public class XBBB {\n" +
					"    public static void bbb() {\n" +
					"\n" +
					"    }\n" +
					"}\n", fileText);
			//submodule-b has its own formatter configuration
		}

		{
			VirtualFile virtualFile = refreshAndFindFile(new File("../testProject/src/main/java/aaa/XAAA.java"));

			PsiFile psiFile = PsiManager.getInstance(getProject()).findFile(virtualFile);

			CommandProcessor.getInstance().executeCommand(getProject(), () -> ApplicationManager.getApplication().runWriteAction(() -> performFormatting(psiFile)), "",
					"");

			Document document = getDocument(psiFile);
			String fileText = document.getText();
			assertEquals("package aaa;\n" +
					"\n" +
					"public class XAAA {\n" +
					"\tpublic static void aaa() {\n" +
					"\n" +
					"\t}\n" +
					"}\n", fileText);
			//the parent project has its own formatter configuration
			assertFormatterFile("testProject/.settings/org.eclipse.jdt.core.prefs");
		}

	}

	private void assertFormatterFile(String s) {
		String filePath = ConfigFileLocator.getInstance(getProject()).TESTING_getMostRecentFormatterFile().getPath();
		assertTrue("Used " + filePath, filePath.endsWith(s));
	}

	@Override
	protected void tearDown() throws Exception {

		ConfigFileLocator.TESTING_setModuleResolver(previousResolver);

		try {
			super.tearDown();
		} catch (RuntimeException exception) {
			// TODO: Fix inability to free pointer
			if (exception.getMessage().contains("Virtual pointer hasn't been disposed")) {
				throw exception;
			}
		}
	}


	protected void performFormatting(PsiFile file) {
		try {
			CodeStyleManager.getInstance(this.getProject()).reformatText(file, Arrays.asList(new TextRange(0, file.getTextLength())));
		} catch (IncorrectOperationException var3) {
			fail();
		}
	}

	@Override
	protected Sdk getProjectJDK() {
		return IdeaTestUtil.getMockJdk17();
	}

	@NotNull
	protected ModuleType getModuleType() {
		return StdModuleTypes.JAVA;
	}

	@Override
	protected String getFileExtension() {
		return "java";
	}

	protected JavaCodeStyleSettings getCustomJavaSettings() {
		return JavaCodeStyleSettings.getInstance(getProject());
	}

	@Override
	protected String getBasePath() {
		return BASE_PATH;
	}
}
