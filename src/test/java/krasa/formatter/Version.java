package krasa.formatter;

import org.junit.Assert;
import org.junit.Test;

import static com.intellij.util.text.VersionComparatorUtil.compare;

public class Version {
	@Test
	public void name() {
		Assert.assertEquals(1, compare("17.4.132.637.0-Eclipse_4.7", "17.4.132.637.0-Eclipse_4.6"));
        Assert.assertEquals(1, compare("17.3.132.637.0-Eclipse_4.7.3a", "17.2.132.637.0-Eclipse_4.7.3a"));
        Assert.assertEquals(1, compare("17.4.132.637.0-Eclipse_4.7.3a", "17.3.132.637.0-Eclipse_4.7.3a"));
        Assert.assertEquals(1, compare("17.4.132.637.0-Eclipse_4.7.3a", "17.3.132.637.0-Eclipse_4.6"));
		Assert.assertEquals(1, compare("18.132.637.0-Eclipse_4.7.3a", "17.3.132.637.0-Eclipse_4.7.3a"));
		Assert.assertEquals(1, compare("18.2.181.000.0-Eclipse_4.8.0", "18.1.181.000.0-Eclipse_4.7.3a"));
		Assert.assertEquals(1, compare("18.2.181.000.0-Eclipse_4.9.0", "18.1.181.000.0-Eclipse_4.8.0"));
	}
}
