package krasa.formatter.eclipse;

import org.junit.Test;

import java.net.URL;
import java.util.List;

public class ConfigurableEclipseLocationTest {

	@Test
	public void run() {
		List<URL> urlList;
		urlList = new ConfigurableEclipseLocation().run("C:/Users/vojtisek/eclipse/java-2023-03");
//		urlList= new ConfigurableEclipseLocation().run("C:\\workspace\\eclipse-standard-kepler-R-macosx-cocoa");
//		urlList= new ConfigurableEclipseLocation().run("C:\\workspace\\eclipse-standard-kepler-R-linux-gtk");
//		urlList= new ConfigurableEclipseLocation().run("C:\\workspace\\eclipse-standard-kepler-R-win32");
//		List<URL> urlList = new ConfigurableEclipseLocation().run("C:\\workspace\\eclipse-jee-2020-06-R-win32-x86_64");
		for (URL jar : urlList) {
			System.out.println(jar);
		}
		urlList = new ConfigurableEclipseLocation().run("C:\\Users\\vojtisek\\eclipse\\java-2021-12");
//		urlList= new ConfigurableEclipseLocation().run("C:\\workspace\\eclipse-standard-kepler-R-macosx-cocoa");
//		urlList= new ConfigurableEclipseLocation().run("C:\\workspace\\eclipse-standard-kepler-R-linux-gtk");
//		urlList= new ConfigurableEclipseLocation().run("C:\\workspace\\eclipse-standard-kepler-R-win32");
//		List<URL> urlList = new ConfigurableEclipseLocation().run("C:\\workspace\\eclipse-jee-2020-06-R-win32-x86_64");
		for (URL jar : urlList) {
			System.out.println(jar);
		}
	}
}