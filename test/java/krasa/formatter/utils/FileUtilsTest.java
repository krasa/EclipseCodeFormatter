package krasa.formatter.utils;

import krasa.formatter.eclipse.TestUtils;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.util.List;

/**
 * @author Vojtech Krasa
 */
public class FileUtilsTest {
	@Test
	public void testGetProfileNamesFromConfigXML() throws Exception {
		File file = new File(TestUtils.normalizeUnitTestPath("resources/format.xml"));
		System.err.println(file.getAbsolutePath());
		List<String> profileNamesFromConfigXML = FileUtils.getProfileNamesFromConfigXML(file);
		Assert.assertFalse(profileNamesFromConfigXML.isEmpty());
	}
}
