package krasa.formatter.eclipse;

import java.io.File;
import java.util.HashMap;
import java.util.Properties;

import krasa.formatter.utils.FileUtils;

/**
 * @author Vojtech Krasa
 */
public class TestUtils {

	public static final String TEST_RESOURCES_ORG_ECLIPSE_JDT_CORE_PREFS = "test/resources/org.eclipse.jdt.core.prefs";
	public static final String TEST_RESOURCES_ORG_ECLIPSE_JDT_CORE_PREFS_DEFAULT = "../test/resources/org.eclipse.jdt.core_DEFAULT.prefs";
	public static final String TEST_RESOURCES_ORG_ECLIPSE_WST_JSDT_CORE_PREFS = "../test/resources/org.eclipse.wst.jsdt.core.prefs";

	public static HashMap<String, String> getJSProperties() {
		HashMap<String, String> jsMap = new HashMap<String, String>();
		Properties js = FileUtils.readPropertiesFile(new File(TEST_RESOURCES_ORG_ECLIPSE_WST_JSDT_CORE_PREFS));
		for (Object o : js.keySet()) {
			jsMap.put(String.valueOf(o), (String) js.get(o));
		}
		return jsMap;
	}

	public static HashMap<String, String> getJavaProperties() {
		Properties javaProperties = FileUtils.readPropertiesFile(new File(
				TEST_RESOURCES_ORG_ECLIPSE_JDT_CORE_PREFS_DEFAULT));
		HashMap<String, String> javaFormattingPrefs = new HashMap<String, String>();
		for (Object o : javaProperties.keySet()) {
			javaFormattingPrefs.put(String.valueOf(o), (String) javaProperties.get(o));
		}
		return javaFormattingPrefs;
	}
}
