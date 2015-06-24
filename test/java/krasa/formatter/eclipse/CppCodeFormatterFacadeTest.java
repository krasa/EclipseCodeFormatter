package krasa.formatter.eclipse;

import junit.framework.Assert;
import krasa.formatter.settings.Settings;
import krasa.formatter.settings.provider.*;
import org.junit.Test;

public class CppCodeFormatterFacadeTest {

	protected static final String INPUT = "#include <iostream>\n" + "\n" + " using namespace std;\n" + "\n"
			+ "  int main() {\n" + "     cout << \"Hello, World!\" << endl;\n" + "     return 0;\n" + "  }";
	protected static final String FORMATTED = "#include <iostream>\n" + "\n" + "using namespace std;\n" + "\n"
			+ "int main() {\n" + "    cout << \"Hello, World!\" << endl;\n" + "    return 0;\n" + "}\n";

	@Test
	public void xmlConfig() throws Exception {
		Settings settings = new Settings();
		settings.setPathToConfigFileCpp("test/resources/cpp.xml");
		settings.setSelectedCppProfile("KR");
		CppCodeFormatterFacade cppCodeFormatterFacade = new CppCodeFormatterFacade(new CppPropertiesProvider(settings));
		String s = cppCodeFormatterFacade.format(INPUT, 0, INPUT.length(), null);
		System.err.println(s);
		Assert.assertEquals(FORMATTED, s);
	}

	@Test
	public void propertiesConfig() throws Exception {
		Settings settings = new Settings();
		settings.setPathToConfigFileCpp("test/resources/org.eclipse.cdt.core.prefs");
		CppCodeFormatterFacade cppCodeFormatterFacade = new CppCodeFormatterFacade(new CppPropertiesProvider(settings));
		String s = cppCodeFormatterFacade.format(INPUT, 0, INPUT.length(), null);
		System.err.println(s);
		Assert.assertEquals(FORMATTED, s);
	}
}