package krasa.formatter.settings.provider;

import krasa.formatter.eclipse.TestUtils;
import krasa.formatter.settings.Settings;
import org.apache.commons.io.FileUtils;
import org.junit.Test;

import java.util.List;

public class ImportOrderProviderTest {

	public static final String[] ORDER = new String[] { "java", "javax", "org", "com", "br.gov.bcb", "foo", "#" };
	private static final String[] ORDER_2 = { "com.mycorp", "#com.mycorp", "com", "#com" };

	@Test
	public void testReadFile() throws Exception {
		List<String> importOrder = getOrderFromFile("resources/bcjur2.importorder");

		org.junit.Assert.assertArrayEquals(ORDER, importOrder.toArray(new String[importOrder.size()]));
	}

	@Test
	public void issue104() throws Exception {
		List<String> importOrder = getOrderFromFile("resources/issue104.importorder");

		org.junit.Assert.assertArrayEquals(ORDER_2, importOrder.toArray(new String[importOrder.size()]));
	}

	@Test
	public void issue130_incorrect_parsing_of_import_order_from_prefs() throws Exception {
		List<String> importOrder = getOrderFromFile("resources/issue130.importorder");
		List<String> importOrder2 = getOrderFromFile("resources/issue130.prefs");

		org.junit.Assert.assertEquals(importOrder, importOrder2);
	}

	private List<String> getOrderFromFile(String path) {
		Settings settings = new Settings();
		path = TestUtils.normalizeUnitTestPath(path);
		settings.setImportOrderConfigFilePath(FileUtils.getFile(path).getAbsolutePath());
		ImportOrderProvider importOrderProvider = new ImportOrderProvider(settings);
		return importOrderProvider.get();
	}

}
