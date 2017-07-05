package krasa.formatter.utils;

import java.io.File;
import java.util.List;

import junit.framework.Assert;

import krasa.formatter.eclipse.TestUtils;
import org.junit.Test;

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
