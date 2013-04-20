package krasa.formatter.plugin.processor;

import static org.easymock.EasyMock.*;

import java.util.ArrayList;
import java.util.List;

import junit.framework.Assert;
import krasa.easymock.EasyMockTest;
import krasa.easymock.Mocked;
import krasa.formatter.common.ModifiableFile;
import krasa.formatter.plugin.ImportSorter;
import krasa.formatter.settings.Settings;
import krasa.formatter.settings.provider.ImportOrderProvider;

import org.junit.Test;

//import static org.easymock.EasyMock.*;

/**
 * @author Vojtech Krasa
 */

public class ImportOrderProcessorTest extends EasyMockTest {
	@Mocked
	protected ImportSorter importSorter;
	@Mocked
	protected ImportOrderProvider orderProvider;
	@Mocked
	protected Settings settings;
	@Mocked
	protected ModifiableFile.Monitor monitor;

	@Test
	public void testInitializeImportSorter() throws Exception {
		expect(settings.isImportOrderFromFile()).andReturn(true);
		expect(orderProvider.getModifiedMonitor()).andReturn(monitor);
		expect(orderProvider.get()).andReturn(getStrings());

		expect(settings.isImportOrderFromFile()).andReturn(true);
		expect(orderProvider.wasChanged(monitor)).andReturn(true);
		expect(orderProvider.getModifiedMonitor()).andReturn(monitor);
		expect(orderProvider.get()).andReturn(getStrings());

		replayAll();

		ImportOrderProcessor importOrderProcessor = new ImportOrderProcessor(settings, orderProvider);

		ImportSorter importSorter = importOrderProcessor.getImportSorter();
		Assert.assertNotNull(importSorter);
		importSorter = importOrderProcessor.getImportSorter();
		Assert.assertNotNull(importSorter);
	}

	@Test
	public void testInitializeImportSorter2() throws Exception {
		expect(settings.isImportOrderFromFile()).andReturn(false);
		expect(settings.getImportOrderAsList()).andReturn(new ArrayList<String>());

		replayAll();

		ImportOrderProcessor importOrderProcessor = new ImportOrderProcessor(settings, orderProvider);

		ImportSorter importSorter = importOrderProcessor.getImportSorter();
		Assert.assertNotNull(importSorter);
	}

	private List<String> getStrings() {
		List<String> importOrder;
		importOrder = new ArrayList<String>();
		importOrder.add("s");
		importOrder.add("a");
		return importOrder;
	}
}
