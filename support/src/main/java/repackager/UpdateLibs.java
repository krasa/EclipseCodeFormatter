package repackager;

import java.io.File;
import java.io.IOException;
import java.util.*;

import com.google.common.io.Files;

public class UpdateLibs {


	public static void main(String[] args) throws IOException, InterruptedException {
		new UpdateLibs().run();

		RepackJars.main(args);
	}

	private void run() throws IOException {
		String from = "C:\\Users\\i7\\Downloads\\eclipse-jee-2019-06-R-win32-x86_64";
		File currentJars = new File("support/eclipseLibs/lib");
		File copyTo = new File(currentJars + "/temp");

		Map<String, String> oldJars = getJarsToUpdate(currentJars);

		List<File> jarsToCopy = getJarsToCopy(oldJars.keySet(), new File(from));

		copyJars(jarsToCopy, copyTo);

		replaceJars(currentJars, copyTo);

	}

	private void replaceJars(File currentJars, File copyTo) {
		File[] files = currentJars.listFiles();

		for (File file : copyTo.listFiles()) {
			String name = file.getName();
			int i = name.indexOf("_");
			String prefix = name.substring(0, i);

			deleteFilesWithPrefix(files, prefix);
			moveFile(file, currentJars);

		}
	}

	public boolean moveFile(File fileToMove, File targetFolder) {
		File to = new File(targetFolder, fileToMove.getName());
		boolean b = fileToMove.renameTo(to);
		if (!b) {
			throw new RuntimeException("File not moved: " + fileToMove);
		} else {
			System.out.println("file moved to: " + to);
		}
		return b;
	}

	private void deleteFilesWithPrefix(File[] files, String prefix) {
		for (File file : files) {
			if (file.getName().startsWith(prefix + "_")) {
				System.out.println("deleting " + file.getName());
				boolean delete = file.delete();
				if (!delete) {
					throw new RuntimeException("file not deleted " + file);
				}
			}
		}
	}


	private Map<String, String> getJarsToUpdate(File t) {
		Map<String, String> oldJars = new HashMap<String, String>();
		Iterator<File> iterator = Files.fileTraverser().breadthFirst(t).iterator();
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
		List<File> jars = new ArrayList<>();
		
		Iterator<File> iterator = Files.fileTraverser().breadthFirst(root).iterator();
		while (iterator.hasNext()) {
			File next = iterator.next();

			if (next.getName().endsWith(".jar")) {
				jars.add(next);
			}
		}

		for (File file : jars) {
			String name = file.getName();
			int i = name.indexOf("_");
			if (i <= 0)
				continue;
			if (jarNames.contains(name.substring(0, i))) {
				jarsToCopy.add(file);
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
