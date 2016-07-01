package krasa.formatter.eclipse;

import krasa.formatter.exception.FormattingFailedException;

import com.intellij.openapi.diagnostic.Logger;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

public class ConfigurableEclipseLocation {
	private static final Logger LOG = Logger.getInstance(ConfigurableEclipseLocation.class.getName());
	private static final int TIMEOUT = 5000;

	//@formatter:off
	String[] JAR_NAMES = {
			"org.eclipse.core.contenttype_",
			"org.eclipse.core.jobs_",
			"org.eclipse.core.resources_",
			"org.eclipse.core.runtime_",
			"org.eclipse.equinox.app_",//probably useless
			"org.eclipse.equinox.common_",
			"org.eclipse.equinox.preferences_",
			"org.eclipse.jdt.core_",
			"org.eclipse.osgi_",
			"org.eclipse.text_"
	};
	//@formatter:on

	public Set<String> jarNames;

	public ConfigurableEclipseLocation() {
		jarNames = new HashSet<String>();
		jarNames.addAll(Arrays.asList(JAR_NAMES));

	}

	public static void main(String[] args) throws IOException {
		List<URL> urlList = new ConfigurableEclipseLocation().run("F:\\workspace\\eclipse-jee 4.5.2");
		for (URL jar : urlList) {
			System.out.println(jar);
		}
	}

	public List<URL> run(String from) {
		long start = System.currentTimeMillis();
		List<URL> jars = null;
		try {
			jars = findJars(start, new File(from));
		} catch (MalformedURLException e) {
			throw new RuntimeException(e);
		}
		if (!jarNames.isEmpty()) {
			throw new FormattingFailedException("Required jars not found in '" + from + "': " + jarNames.toString(), true);
		}

		long total = System.currentTimeMillis() - start;
		LOG.info("found " + jars.size() + " jars in " + total + "ms, (" + from + ")");
		return jars;
	}

	@NotNull
	private List<URL> findJars(long start, File from) throws MalformedURLException {
		if (System.currentTimeMillis() - start > TIMEOUT) {
			throw new FormattingFailedException("Timeout, aborting search for jars.", true);
		}

		List<URL> files = new ArrayList<URL>();
		Iterator<File> iterator = FileUtils.iterateFiles(from, FileFilterUtils.trueFileFilter(), FileFilterUtils.trueFileFilter());
		while (iterator.hasNext()) {
			File next = iterator.next();
			if (next.isDirectory()) {
				files.addAll(findJars(start, next));
			} else {
				String name = next.getName();
				if (name.endsWith(".jar")) {
					int i = name.indexOf("_");
					if (i <= 0)
						continue;
					String jarName = name.substring(0, i + 1);
					if (jarNames.contains(jarName)) {
						jarNames.remove(jarName);
						files.add(next.toURI().toURL());
					}
				}
			}
		}
		return files;
	}
}
