package krasa.formatter.settings.provider;

import java.io.File;
import java.util.List;

import krasa.formatter.settings.Settings;

import org.apache.commons.io.FileUtils;
import org.junit.Test;

public class ImportOrderProviderTest {

	public static final String[] ORDER = new String[] { "java", "javax", "org", "com", "br.gov.bcb" };

	@Test
	public void testReadFile() throws Exception {
		Settings settings = new Settings();
		File file = FileUtils.getFile("test/resources/bcjur2.importorder");
		settings.setImportOrderConfigFilePath(file.getAbsolutePath());
		ImportOrderProvider importOrderProvider = new ImportOrderProvider(settings);
		List<String> stringList = importOrderProvider.get();

		org.junit.Assert.assertArrayEquals(ORDER, stringList.toArray(new String[stringList.size()]));
	}
}
