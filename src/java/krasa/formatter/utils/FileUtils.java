package krasa.formatter.utils;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.io.IOUtils;
import org.jetbrains.annotations.NotNull;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.fileTypes.StdFileTypes;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.ReadonlyStatusHandler;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;

import krasa.formatter.exception.FileDoesNotExistsException;
import krasa.formatter.exception.ParsingFailedException;
import krasa.formatter.plugin.InvalidPropertyFile;

/**
 * @author Vojtech Krasa
 */
public class FileUtils {
	private static final Logger LOG = Logger.getInstance(FileUtils.class.getName());

	public static boolean isWritable(PsiFile psiFile) {
		return isWritable(psiFile.getVirtualFile(), psiFile.getProject());
	}
	      
	public static boolean isWritable(@NotNull VirtualFile file, @NotNull Project project) {
		return !ReadonlyStatusHandler.getInstance(project).ensureFilesWritable(file).hasReadonlyFiles();
	}

	public static boolean isWholeFile(int startOffset, int endOffset, String text) {
		return startOffset == 0 && endOffset == text.length();
	}

	public static boolean isJavaScript(PsiFile psiFile) {
		FileType fileType = psiFile.getFileType();
		return StdFileTypes.JS.equals(fileType) || "JavaScript".equals(fileType.getName());
	}

	public static boolean isCpp(PsiFile psiFile) {
		String name = psiFile.getFileType().getName();
		return name.equals("C++") || name.equals("C/C++") || name.equals("ObjectiveC") ; // CLion calls it ObjectiveC... wtf
	}

	public static boolean isJava(PsiFile psiFile) {
		return StdFileTypes.JAVA.equals(psiFile.getFileType());
	}

	public static Properties readPropertiesFile(File file, Properties defaultConfig) {
		if (!file.exists()) {
			throw new FileDoesNotExistsException(file);

		}
		BufferedInputStream stream = null;
		final Properties formatterOptions;
		try {
			stream = new BufferedInputStream(new FileInputStream(file));
			formatterOptions = new Properties(defaultConfig);
			String s = IOUtils.toString(stream);
			StringReader reader = new StringReader(s.replace("=\\#", "=#"));
			formatterOptions.load(reader);
		} catch (IOException e) {
			throw new RuntimeException("config file read error", e);
		} finally {
			if (stream != null) {
				try {
					stream.close();
				} catch (IOException e) { /* ignore */
				}
			}
		}
		return formatterOptions;
	}

	public static Properties readPropertiesFile(File file) {
		return readPropertiesFile(file, null);
	}

	public static Properties readXmlJavaSettingsFile(File file, Properties properties, String profile) {
		int defaultSize = properties.size();
		if (!file.exists()) {
			throw new FileDoesNotExistsException(file);
		}
		if (profile == null) {
			throw new IllegalStateException("no profile selected, go to settings and select proper settings file");
		}
		boolean profileFound = false;
		try { // load file profiles
			org.w3c.dom.Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(file);
			doc.getDocumentElement().normalize();

			NodeList profiles = doc.getElementsByTagName("profile");
			if (profiles.getLength() == 0) {
				throw new IllegalStateException(
						"loading of profile settings failed, file does not contain any profiles");
			}
			for (int temp = 0; temp < profiles.getLength(); temp++) {
				Node profileNode = profiles.item(temp);
				if (profileNode.getNodeType() == Node.ELEMENT_NODE) {
					Element profileElement = (Element) profileNode;
					String name = profileElement.getAttribute("name");
					if (profile.equals(name)) {
						profileFound = true;
						NodeList childNodes = profileElement.getElementsByTagName("setting");
						if (childNodes.getLength() == 0) {
							throw new IllegalStateException(
									"loading of profile settings failed, profile has no settings elements");
						}
						for (int i = 0; i < childNodes.getLength(); i++) {
							Node item = childNodes.item(i);
							if (item.getNodeType() == Node.ELEMENT_NODE) {
								Element attributeItem = (Element) item;
								String id = attributeItem.getAttribute("id");
								String value = attributeItem.getAttribute("value");
								properties.setProperty(id.trim(), value.trim());
							}
						}
					}
				}
			}
		} catch (Exception e) {
			LOG.error("file: " + file.getAbsolutePath() + ", profile: " + profile, e);
			throw new InvalidPropertyFile(e.getMessage(), e);
		}
		if (!profileFound) {
			throw new IllegalStateException("profile not found in the file " + file.getAbsolutePath());
		}
		if (properties.size() == defaultSize) {
			throw new IllegalStateException("no properties loaded, something is broken, file:" + file.getAbsolutePath());
		}
		return properties;
	}

	public static List<String> getProfileNamesFromConfigXML(File file) throws ParsingFailedException {
		List<String> profileNames = new ArrayList<String>();
		if (file.exists()) {
			try { // load file profiles
				// delete eclipse dependency to fix java.lang.ClassCastException:
				// org.apache.xerces.jaxp.DocumentBuilderFactoryImpl cannot be cast to
				// javax.xml.parsers.DocumentBuilderFactory

				org.w3c.dom.Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(file);
				doc.getDocumentElement().normalize();

				NodeList nList = doc.getElementsByTagName("profile");
				for (int temp = 0; temp < nList.getLength(); temp++) {
					Node nNode = nList.item(temp);
					if (nNode.getNodeType() == Node.ELEMENT_NODE) {
						Element eElement = (Element) nNode;
						String name = eElement.getAttribute("name");
						profileNames.add(name);
					}
				}
			} catch (Exception e) {
				LOG.info(e);
				throw new ParsingFailedException(e);
			}

		} else {
			LOG.info("not existing file");
		}
		return profileNames;
	}

}
