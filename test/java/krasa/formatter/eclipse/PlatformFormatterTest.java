package krasa.formatter.eclipse;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.CommandProcessor;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.module.ModuleType;
import com.intellij.openapi.module.StdModuleTypes;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.codeStyle.CodeStyleManager;
import com.intellij.psi.codeStyle.JavaCodeStyleSettings;
import com.intellij.psi.formatter.FormatterTestCase;
import com.intellij.testFramework.IdeaTestUtil;
import com.intellij.util.IncorrectOperationException;
import krasa.formatter.settings.ProjectComponent;
import krasa.formatter.settings.Settings;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.Arrays;

public class PlatformFormatterTest extends FormatterTestCase {

	@Override
	public void setUp() throws Exception {
		super.setUp();
		Settings settings = new Settings();
		settings.setFormatter(Settings.Formatter.ECLIPSE);
		ProjectComponent.getInstance(getProject()).installOrUpdate(settings);
	}


	public void testFormatting() {
		VirtualFile virtualFile;
		File file = new File("testProject\\src\\main\\java\\aaa\\XAAA.java");
		virtualFile = refreshAndFindFile(file);
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

	}

	@Override
	protected void tearDown() throws Exception {
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
		return "/psi/formatter/wrapping";
	}
}
