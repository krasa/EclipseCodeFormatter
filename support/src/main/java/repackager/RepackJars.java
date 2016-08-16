package repackager;

import static java.util.jar.Pack200.Packer.*;

import java.io.*;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;
import java.util.jar.Pack200;

import org.apache.commons.io.FileUtils;

import com.google.common.io.Files;

@SuppressWarnings("Duplicates")
public class RepackJars {
	static StringBuilder log = new StringBuilder();

	public static void main(String[] args) throws IOException, InterruptedException {
		// new RepackJars().execute(rootFile("lib/eclipse44"));
		// new RepackJars().execute(rootFile("lib/eclipse"));
		new RepackJars().execute(rootFile("support/eclipseLibs/lib"));
		System.out.println("-----------------");
		System.out.println("-----------------");
		System.out.println("-----------------");
		System.out.println(log);
	}

	private static File rootFile(String s) throws IOException {
		File file = new File(s);
		if (file.exists()) {
			return file;
		}
		file = new File("../", s);
		if (file.exists()) {
			return file;
		}
		throw new RuntimeException(file.getCanonicalPath() + " does not exists");
	}

	private void execute(File sourceDir) throws IOException, InterruptedException {
		List<File> files = getJars(sourceDir);
		// List<File> files = Arrays.asList(new
		// File("F:\\workspace\\_projekty\\Github\\EclipseCodeFormatter4\\lib\\eclipse\\org.eclipse.jdt.core_3.11.1.v20150902-1521.jar"));
		File tempDir = new File(sourceDir, "temp");
		tempDir.mkdir();

		for (File jar : files) {
			File destJar = new File(tempDir, jar.getName());
//
			removeCrap(jar, destJar, new Condition<JarEntry>() {
				@Override
				public boolean isCrap(JarEntry entry) {
					return !entry.getName().startsWith("org") && !entry.getName().startsWith("com");
				}
			});
			repack(destJar);
			moveJar(destJar, jar);
			System.out.println("-----------------");
		}

	}

	private void moveJar(File from, File to) throws IOException {
		print("moving " + from.getCanonicalPath() + " to " + to.getCanonicalPath());
		if (!to.delete()) {
			throw new RuntimeException("delete failed: " + to.getCanonicalPath()+ " exists:"+to.exists());
		}
		FileUtils.copyFile(from, to);
		from.delete();
	}

	private interface Condition<T> {
		boolean isCrap(T var1);
	}

	private void removeCrap(File srcJarFile, File dest, Condition<JarEntry> condition) throws IOException {
		print("removing crap from " + srcJarFile);
		File tmpJarFile = File.createTempFile("tempJar", ".tmp");
		tmpJarFile.deleteOnExit();
		JarFile jarFile = new JarFile(srcJarFile);
		boolean jarUpdated = false;

		try {
			JarOutputStream tempJarOutputStream = new JarOutputStream(new FileOutputStream(tmpJarFile));

			try {
				Enumeration jarEntries = jarFile.entries();
				while (jarEntries.hasMoreElements()) {
					JarEntry entry = (JarEntry) jarEntries.nextElement();
					if (condition.isCrap(entry)) {
						System.out.println("\t\tremoving crap: " + entry.getName());
						continue;
					}
					InputStream entryInputStream = jarFile.getInputStream(entry);
					tempJarOutputStream.putNextEntry(entry);
					byte[] buffer = new byte[1024];
					int bytesRead = 0;
					while ((bytesRead = entryInputStream.read(buffer)) != -1) {
						tempJarOutputStream.write(buffer, 0, bytesRead);
					}
				}

				jarUpdated = true;
			} catch (Exception ex) {
				throw new RuntimeException(ex);
			} finally {
				tempJarOutputStream.close();
			}

		} finally {
			jarFile.close();

			if (!jarUpdated) {
				tmpJarFile.delete();
			}
		}

		if (jarUpdated) {
			dest.delete();
			tmpJarFile.renameTo(dest);
			print("\tcrap removed: " + dest.getName() + " (" + size(dest) + ", original " + size(srcJarFile) + ")");
		} else {
			throw new RuntimeException(srcJarFile.getCanonicalPath() + " not updated.");
		}
	}

	private void print(String s) {
		log.append(s).append("\n");
		System.out.println(s);
	}

	private List<File> getJars(File dir) throws IOException {
		print("searching for jars in " + dir.getCanonicalPath());
		List<File> files = new ArrayList<File>();
		for (File next : Files.fileTreeTraverser().children(dir)) {
			String name = next.getName();
			if (name.startsWith("adapter")) {
				continue;
			}
			if (name.endsWith(".jar") || name.endsWith(".zip")) {
				files.add(next);
			}
		}
		return files;
	}

	private void repack(File file) throws IOException {
		try {
			print("repackaging:   " + file.getName());
			Pack200Utils.normalize(file, properties());
			print("\tcompressed:   " + file.getName() + " (" + size(file) + ")");
		} catch (IOException ioe) {
			throw new RuntimeException(ioe);
		}

	}

	private String size(File file) {
		double bytes = file.length();
		long kilobytes = (long) (bytes / 1024);
		return kilobytes + " kb";
	}

	private Map<String, String> properties() {
		// Create the Packer object
		Pack200.Packer packer = Pack200.newPacker();
		Map<String, String> p = packer.properties();
		p.put(Pack200.Packer.EFFORT, "9"); // default is "5"
		p.put(Pack200.Packer.SEGMENT_LIMIT, "-1");
		p.put(Pack200.Packer.KEEP_FILE_ORDER, Pack200.Packer.FALSE);
		p.put(Pack200.Packer.MODIFICATION_TIME, Pack200.Packer.LATEST);
		p.put(Pack200.Packer.DEFLATE_HINT, Pack200.Packer.TRUE); // compression enabled
		// p.put("com.sun.java.util.jar.pack.verbose", Pack200.Packer.FALSE);
		// p.put("com.sun.java.util.jar.pack.nolog", Pack200.Packer.TRUE);
		String[] attributes = { UNKNOWN_ATTRIBUTE, CLASS_ATTRIBUTE_PFX, FIELD_ATTRIBUTE_PFX, METHOD_ATTRIBUTE_PFX,
				CODE_ATTRIBUTE_PFX, };
		String[] stripCodeAttributes = { "SourceFile", "LineNumberTable", "LocalVariableTable", "Deprecated" };
		for (String attribute : attributes) {
			for (String attributeName : stripCodeAttributes) {
				p.put(attribute + attributeName, Pack200.Packer.STRIP);
			}
		}
		p.put(Pack200.Packer.UNKNOWN_ATTRIBUTE, Pack200.Packer.STRIP);
		return p;
	}

	/*
	 * Copyright 2001-2016 The Apache Software Foundation.
	 *
	 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance
	 * with the License. You may obtain a copy of the License at
	 *
	 * http://www.apache.org/licenses/LICENSE-2.0
	 *
	 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
	 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for
	 * the specific language governing permissions and limitations under the License.
	 */
	public static class Pack200Utils {
		private Pack200Utils() {
		}

		public static void normalize(File jar) throws IOException {
			normalize(jar, jar, (Map) null);
		}

		public static void normalize(File jar, Map<String, String> props) throws IOException {
			normalize(jar, jar, props);
		}

		public static void normalize(File from, File to) throws IOException {
			normalize(from, to, (Map) null);
		}

		public static void normalize(File from, File to, Map<String, String> props) throws IOException {
			if (props == null) {
				props = new HashMap();
			}

			((Map) props).put("pack.segment.limit", "-1");
			File f = File.createTempFile("commons-compress", "pack200normalize");
			f.deleteOnExit();

			try {
				Object os = new FileOutputStream(f);
				Object j = null;

				try {
					Pack200.Packer p = Pack200.newPacker();
					p.properties().putAll((Map) props);
					p.pack(new JarFile(from), (OutputStream) os);
					j = null;
					((OutputStream) os).close();
					os = null;
					Pack200.Unpacker u = Pack200.newUnpacker();
					os = new JarOutputStream(new FileOutputStream(to));
					u.unpack(f, (JarOutputStream) os);
				} finally {
					if (j != null) {
						((JarFile) j).close();
					}

					if (os != null) {
						((OutputStream) os).close();
					}

				}
			} finally {
				f.delete();
			}

		}
	}
}