package krasa.formatter.settings.provider;

import com.intellij.openapi.util.io.FileUtil;
import krasa.formatter.common.ModifiableFile;
import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

/**
 * @author Vojtech Krasa
 */
public class CachedProviderTest {

	protected CachedProvider<String> cachedProvider;
	protected File tempFile;

	@Before
	public void setUp() throws Exception {
		tempFile = File.createTempFile("12311", "2");
		cachedProvider = new CachedProvider<String>(new ModifiableFile(tempFile.getPath())) {

			@Override
			protected String readFile(File file) {
				try {
					return FileUtils.readFileToString(file);
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			}
		};
	}

	@Test
	public void testWasChanged() throws Exception {
		cachedProvider.get();
		Assert.assertFalse(cachedProvider.wasChanged());
		tempFile.setLastModified(tempFile.lastModified() + 1000);
		Assert.assertTrue(cachedProvider.wasChanged());
	}

	@Test
	public void testGet() throws Exception {
		FileUtil.writeToFile(tempFile, "foo");
		tempFile.setLastModified(1000);
		String s = cachedProvider.get();
		Assert.assertEquals("foo", s);

		FileUtil.writeToFile(tempFile, "bar");

		tempFile.setLastModified(1000);
		s = cachedProvider.get();
		Assert.assertEquals("foo", s);

		tempFile.setLastModified(2000);
		s = cachedProvider.get();
		Assert.assertEquals("bar", s);
	}
}
