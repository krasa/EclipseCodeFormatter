package repackager;

import java.io.File;
import java.io.IOException;
import java.util.*;

import org.apache.commons.io.FileUtils;

import com.google.common.base.Predicate;
import com.google.common.io.Files;

public class UpdateLibs {


	public static void main(String[] args) throws IOException {
		new UpdateLibs().run();
	}

	private void run() throws IOException {
		String from = "F:\\workspace\\eclipse-jee-neon-3-win32\\";
		File currentJars = new File("support/eclipseLibs/lib");
		File copyTo = new File(currentJars + "/temp");

		Map<String, String> oldJars = getJarsToUpdate(currentJars);

		List<File> jarsToCopy = getJarsToCopy(oldJars.keySet(), new File(from));

		copyJars(jarsToCopy, copyTo);

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
