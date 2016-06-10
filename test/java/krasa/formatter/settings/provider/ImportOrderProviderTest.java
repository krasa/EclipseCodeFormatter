package krasa.formatter.settings.provider;

import krasa.formatter.settings.Settings;

import org.apache.commons.io.FileUtils;
import org.junit.Test;

import java.io.File;
import java.util.List;

public class ImportOrderProviderTest {

	public static final String[] ORDER = new String[] { "java", "javax", "org", "com", "br.gov.bcb", "foo" };

	@Test
	public void testReadFile() throws Exception {
		Settings settings = new Settings();
		File file = FileUtils.getFile("resources/bcjur2.importorder");
		settings.setImportOrderConfigFilePath(file.getAbsolutePath());
		ImportOrderProvider importOrderProvider = new ImportOrderProvider(settings);
		List<String> stringList = importOrderProvider.get();

		org.junit.Assert.assertArrayEquals(ORDER, stringList.toArray(new String[stringList.size()]));
	}
}
