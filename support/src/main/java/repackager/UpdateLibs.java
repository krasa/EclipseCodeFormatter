package repackager;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.FileUtils;

import com.google.common.base.Predicate;
import com.google.common.io.Files;

public class UpdateLibs {

	private static final File CLASSLOADERS = new File("./src/java/krasa/formatter/eclipse/Classloaders.java");

	public static void main(String[] args) throws IOException {
		new UpdateLibs().run();
	}

	private void run() throws IOException {
		String from = "F:\\workspace\\eclipse-cpp-mars-2-win32";
		File currentJars = new File("support/eclipseLibs45/libs");
		File copyTo = new File(currentJars + "/temp");

		Map<String, String> oldJars = getJarsToUpdate(currentJars);

		List<File> jarsToCopy = getJarsToCopy(oldJars.keySet(), new File(from));

		copyJars(jarsToCopy, copyTo);

		updateClassloaders(jarsToCopy, oldJars);
	}

	private void updateClassloaders(List<File> jarsToCopy, Map<String, String> oldJars) throws IOException {
		assert (CLASSLOADERS.exists());
		String s = FileUtils.readFileToString(CLASSLOADERS);

		for (File file : jarsToCopy) {
			String prefix = jarPrefix(file.getName());
			String oldJar = oldJars.get(prefix);
			s = s.replace(oldJar, file.getName());
		}
		FileUtils.writeStringToFile(CLASSLOADERS, s);
	}

	private String jarPrefix(String name) {
		int i = name.indexOf("_");
		return name.substring(0, i);
	}

	private Map<String, String> getJarsToUpdate(File t) {
		Map<String, String> oldJars = new HashMap<String, String>();
		Iterator<File> iterator = Files.fileTreeTraverser().children(t).iterator();
		while (iterator.hasNext()) {
			File next = iterator.next();
			String name = next.getName();
			if (name.endsWith(".jar")) {
				int i = name.indexOf("_");
				if (i <= 0)
					continue;
				oldJars.put(name.substring(0, i), name);
			}
		}
		System.out.println("Old jars (" + oldJars.size() + ") found in " + t.getAbsolutePath());
		for (Map.Entry<String, String> jarName : oldJars.entrySet()) {
			System.out.println("\t" + jarName.getValue());
		}
		return oldJars;
	}

	private List<File> getJarsToCopy(Set<String> jarNames, File root) {
		List<File> jarsToCopy = new ArrayList<File>();
		Iterator<File> eclipseJars = Files.fileTreeTraverser().breadthFirstTraversal(root).filter(
				new Predicate<File>() {
					@Override
					public boolean apply(File file) {
						return file.getName().endsWith(".jar");
					}
				}).iterator();
		while (eclipseJars.hasNext()) {
			File next = eclipseJars.next();
			String name = next.getName();
			int i = name.indexOf("_");
			if (i <= 0)
				continue;
			if (jarNames.contains(name.substring(0, i))) {
				jarsToCopy.add(next);
			}
		}
		System.out.println("New jars (" + jarNames.size() + ") found in " + root.getAbsolutePath());
		for (File jarName : jarsToCopy) {
			System.out.println("\t" + jarName.getName());
		}
		return jarsToCopy;
	}

	private void copyJars(List<File> jarsToCopy, File to) {
		System.out.println("Copying to " + to.getAbsolutePath());
		to.mkdirs();
		for (File file : jarsToCopy) {
			File to1 = new File(to, file.getName());
			try {
				System.out.println("\t" + file.getName());
				Files.copy(file, to1);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
	}

}
