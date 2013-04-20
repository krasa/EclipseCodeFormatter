package krasa.formatter.settings;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author Vojtech Krasa
 */
public class DisabledFileTypeSettingsTest {

	@Test
	public void testIsDisabled() throws Exception {
		DisabledFileTypeSettings disabledFileTypeSettings = new DisabledFileTypeSettings("html; groovy;");
		Assert.assertFalse(disabledFileTypeSettings.isDisabled("sadasd/aasdasdas.java"));
		Assert.assertTrue(disabledFileTypeSettings.isDisabled("sadasd/aasdasdas.html"));
		Assert.assertTrue(disabledFileTypeSettings.isDisabled("sadasd/aasdasdas.groovy"));
		Assert.assertFalse(disabledFileTypeSettings.isDisabled("sadasd/aasdasdas.groovy1"));

	}
}
