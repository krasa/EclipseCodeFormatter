package krasa.formatter.adapter;

import java.io.File;
import java.io.IOException;
import java.util.*;

import org.jetbrains.annotations.NotNull;

import com.google.common.base.Predicate;
import com.google.common.io.Files;

public class UpdateLibs {
	public static void main(String[] args) {
		String to = "lib/eclipse45";
		String from = "F:\\workspace\\eclipse-jee-mars-1-win32X\\eclipse";

		Set<String> jarNames = getJarsToupdate(to);
		for (String jarName : jarNames) {
			System.out.println(jarName);
		}

		List<File> jarsToCopy = getJarsToCopy(from, jarNames);

		copyJars(to, jarsToCopy);
	}

	private static void copyJars(String to, List<File> jarsToCopy) {
		for (File file : jarsToCopy) {
			File to1 = new File(to, file.getName());
			try {
				System.out.println("copying " + file.getName());
				Files.copy(file, to1);
			} catch (IOException e) {
				e.printStackTrace();
				continue;
			}
		}
	}

	@NotNull
	private static List<File> getJarsToCopy(String from, Set<String> jarNames) {
		List<File> jarsToCopy = new ArrayList<File>();
		Iterator<File> eclipseJars = Files.fileTreeTraverser().breadthFirstTraversal(new File(from)).filter(
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
		return jarsToCopy;
	}

	@NotNull
	private static Set<String> getJarsToupdate(String to) {
		Set<String> jarNames = new HashSet<String>();
		Iterator<File> iterator = Files.fileTreeTraverser().children(new File(to)).iterator();
		while (iterator.hasNext()) {
			File next = iterator.next();
			String name = next.getName();
			if (name.endsWith(".jar")) {
				int i = name.indexOf("_");
				if (i <= 0)
					continue;
				jarNames.add(name.substring(0, i));
			}
		}
		return jarNames;
	}

}
