package krasa.formatter;

import static com.intellij.util.text.VersionComparatorUtil.compare;

import org.junit.Assert;
import org.junit.Test;

public class Version {
	@Test
	public void name() {
        Assert.assertEquals(1, compare("17.3.132.637.0-Eclipse_4.7.3a", "17.2.132.637.0-Eclipse_4.7.3a"));
        Assert.assertEquals(1, compare("17.4.132.637.0-Eclipse_4.7.3a", "17.3.132.637.0-Eclipse_4.7.3a"));
        Assert.assertEquals(1, compare("17.4.132.637.0-Eclipse_4.7.3a", "17.3.132.637.0-Eclipse_4.6"));
		Assert.assertEquals(1, compare("18.132.637.0-Eclipse_4.7.3a", "17.3.132.637.0-Eclipse_4.7.3a"));
		
		
		Assert.assertEquals(1, compare("17.4.132.637.0-Eclipse_4.7", "17.4.132.637.0-Eclipse_4.6"));
	}
}
