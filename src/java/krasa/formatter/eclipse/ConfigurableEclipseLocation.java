package krasa.formatter.eclipse;

import com.google.common.io.Files;
import com.intellij.openapi.diagnostic.Logger;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

public class ConfigurableEclipseLocation {
	private static final Logger LOG = Logger.getInstance(ConfigurableEclipseLocation.class.getName());


	String[] JAR_NAMES = {"org.eclipse.cdt.core_",
			"org.eclipse.core.contenttype_",
			"org.eclipse.core.jobs_",
			"org.eclipse.core.resources.win32.x86_",
			"org.eclipse.core.resources_",
			"org.eclipse.core.runtime_",
			"org.eclipse.equinox.app_",
			"org.eclipse.equinox.common_",
			"org.eclipse.equinox.preferences_",
			"org.eclipse.jdt.core_",
			"org.eclipse.osgi_",
			"org.eclipse.text_"
	};
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
			jars = findJars(new File(from));
		} catch (MalformedURLException e) {
			throw new RuntimeException(e);
		}

		long total = System.currentTimeMillis() - start;
		LOG.info("found " + jars.size() + " jars in " + total + "ms, (" + from + ")");
		return jars;
	}

	@NotNull
	private List<URL> findJars(File from) throws MalformedURLException {
		List<URL> files = new ArrayList<URL>();
		Iterator<File> iterator = Files.fileTreeTraverser().children(from).iterator();
		while (iterator.hasNext()) {
			File next = iterator.next();
			if (next.isDirectory()) {
				files.addAll(findJars(next));
			} else {
				String name = next.getName();
				if (name.endsWith(".jar")) {
					int i = name.indexOf("_");
					if (i <= 0)
						continue;
					if (jarNames.contains(name.substring(0, i + 1))) {
						files.add(next.toURI().toURL());
					}
				}
			}
		}
		return files;
	}
}
