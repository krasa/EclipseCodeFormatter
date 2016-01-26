package krasa.formatter.utils;

import java.io.File;
import java.util.List;

import junit.framework.Assert;

import org.junit.Test;

/**
 * @author Vojtech Krasa
 */
public class FileUtilsTest {
	@Test
	public void testGetProfileNamesFromConfigXML() throws Exception {
		File file = new File("resources/format.xml");
		System.err.println(file.getAbsolutePath());
		List<String> profileNamesFromConfigXML = FileUtils.getProfileNamesFromConfigXML(file);
		Assert.assertFalse(profileNamesFromConfigXML.isEmpty());
	}
}
