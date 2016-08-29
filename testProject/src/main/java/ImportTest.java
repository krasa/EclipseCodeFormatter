import java.io.IOException;

import org.junit.Before;
import org.junit.Test;

import mockit.MockUp;

/**
 * Test.
 */
public class ImportTest {

	@Before
	public void beforeTest() throws Exception {
		new MockUp<ImportTest>() {
			@Override

			public String toString() {
				return null;
			}
		};
	}

	@Test(expected = IOException.class)
	public void test() throws Exception {
		throw new IOException();
	}
}
