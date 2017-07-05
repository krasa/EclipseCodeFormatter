package krasa.formatter.plugin;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import krasa.formatter.eclipse.TestUtils;
import krasa.formatter.settings.Settings;
import krasa.formatter.settings.provider.ImportOrderProvider;
import krasa.formatter.utils.StringUtils;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author Vojtech Krasa
 */
public class ImportsSorter450Test {

	public static final String N = "\n";
	public static final List<String> DEFAULT_ORDER = Arrays.asList("java", "javax", "org", "com");

	@Test
	public void issue105() throws Exception {
		//@formatter:off
		String expected =
				"import importorder.example.root.BFoo;\n" +
				"import importorder.example.root.CFoo;\n" +
				"import importorder.example.root.A.AFoo;\n";

		String imports =
				"import importorder.example.root.BFoo;\n" +
				"import importorder.example.root.CFoo;\n" +
				"import importorder.example.root.A.AFoo;\n";

		//@formatter:on

		List<String> importsOrder = Arrays.asList("java", "javax", "org", "com");

		List<String> importsList = StringUtils.trimImports(imports);
		Collections.shuffle(importsList);
		List<String> strings = ImportsSorter450.sort(importsList, importsOrder);
		printAndAssert(expected, strings);
	}

	@Test
	public void issue105a() throws Exception {
		//@formatter:off
			String expected =
					"import importorder.example.root.AAAA;\n" +
					"import importorder.example.root.BFoo;\n" +
					"import importorder.example.root.DFoo;\n" +
					"import importorder.example.root.aaa;\n" +
					"import importorder.example.root.Aa.AFoo;\n" +
					"import importorder.example.root.Aaa.AaaFoo;\n" +
					"import importorder.example.root.Bb.bb;\n" +
					"import importorder.example.root.aB.Ab;\n" +
					"import importorder.example.root.ba.ba;\n" +
					"import importorder.example.root.bc.bc;\n";
	
			String imports =
					"import importorder.example.root.AAAA;\n" +
					"import importorder.example.root.BFoo;\n" +
					"import importorder.example.root.DFoo;\n" +
					"import importorder.example.root.aaa;\n" +
					"import importorder.example.root.Aa.AFoo;\n" +
					"import importorder.example.root.Aaa.AaaFoo;\n" +
					"import importorder.example.root.Bb.bb;\n" +
					"import importorder.example.root.aB.Ab;\n" +
					"import importorder.example.root.ba.ba;\n" +
					"import importorder.example.root.bc.bc;\n";
	
			//@formatter:on

		List<String> importsOrder = Arrays.asList("java", "javax", "org", "com");

		List<String> importsList = StringUtils.trimImports(imports);
		Collections.shuffle(importsList);
		List<String> strings = ImportsSorter450.sort(importsList, importsOrder);
		printAndAssert(expected, strings);
	}

	@Test
	public void testIssue() throws Exception {
		String expected = "import mockit.MockUp;\n" + "\n" + "import org.junit.Before;\n" + "\n"
				+ "import java.io.IOException;" + N;

		List<String> imports = Arrays.asList("mockit.MockUp", "org.junit.Before", "java.io.IOException");

		Settings settings = new Settings();
		settings.setImportOrderFromFile(true);
		setPath(settings, "resources/eclipse.importorder");
		List<String> importsOrder1 = new ImportOrderProvider(settings).get();
		List<String> strings = ImportsSorter450.sort(imports, importsOrder1);
		printAndAssert(expected, strings);
	}

	private void setPath(Settings settings, String importOrderConfigFilePath) {
		importOrderConfigFilePath = TestUtils.normalizeUnitTestPath(importOrderConfigFilePath);
		settings.setImportOrderConfigFilePath(importOrderConfigFilePath);
	}

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

		List<String> strings = ImportsSorter450.sort(imports, importsOrder);
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

		List<String> strings = ImportsSorter450.sort(StringUtils.trimImports(imports), importsOrder);
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

		List<String> imports1 = StringUtils.trimImports(imports);
		System.err.println(Arrays.toString(imports1.toArray()));
		List<String> strings = ImportsSorter450.sort(imports1, DEFAULT_ORDER);
		printAndAssert(expected, strings);
	}

	@Test
	public void test4() throws Exception {
		String expected = "import java.util.Arrays;\n" + "\n" + "import models.Deployment;\n"
				+ "import play.jobs.Job;\n" + "import play.mvc.Before;\n" + "import controllers.deadbolt.Restrict;\n";

		String imports = "import controllers.deadbolt.Restrict;\n" + "import java.util.Arrays;\n"
				+ "import play.mvc.Before;\n" + "import models.Deployment;\n" + "import play.jobs.Job;\n";

		List<String> imports1 = StringUtils.trimImports(imports);
		System.err.println(Arrays.toString(imports1.toArray()));
		List<String> strings = ImportsSorter450.sort(imports1, DEFAULT_ORDER);
		printAndAssert(expected, strings);
	}

	@Test
	public void test4a() throws Exception {
		String expected = "import java.util.Arrays;\n" + "\n" + "import models.Deployment;\n"
				+ "import play.jobs.Job;\n" + "import play.mvc.Before;\n" + "\n"
				+ "import comtrollers.deadbolt.Restrict;\n";

		String imports = "import comtrollers.deadbolt.Restrict;\n" + "import java.util.Arrays;\n"
				+ "import play.mvc.Before;\n" + "import models.Deployment;\n" + "import play.jobs.Job;\n";

		List<String> imports1 = StringUtils.trimImports(imports);
		System.err.println(Arrays.toString(imports1.toArray()));
		List<String> strings = ImportsSorter450.sort(imports1, DEFAULT_ORDER);
		printAndAssert(expected, strings);
	}

	@Test
	public void test4b() throws Exception {
		String expected = "import java.util.Arrays;\n" + "\n" + "import javax.Javax;\n" + "\n"
				+ "import models.Deployment;\n" + "import play.jobs.Job;\n" + "import play.mvc.Before;\n" + "\n"
				+ "import comtrollers.deadbolt.Restrict;\n" + "\n" + "import controllers.deadbolt.Restricts;\n";

		String imports = "\n" + "import java.util.Arrays;\n" + "import models.Deployment;\n" + "import play.jobs.Job;\n"
				+ "import play.mvc.Before;\n" + "import javax.Javax;\n" + "import comtrollers.deadbolt.Restrict;\n"
				+ "import controllers.deadbolt.Restricts;\n";

		List<String> imports1 = StringUtils.trimImports(imports);
		System.err.println(Arrays.toString(imports1.toArray()));
		List<String> strings = ImportsSorter450.sort(imports1, DEFAULT_ORDER);
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

		List<String> imports1 = StringUtils.trimImports(imports);
		System.err.println(Arrays.toString(imports1.toArray()));
		List<String> strings = ImportsSorter450.sort(imports1, importsOrder);
		printAndAssert(expected, strings);
	}

//	@Test
//	public void test6() throws Exception {
//	String document = "package jobs;\n" + "\n" + "import models.Album;\n" + "import models.Picture;\n"
//	+ "import org.apache.commons.lang.StringUtils;\n" + "import org.apache.http.HttpEntity;\n"
//	+ "import org.apache.http.HttpResponse;\n" + "import org.apache.http.HttpStatus;\n"
//	+ "import org.apache.http.client.methods.HttpGet;\n"
//	+ "import org.apache.http.impl.client.DefaultHttpClient;\n"
//	+ "import org.apache.http.params.CoreConnectionPNames;\n"
//	+ "import org.apache.http.util.EntityUtils;\n" + "import org.jsoup.Jsoup;\n"
//	+ "import org.jsoup.nodes.Document;\n" + "import org.jsoup.nodes.Element;\n"
//	+ "import org.jsoup.select.Elements;\n" + "import play.Logger;\n" + "import play.db.jpa.JPA;\n"
//	+ "import play.jobs.Job;\n" + "import play.libs.Codec;\n" + "import play.mvc.Router;\n"
//	+ "import play.vfs.VirtualFile;\n" + "import utils.BaseX;\n" + "import utils.UpYunUtils;\n" + "\n"
//	+ "import java.io.File;\n" + "import java.io.FileOutputStream;\n" + "import java.io.IOException;\n"
//	+ "import java.io.InputStream;\n" + "import java.math.BigInteger;\n" + "\n" + "/**\n"
//	+ " * User: divxer Date: 12-6-4 Time: 上午12:17\n" + " */\n" + "// @Every(\"7h\")\n"
//	+ "// @OnApplicationStart(async=true)\n" + "public class Picture4493Crawler extends Job {\n\n}";
//	
//	List<String> importsOrder = Collections.emptyList();
//	ImportSorterAdapter importSorter = new ImportSorterAdapter(importsOrder);
//	MockDocument document1 = new MockDocument(document);
//	importSorter.sortImports(document1, psiFile);
//	System.err.println(document1.getText());
//	}

	@Test
	public void test9() throws Exception {
		String expected = "import android.content.Context;\n" + "import android.view.LayoutInflater;\n"
				+ "import android.view.View;\n" + "import android.widget.TextView;\n";

		String imports = "\n" + "import android.widget.TextView;\n" + "import android.view.LayoutInflater;\n"
				+ "import android.view.View;\n" + "import android.content.Context;";

		List<String> imports1 = StringUtils.trimImports(imports);
		System.err.println(Arrays.toString(imports1.toArray()));
		List<String> strings = ImportsSorter450.sort(imports1, DEFAULT_ORDER);
		printAndAssert(expected, strings);
	}

	@Test
	public void test7() throws Exception {
		String imports = "import java.util.Calendar;";

		String expected = "import java.util.Calendar;\n";

		List<String> importsOrder = Arrays.asList("com", "java", "javax", "org");

		List<String> imports1 = StringUtils.trimImports(imports);
		System.err.println(Arrays.toString(imports1.toArray()));
		List<String> strings = ImportsSorter450.sort(imports1, importsOrder);
		printAndAssert(expected, strings);
	}

	@Test
	public void test10() throws Exception {
		String imports = "\n" + "import static org.junit.Assert.assertSame;\n" + "import org.junit.Test;";

		String expected = "import org.junit.Test;\n" + "\n" + "import static org.junit.Assert.assertSame;\n";

		List<String> importsOrder = Arrays.asList("", "\\#");

		List<String> imports1 = StringUtils.trimImports(imports);
		System.err.println(Arrays.toString(imports1.toArray()));
		List<String> strings = ImportsSorter450.sort(imports1, importsOrder);
		printAndAssert(expected, strings);
	}

	@Test
	public void test11() throws Exception {
		String imports = "import static org.junit.Assert.assertSame;\n" + "import org.junit.Test;";

		String expected = "import static org.junit.Assert.assertSame;\n" + "\n" + "import org.junit.Test;\n";

		List<String> importsOrder = Arrays.asList("\\#", "");

		List<String> imports1 = StringUtils.trimImports(imports);
		System.err.println(Arrays.toString(imports1.toArray()));
		List<String> strings = ImportsSorter450.sort(imports1, importsOrder);
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

		List<String> imports1 = StringUtils.trimImports(imports);
		System.err.println(Arrays.toString(imports1.toArray()));
		List<String> strings = ImportsSorter450.sort(imports1, importsOrder);
		printAndAssert(expected, strings);
	}

	@Test
	public void test13() throws Exception {
		String imports = "import static foo.JettyStart.startJetty;\n" + "import org.apache.commons.lang3.ArrayUtils;\n"
				+ "import static foo.Tomcat7Start.startTomcat;\n";

		String expected = "import org.apache.commons.lang3.ArrayUtils;\n" + "\n"
				+ "import static foo.JettyStart.startJetty;\n" + "\n" + "import static foo.Tomcat7Start.startTomcat;\n";

		List<String> importsOrder = Arrays.asList("", "\\#foo.Tomcat7Start");

		List<String> imports1 = StringUtils.trimImports(imports);
		System.err.println(Arrays.toString(imports1.toArray()));
		List<String> strings = ImportsSorter450.sort(imports1, importsOrder);
		printAndAssert(expected, strings);
	}

	@Test
	public void test8() throws Exception {
		String imports = "";

		String expected = "";

		List<String> imports1 = StringUtils.trimImports(imports);
		System.err.println(Arrays.toString(imports1.toArray()));
		List<String> strings = ImportsSorter450.sort(imports1, DEFAULT_ORDER);
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
