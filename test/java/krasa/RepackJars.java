package krasa;

import com.google.common.io.Files;
import org.apache.commons.io.FileUtils;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;
import java.util.jar.Pack200;

import static java.util.jar.Pack200.Packer.*;

@SuppressWarnings("Duplicates")
public class RepackJars {
	private static final String SOURCE = "lib/eclipse45";
//	private static final String SOURCE = "lib/eclipse44";

	public static void main(String[] args) throws IOException, InterruptedException {
		new RepackJars().execute();
	}

	StringBuilder log = new StringBuilder();

	public void execute() throws IOException, InterruptedException {
		List<File> files = getJars(new File(SOURCE));
		// List<File> files = Arrays.asList(new
		// File("F:\\workspace\\_projekty\\Github\\EclipseCodeFormatter4\\lib\\eclipse45\\org.eclipse.jdt.core_3.11.1.v20150902-1521.jar"));

		try {
			for (File jar : files) {
				File destDir = new File(jar.getParent(), "temp");
				destDir.mkdir();
				File destJar = new File(destDir, jar.getName());

				removeCrap(jar, destJar);
				repack(destJar);
			}
		} finally {
			System.out.println("-----------------");
			System.out.println(log);
		}
	}

	public void removeCrap(File srcJarFile, File dest) throws IOException {
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
					if (!entry.getName().startsWith("org") && !entry.getName().startsWith("com") && !entry.getName().startsWith("krasa")) {
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
			FileUtils.deleteQuietly(dest);
			tmpJarFile.renameTo(dest);
			print("\tcrap removed: " + dest.getName() + " (" + size(dest) + ", original " + size(srcJarFile) + ")");
		} else {
			throw new RuntimeException(srcJarFile.getAbsolutePath() + " not updated.");
		}
	}

	private void print(String s) {
		log.append(s).append("\n");
		System.out.println(s);
	}

	@NotNull
	private List<File> getJars(File dir) {
		List<File> files = new ArrayList<File>();
		for (File next : Files.fileTreeTraverser().children(dir)) {
			String name = next.getName();
			if (name.endsWith(".jar") || name.endsWith(".zip")) {
				files.add(next);
			}
		}
		return files;
	}

	@NotNull
	private void repack(File file) throws IOException {
		try {
			Pack200Utils.normalize(file, properties());
			print("\tcompressed:   " + file.getName() + " (" + size(file) + ")");
		} catch (IOException ioe) {
			throw new RuntimeException(ioe);
		}

	}

	@NotNull
	private String size(File file) {
		double bytes = file.length();
		long kilobytes = (long) (bytes / 1024);
		return kilobytes + " kb";
	}

	@NotNull
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
		String[] attributes = {UNKNOWN_ATTRIBUTE, CLASS_ATTRIBUTE_PFX, FIELD_ATTRIBUTE_PFX, METHOD_ATTRIBUTE_PFX,
				CODE_ATTRIBUTE_PFX,};
		String[] stripCodeAttributes = {"SourceFile", "LineNumberTable", "LocalVariableTable", "Deprecated"};
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