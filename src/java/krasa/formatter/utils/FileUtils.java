package krasa.formatter.utils;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.fileTypes.StdFileTypes;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.vfs.ReadonlyStatusHandler;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import krasa.formatter.exception.FileDoesNotExistsException;
import krasa.formatter.exception.ParsingFailedException;
import krasa.formatter.plugin.InvalidPropertyFile;
import org.apache.commons.io.IOUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Properties;

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

	public static boolean isWholeFile(Collection<TextRange> textRanges, String text) {
		for (TextRange textRange : textRanges) {
			if (isWholeFile(textRange.getStartOffset(), textRange.getEndOffset(), text)) {
				return true;
			}
		}
		return false;
	}

	public static boolean isWholeFile(int startOffset, int endOffset, String text) {
		return startOffset == 0 && endOffset == text.length();
	}

	public static boolean isJava(PsiFile psiFile) {
		return StdFileTypes.JAVA.equals(psiFile.getFileType());
	}

	public static Properties readPropertiesFile(@NotNull File file, @Nullable Properties defaultConfig) {
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

	public static Properties readPropertiesFile(@NotNull File file) {
		return readPropertiesFile(file, null);
	}

	public static Properties readXmlJavaSettingsFile(File file, Properties properties, String profile) {
		if (!file.exists()) {
			throw new FileDoesNotExistsException(file);
		}
		try {
			return readXmlJavaSettingsFile(org.apache.commons.io.FileUtils.readFileToString(file, "UTF-8"), properties, profile);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public static Properties readXmlJavaSettingsFile(String xml, Properties properties, String profile) {
		int defaultSize = properties.size();
		boolean profileFound = false;
		try {

			// load file profiles
			org.w3c.dom.Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(IOUtils.toInputStream(xml));
			doc.getDocumentElement().normalize();

			NodeList profiles = doc.getElementsByTagName("profile");
			if (profiles.getLength() == 0) {
				throw new IllegalStateException("Loading of profile settings failed, the file does not contain any profiles.");
			}
			if (profiles.getLength() > 1 && profile == null) {
				throw new IllegalStateException("No Eclipse formatter profile selected, go to settings and properly configure it.");
			}
			for (int temp = 0; temp < profiles.getLength(); temp++) {
				Node profileNode = profiles.item(temp);
				if (profileNode.getNodeType() == Node.ELEMENT_NODE) {
					Element profileElement = (Element) profileNode;
					String name = profileElement.getAttribute("name");
					if ((profile != null && profile.equals(name)) || profiles.getLength() == 1) {
						profileFound = true;
						NodeList childNodes = profileElement.getElementsByTagName("setting");
						if (childNodes.getLength() == 0) {
							throw new IllegalStateException("Loading of profile settings failed, the profile has no settings elements.");
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
			if (!profileFound) {
				throw new IllegalStateException("profile not found in the xml: " + xml);
			}
			if (properties.size() == defaultSize) {
				throw new IllegalStateException("no properties loaded, something is broken, xml:" + xml);
			}
		} catch (Exception e) {
			LOG.warn("xml: " + xml + ", profile: " + profile, e);
			throw new InvalidPropertyFile(e.getMessage(), e);
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

	public static List<String> getProfileNamesFromConfigXML(InputStream s) throws ParsingFailedException {
		List<String> profileNames = new ArrayList<String>();
		try { // load file profiles
			// delete eclipse dependency to fix java.lang.ClassCastException:
			// org.apache.xerces.jaxp.DocumentBuilderFactoryImpl cannot be cast to
			// javax.xml.parsers.DocumentBuilderFactory

			org.w3c.dom.Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(s);
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

		return profileNames;
	}

	@NotNull
	public static Properties convertEPF(Properties properties, Properties defaultConfig) {
		int beginIndex = "/instance/org.eclipse.jdt.core/".length();
		Properties result = new Properties(defaultConfig);
		for (Object object : properties.keySet()) {
			String key = (String) object;
			if (key.startsWith("/instance/org.eclipse.jdt.core/org.eclipse.jdt.core.formatter")) {
				String value = properties.getProperty(key);
				result.put(key.substring(beginIndex), value);
			}
		}
		return result;
	}
}
