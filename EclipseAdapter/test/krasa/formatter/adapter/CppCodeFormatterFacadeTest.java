package krasa.formatter.adapter;

import java.io.File;

import org.junit.Before;
import org.junit.Test;

import junit.framework.Assert;
import krasa.formatter.settings.Settings;
import krasa.formatter.settings.provider.CppPropertiesProvider;

public class CppCodeFormatterFacadeTest {

	protected static final String INPUT = "#include <iostream>\n" + "\n" + " using namespace std;\n" + "\n"
			+ "  int main() {\n" + "     cout << \"Hello, World!\" << endl;\n" + "     return 0;\n" + "  }";
	protected static final String FORMATTED = "#include <iostream>\n" + "\n" + "using namespace std;\n" + "\n"
			+ "int main() {\n" + "    cout << \"Hello, World!\" << endl;\n" + "    return 0;\n" + "}\n";
	private String prefix = "";

	@Before
	public void setUp() throws Exception {
		if (!new File("test/resources").exists()) {
			prefix = "../";
		}
	}

	@Test
	public void xmlConfig() throws Exception {
		Settings settings = new Settings();
		settings.setPathToConfigFileCpp(prefix + "test/resources/cpp.xml");
		settings.setSelectedCppProfile("KR");
		CppCodeFormatterFacade cppCodeFormatterFacade = new CppCodeFormatterFacade(new CppPropertiesProvider(settings));
		String s = cppCodeFormatterFacade.format(INPUT, 0, INPUT.length(), null);
		System.err.println(s);
		Assert.assertEquals(FORMATTED, s);
	}

	@Test
	public void propertiesConfig() throws Exception {
		Settings settings = new Settings();
		settings.setPathToConfigFileCpp(prefix + "test/resources/org.eclipse.cdt.core.prefs");
		CppCodeFormatterFacade cppCodeFormatterFacade = new CppCodeFormatterFacade(new CppPropertiesProvider(settings));
		String s = cppCodeFormatterFacade.format(INPUT, 0, INPUT.length(), null);
		System.err.println(s);
		Assert.assertEquals(FORMATTED, s);
	}
}