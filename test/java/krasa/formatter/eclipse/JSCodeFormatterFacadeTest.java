package krasa.formatter.eclipse;

import junit.framework.Assert;
import krasa.formatter.settings.Settings;
import krasa.formatter.settings.provider.JSPropertiesProvider;

import org.junit.Before;
import org.junit.Test;

/**
 * @author Vojtech Krasa
 */
public class JSCodeFormatterFacadeTest {

	public static final String INPUT = "/**\n" + " * Wrapper for java.lang.Object.wait\n" + "       *\n"
			+ "       * can be called only within a sync method\n" + " */\n" + "function wait(object) {\n"
			+ "                 var objClazz = java.lang.Class.forName('java.lang.Object');\n"
			+ "    var waitMethod = objClazz.getMethod('wait', null);\n" + "    waitMethod.invoke(object, null);\n"
			+ "} \n" + "wait.docString = \"convenient wrapper for java.lang.Object.wait method\";";

	public static final String FORMATTED = "/**\n" + " * Wrapper for java.lang.Object.wait\n" + " *\n"
			+ " * can be called only within a sync method\n" + " */\n" + "function wait(object) {\n"
			+ "\tvar objClazz = java.lang.Class.forName('java.lang.Object');\n"
			+ "\tvar waitMethod = objClazz.getMethod('wait', null);\n" + "\twaitMethod.invoke(object, null);\n" + "}\n"
			+ "wait.docString = \"convenient wrapper for java.lang.Object.wait method\";";

	public static final String PATH_TO_CONFIG_FILE = "resources/org.eclipse.wst.jsdt.core.prefs";

	@Before
	public void setUp() throws Exception {
		Settings settings = new Settings();
		setPath(settings, PATH_TO_CONFIG_FILE);
		eclipseCodeFormatterFacade = Classloaders.getJsFormatter(new JSPropertiesProvider(settings));
	}

	private void setPath(Settings settings, String pathToConfigFile) {
		pathToConfigFile = TestUtils.normalizeUnitTestPath(pathToConfigFile);
		settings.setPathToConfigFileJS(pathToConfigFile);
	}

	protected CodeFormatterFacade eclipseCodeFormatterFacade;

	@Test
	public void testFormat() throws Exception {
		String output = eclipseCodeFormatterFacade.format(INPUT, 0, INPUT.length(), null);
		Assert.assertEquals(FORMATTED, output);
	}

	@Test
	public void testFormat2() throws Exception {
		String output = eclipseCodeFormatterFacade.format(INPUT, 10, INPUT.length() - 10, null);
		Assert.assertEquals(FORMATTED, output);
	}

	@Test
	public void testEndOffset() throws Exception {
		eclipseCodeFormatterFacade.format(INPUT, INPUT.length() - 11, INPUT.length() - 10, null);
	}

}
