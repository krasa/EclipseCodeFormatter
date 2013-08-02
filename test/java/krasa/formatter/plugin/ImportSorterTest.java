package krasa.formatter.plugin;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import krasa.formatter.utils.StringUtils;

import org.junit.Assert;
import org.junit.Test;

import com.intellij.mock.MockDocument;

/**
 * @author Vojtech Krasa
 */
public class ImportSorterTest {

	public static final String N = "\n";
	public static final List<String> DEFAULT_ORDER = Arrays.asList("java", "javax", "org", "com");

	@Test
	public void test() throws Exception {
		String expected = "import java.util.HashMap;\n" + "import java.util.Map;\n" + "\n"
				+ "import org.jingle.mocquer.MockControl;\n" + N + "import sun.security.action.GetLongAction;\n"
				+ "import tmplatform.authorisation.ApiClientLink;\n"
				+ "import tmplatform.comms.common.caaa.EvasLdapInterfaceProfileWrapper;\n"
				+ "import base.LoadUnitTestDataTestCase;\n" + N + "import com.sun.rmi.rmid.ExecOptionPermission;" + N;

		List<String> imports = Arrays.asList("java.util.HashMap",
				"tmplatform.comms.common.caaa.EvasLdapInterfaceProfileWrapper",
				"com.sun.rmi.rmid.ExecOptionPermission", "java.util.Map", "base.LoadUnitTestDataTestCase",
				"org.jingle.mocquer.MockControl", "tmplatform.authorisation.ApiClientLink",
				"sun.security.action.GetLongAction");

		List<String> importsOrder = Arrays.asList("java", "javax", "org", "com");
		ImportSorter importSorter = new ImportSorter(importsOrder);
		List<String> strings = importSorter.sortByEclipseStandard(imports);
		printAndAssert(expected, strings);
	}

	@Test
	public void test2() throws Exception {
		String expected = "import static com.MyUtil.kuk;\n" + "import static org.junit.Assert.assertNotNull;\n"
				+ "import static tmutil.StringUtil.replaceText;\n\n" + "import java.util.HashMap;\n"
				+ "import java.util.Map;\n" + "\n" + "import org.jingle.mocquer.MockControl;\n" + N
				+ "import sun.security.action.GetLongAction;\n" + "import tmplatform.authorisation.ApiClientLink;\n"
				+ "import tmplatform.comms.common.caaa.EvasLdapInterfaceProfileWrapper;\n"
				+ "import base.LoadUnitTestDataTestCase;\n";

		String imports = "import static com.MyUtil.kuk;\n" + "import java.util.Map;\n" + "\n"
				+ "import static org.junit.Assert.assertNotNull;\n"
				+ "import tmplatform.authorisation.ApiClientLink;\n" + "import java.util.HashMap;\n"
				+ "import sun.security.action.GetLongAction;\n" + "import org.jingle.mocquer.MockControl;\n"
				+ "import tmplatform.comms.common.caaa.EvasLdapInterfaceProfileWrapper;\n" + N
				+ "import static tmutil.StringUtil.replaceText;\n\n" + "import base.LoadUnitTestDataTestCase;\n";

		List<String> importsOrder = Arrays.asList("java", "javax", "org", "com");
		ImportSorter importSorter = new ImportSorter(importsOrder);
		List<String> strings = importSorter.sortByEclipseStandard(StringUtils.trimImport(imports));
		printAndAssert(expected, strings);
	}

	@Test
	public void test3() throws Exception {
		String expected = "import static com.MyUtil.kuk;\n" + "import static org.junit.Assert.assertNotNull;\n"
				+ "import static tmutil.StringUtil.replaceText;\n\n" + "import java.util.ArrayList;\n" + "\n"
				+ "import org.w3c.dom.DOMConfiguration;\n" + "import org.w3c.dom.DOMException;\n"
				+ "import org.w3c.dom.Document;\n" + "import org.w3c.dom.Node;\n"
				+ "import org.w3c.dom.ls.LSException;\n" + "import org.w3c.dom.ls.LSInput;\n"
				+ "import org.w3c.dom.ls.LSParser;\n" + "import org.w3c.dom.ls.LSParserFilter;\n"
				+ "import org.xml.sax.InputSource;\n";

		String imports = "import org.w3c.dom.DOMConfiguration;\n" + "import org.w3c.dom.DOMException;\n"
				+ "import org.w3c.dom.Document;\n" + "import org.w3c.dom.Node;\n"
				+ "import org.w3c.dom.ls.LSException;\n" + "import org.w3c.dom.ls.LSInput;\n"
				+ "import static com.MyUtil.kuk;\n" + "import static org.junit.Assert.assertNotNull;\n"
				+ "import static tmutil.StringUtil.replaceText;\n" + "import org.w3c.dom.ls.LSParser;\n"
				+ "import org.w3c.dom.ls.LSParserFilter;\n" + "import org.xml.sax.InputSource;\n" + N
				+ "import java.util.ArrayList;";

		ImportSorter importSorter = new ImportSorter(DEFAULT_ORDER);

		List<String> imports1 = StringUtils.trimImport(imports);
		System.err.println(Arrays.toString(imports1.toArray()));
		List<String> strings = importSorter.sortByEclipseStandard(imports1);
		printAndAssert(expected, strings);
	}

	@Test
	public void test4() throws Exception {
		String expected = "import java.util.Arrays;\n" + "\n" + "import models.Deployment;\n"
				+ "import play.jobs.Job;\n" + "import play.mvc.Before;\n" + "import controllers.deadbolt.Restrict;\n";

		String imports = "import controllers.deadbolt.Restrict;\n" + "import java.util.Arrays;\n"
				+ "import play.mvc.Before;\n" + "import models.Deployment;\n" + "import play.jobs.Job;\n";

		ImportSorter importSorter = new ImportSorter(DEFAULT_ORDER);

		List<String> imports1 = StringUtils.trimImport(imports);
		System.err.println(Arrays.toString(imports1.toArray()));
		List<String> strings = importSorter.sortByEclipseStandard(imports1);
		printAndAssert(expected, strings);
	}

	@Test
	public void test5() throws Exception {
		String expected = "import static java.lang.Integer.numberOfLeadingZeros;\n"
				+ "import static java.lang.Integer.valueOf;\n" + "\n" + "import java.sql.Date;\n"
				+ "import java.util.List;\n" + "import java.util.Map;\n"
				+ "import javax.xml.crypto.dsig.spec.HMACParameterSpec;\n" + "import org.w3c.dom.Text;\n"
				+ "import org.w3c.dom.stylesheets.StyleSheetList;\n";

		String imports = "import javax.xml.crypto.dsig.spec.HMACParameterSpec;\n" + "import org.w3c.dom.Text;\n"
				+ "import java.util.List;\n" + "import static java.lang.Integer.numberOfLeadingZeros;\n"
				+ "import java.sql.Date;\n" + "\n" + "import static java.lang.Integer.valueOf;\n"
				+ "import java.util.Map;\n" + "import org.w3c.dom.stylesheets.StyleSheetList;\n";

		List<String> importsOrder = Collections.emptyList();
		ImportSorter importSorter = new ImportSorter(importsOrder);

		List<String> imports1 = StringUtils.trimImport(imports);
		System.err.println(Arrays.toString(imports1.toArray()));
		List<String> strings = importSorter.sortByEclipseStandard(imports1);
		printAndAssert(expected, strings);
	}

	@Test
	public void test6() throws Exception {
		String document = "package jobs;\n" + "\n" + "import models.Album;\n" + "import models.Picture;\n"
				+ "import org.apache.commons.lang.StringUtils;\n" + "import org.apache.http.HttpEntity;\n"
				+ "import org.apache.http.HttpResponse;\n" + "import org.apache.http.HttpStatus;\n"
				+ "import org.apache.http.client.methods.HttpGet;\n"
				+ "import org.apache.http.impl.client.DefaultHttpClient;\n"
				+ "import org.apache.http.params.CoreConnectionPNames;\n"
				+ "import org.apache.http.util.EntityUtils;\n" + "import org.jsoup.Jsoup;\n"
				+ "import org.jsoup.nodes.Document;\n" + "import org.jsoup.nodes.Element;\n"
				+ "import org.jsoup.select.Elements;\n" + "import play.Logger;\n" + "import play.db.jpa.JPA;\n"
				+ "import play.jobs.Job;\n" + "import play.libs.Codec;\n" + "import play.mvc.Router;\n"
				+ "import play.vfs.VirtualFile;\n" + "import utils.BaseX;\n" + "import utils.UpYunUtils;\n" + "\n"
				+ "import java.io.File;\n" + "import java.io.FileOutputStream;\n" + "import java.io.IOException;\n"
				+ "import java.io.InputStream;\n" + "import java.math.BigInteger;\n" + "\n" + "/**\n"
				+ " * User: divxer Date: 12-6-4 Time: 上午12:17\n" + " */\n" + "// @Every(\"7h\")\n"
				+ "// @OnApplicationStart(async=true)\n" + "public class Picture4493Crawler extends Job {\n\n}";

		List<String> importsOrder = Collections.emptyList();
		ImportSorter importSorter = new ImportSorter(importsOrder);
		MockDocument document1 = new MockDocument(document);
		importSorter.sortImports(document1);
		System.err.println(document1.getText());
	}

	@Test
	public void test9() throws Exception {
		String expected = "import android.content.Context;\n" + "import android.view.LayoutInflater;\n"
				+ "import android.view.View;\n" + "import android.widget.TextView;\n";

		String imports = "\n" + "import android.widget.TextView;\n" + "import android.view.LayoutInflater;\n"
				+ "import android.view.View;\n" + "import android.content.Context;";

		ImportSorter importSorter = new ImportSorter(DEFAULT_ORDER);

		List<String> imports1 = StringUtils.trimImport(imports);
		System.err.println(Arrays.toString(imports1.toArray()));
		List<String> strings = importSorter.sortByEclipseStandard(imports1);
		printAndAssert(expected, strings);
	}

	@Test
	public void test7() throws Exception {
		String imports = "import java.util.Calendar;";

		String expected = "import java.util.Calendar;\n";

		List<String> importsOrder = Arrays.asList("com", "java", "javax", "org");
		ImportSorter importSorter = new ImportSorter(importsOrder);

		List<String> imports1 = StringUtils.trimImport(imports);
		System.err.println(Arrays.toString(imports1.toArray()));
		List<String> strings = importSorter.sortByEclipseStandard(imports1);
		printAndAssert(expected, strings);
	}

	@Test
	public void test10() throws Exception {
		String imports = "\n" + "import static org.junit.Assert.assertSame;\n" + "import org.junit.Test;";

		String expected = "import org.junit.Test;\n" + "\n" + "import static org.junit.Assert.assertSame;\n";

		List<String> importsOrder = Arrays.asList("", "\\#");
		ImportSorter importSorter = new ImportSorter(importsOrder);

		List<String> imports1 = StringUtils.trimImport(imports);
		System.err.println(Arrays.toString(imports1.toArray()));
		List<String> strings = importSorter.sortByEclipseStandard(imports1);
		printAndAssert(expected, strings);
	}

	@Test
	public void test11() throws Exception {
		String imports = "import static org.junit.Assert.assertSame;\n" + "import org.junit.Test;";

		String expected = "import static org.junit.Assert.assertSame;\n" + "\n" + "import org.junit.Test;\n";

		List<String> importsOrder = Arrays.asList("\\#", "");
		ImportSorter importSorter = new ImportSorter(importsOrder);

		List<String> imports1 = StringUtils.trimImport(imports);
		System.err.println(Arrays.toString(imports1.toArray()));
		List<String> strings = importSorter.sortByEclipseStandard(imports1);
		printAndAssert(expected, strings);
	}

	@Test
	public void test12() throws Exception {
		String imports = "import static foo.JettyStart.startJetty;\n" + "import org.apache.commons.lang3.ArrayUtils;\n"
				+ "import static foo.Tomcat7Start.startTomcat;\n";

		String expected = "import static foo.JettyStart.startJetty;\n" + "\n"
				+ "import org.apache.commons.lang3.ArrayUtils;\n" + "\n"
				+ "import static foo.Tomcat7Start.startTomcat;\n";

		List<String> importsOrder = Arrays.asList("\\#", "", "\\#foo.Tomcat7Start");
		ImportSorter importSorter = new ImportSorter(importsOrder);

		List<String> imports1 = StringUtils.trimImport(imports);
		System.err.println(Arrays.toString(imports1.toArray()));
		List<String> strings = importSorter.sortByEclipseStandard(imports1);
		printAndAssert(expected, strings);
	}

	@Test
	public void test13() throws Exception {
		String imports = "import static foo.JettyStart.startJetty;\n" + "import org.apache.commons.lang3.ArrayUtils;\n"
				+ "import static foo.Tomcat7Start.startTomcat;\n";

		String expected = "import org.apache.commons.lang3.ArrayUtils;\n" + "\n"
				+ "import static foo.JettyStart.startJetty;\n" + "\n" + "import static foo.Tomcat7Start.startTomcat;\n";

		List<String> importsOrder = Arrays.asList("", "\\#foo.Tomcat7Start");
		ImportSorter importSorter = new ImportSorter(importsOrder);

		List<String> imports1 = StringUtils.trimImport(imports);
		System.err.println(Arrays.toString(imports1.toArray()));
		List<String> strings = importSorter.sortByEclipseStandard(imports1);
		printAndAssert(expected, strings);
	}

	@Test
	public void test8() throws Exception {
		String imports = "";

		String expected = "";

		ImportSorter importSorter = new ImportSorter(DEFAULT_ORDER);

		List<String> imports1 = StringUtils.trimImport(imports);
		System.err.println(Arrays.toString(imports1.toArray()));
		List<String> strings = importSorter.sortByEclipseStandard(imports1);
		printAndAssert(expected, strings);
	}

	private void printAndAssert(String expected, List<String> strings) {
		StringBuilder stringBuilder = print(strings);
		System.out.println("-----expected------");
		System.out.println(expected);
		Assert.assertEquals(expected, stringBuilder.toString());
		System.out.println("-----------------");

	}

	private StringBuilder print(List<String> strings) {
		StringBuilder stringBuilder = new StringBuilder();
		for (String string : strings) {
			stringBuilder.append(string);
		}

		System.out.println(stringBuilder.toString());
		return stringBuilder;
	}
}
