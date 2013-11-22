package krasa.formatter.plugin;

import static org.easymock.EasyMock.*;

import java.util.ArrayList;
import java.util.List;

import junit.framework.Assert;
import krasa.easymock.EasyMockTest;
import krasa.easymock.Mocked;
import krasa.formatter.common.ModifiableFile;
import krasa.formatter.settings.Settings;
import krasa.formatter.settings.provider.ImportOrderProvider;

import org.junit.Test;

//import static org.easymock.EasyMock.*;

/**
 * @author Vojtech Krasa
 */

public class EclipseImportOptimizerTest extends EasyMockTest {
	@Mocked
	protected ImportSorterAdapter importSorter;
	@Mocked
	protected ImportOrderProvider orderProvider;
	@Mocked
	protected Settings settings;
	@Mocked
	protected ModifiableFile.Monitor monitor;

	@Test
	public void testInitializeImportSorter() throws Exception {
		expect(settings.isImportOrderFromFile()).andReturn(true);
		expect(settings.getImportOrderProvider()).andReturn(orderProvider);
		expect(orderProvider.get()).andReturn(getStrings());

		replayAll();

		EclipseImportOptimizer eclipseImportOptimizer = new EclipseImportOptimizer();

		ImportSorterAdapter importSorter = eclipseImportOptimizer.getImportSorter(settings);
		Assert.assertNotNull(importSorter);
	}

	@Test
	public void testInitializeImportSorterFromFile() throws Exception {
		expect(settings.isImportOrderFromFile()).andReturn(false);
		expect(settings.getImportOrderAsList()).andReturn(new ArrayList<String>());

		replayAll();

		EclipseImportOptimizer eclipseImportOptimizer = new EclipseImportOptimizer();

		ImportSorterAdapter importSorter = eclipseImportOptimizer.getImportSorter(settings);
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
