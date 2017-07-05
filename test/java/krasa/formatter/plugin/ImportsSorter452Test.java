package krasa.formatter.plugin;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import krasa.formatter.settings.Settings;
import krasa.formatter.settings.provider.ImportOrderProvider;
import krasa.formatter.utils.StringUtils;

import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import com.intellij.psi.PsiClass;

/**
 * @author Vojtech Krasa
 */
public class ImportsSorter452Test {

	public static final String N = "\n";
	public static final List<String> DEFAULT_ORDER = Arrays.asList("java", "javax", "org", "com");

	static List<String> sort(List<String> imports, List<String> importsOrder) {
		ImportsSorter452 importsSorter = new ImportsSorter452(importsOrder, new ImportsComparator() {
			@Override
			protected PsiClass getPsiClass(String qualified) {
				// todo testing import of inner classes
				return null;
			}
		});
		return importsSorter.sort(imports);
	}

	@Ignore
	@Test
	public void issue107() throws Exception {
		//@formatter:off
		String expected =
				"import com.model.Ethernet;\n" +
				"import com.model.Ethernet.B;\n" +
				"import com.model.Ethernet.PortS;\n" +
				"import com.model.Ethernet.PortT;\n" +
				"import com.model.Id;\n";

		String imports =
				"import com.model.Ethernet;\n" +
				"import com.model.Id;\n" +
				"import com.model.Ethernet.B;\n" +
				"import com.model.Ethernet.PortS;\n" +
				"import com.model.Ethernet.PortT;\n\n";

		//@formatter:on

		List<String> importsOrder = DEFAULT_ORDER;

		List<String> importsList = StringUtils.trimImports(imports);
		Collections.shuffle(importsList);
		List<String> strings = sort(importsList, importsOrder);
		printAndAssert(expected, strings);
	}

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

		List<String> importsOrder = Arrays.asList();

		List<String> importsList = StringUtils.trimImports(imports);
		Collections.shuffle(importsList);
		List<String> strings = sort(importsList, importsOrder);
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

		List<String> importsOrder = Arrays.asList();

		List<String> importsList = StringUtils.trimImports(imports);
		Collections.shuffle(importsList);
		List<String> strings = sort(importsList, importsOrder);
		printAndAssert(expected, strings);
	}

	@Test
	public void issue104() throws Exception {
		//@formatter:off
		String expected =
		"import com.mycorp.DateUtils;\n" +
		"\n" +
		"import static com.google.common.base.MoreObjects.firstNonNull;\n";

		String imports =
				"import static com.google.common.base.MoreObjects.firstNonNull;\n"+
		"import com.mycorp.DateUtils;\n" ;

		//@formatter:on

		List<String> importsOrder = Arrays.asList("com.mycorp", "\\#com.mycorp", "com", "\\#com");

		List<String> importsList = StringUtils.trimImports(imports);
		Collections.shuffle(importsList);
		List<String> strings = sort(importsList, importsOrder);
		printAndAssert(expected, strings);
	}

	@Test
	public void issue104a() throws Exception {
		//@formatter:off
		String expected =
				"import com.mycorp.Foo;\n" +
						"\n" +
						"import static com.mycorp.Foo.foo;\n" +
						"\n" +
						"import com.google.bar;\n" +
						"\n" +
						"import static com.google.bar.bar;\n";

		String imports =
				"import static com.google.bar.bar;\n" +
				"import com.google.bar;\n" +
				"import static com.mycorp.Foo.foo;\n" +
				"import com.mycorp.Foo;\n" +
						"";

		//@formatter:on


		List<String> importsList = StringUtils.trimImports(imports);
		Collections.shuffle(importsList);

		Settings settings = new Settings();
		File file = FileUtils.getFile("resources/issue104.importorder");
		setPath(settings, file);
		ImportOrderProvider importOrderProvider = new ImportOrderProvider(settings);

		List<String> strings = sort(importsList, importOrderProvider.get());
		printAndAssert(expected, strings);
	}

	private void setPath(Settings settings, File file) {
		if (!file.exists()) {
			file = new File("test/" + file.getPath());
		}
		settings.setImportOrderConfigFilePath(file.getAbsolutePath());
	}

	@Test
	public void test452() throws Exception {

		//@formatter:off
		String expected =
				"import static aaa.XAAA.aaa;\n" +
						"import static com.google.common.collect.Multimaps.forMap;\n" +
						"import static foo.bar.XFOO.foo;\n" +
						"\n" +
						"import static org.easymock.EasyMock.createControl;\n" +
						"\n" +
						"import static java.util.Collections.EMPTY_LIST;\n" +
						"\n" +
						"import java.util.ArrayList;\n" +
						"import java.util.HashMap;\n" +
						"import java.util.List;\n" +
						"\n" +
						"import org.easymock.IMocksControl;\n" +
						"\n" +
						"import com.google.common.collect.SetMultimap;\n" +
						"\n" +
						"import aaa.XAAA;\n" +
						"import foo.bar.XFOO;\n";

		String imports =
				"\n" +
						"import static aaa.XAAA.aaa;\n" +
						"import static com.google.common.collect.Multimaps.forMap;\n" +
						"import static foo.bar.XFOO.foo;\n" +
						"import static org.easymock.EasyMock.createControl;\n" +
						"import static java.util.Collections.EMPTY_LIST;\n" +
						"import java.util.ArrayList;\n" +
						"import java.util.HashMap;\n" +
						"import java.util.List;\n" +
						"import org.easymock.IMocksControl;\n" +
						"import com.google.common.collect.SetMultimap;\n" +
						"import aaa.XAAA;\n" +
						"import foo.bar.XFOO;\n";

		//@formatter:on

		List<String> importsOrder = Arrays.asList("\\#org", "\\#java", "java", "javax", "org", "com");

		List<String> importsList = StringUtils.trimImports(imports);
		Collections.shuffle(importsList);
		List<String> strings = sort(importsList, importsOrder);
		printAndAssert(expected, strings);
	}

	@Test
	public void test2() throws Exception {
		//@formatter:off
		String expected =
				"import static org.easymock.EasyMock.createControl;\n" +
						"\n" +
						"import java.util.ArrayList;\n" +
						"import java.util.HashMap;\n" +
						"import java.util.List;\n" +
						"\n" +
						"import org.easymock.IMocksControl;\n" +
						"\n" +
						"import com.google.common.collect.SetMultimap;\n" +
						"\n" +
						"import aaa.XAAA;\n" +
						"import foo.bar.XFOO;\n" +
						"\n" +
						"import static aaa.XAAA.aaa;\n" +
						"import static com.google.common.collect.Multimaps.forMap;\n" +
						"import static foo.bar.XFOO.foo;\n";

		String imports =
				"import static org.easymock.EasyMock.createControl;\n" +
						"\n" +
						"import java.util.ArrayList;\n" +
						"import java.util.HashMap;\n" +
						"import java.util.List;\n" +
						"\n" +
						"import org.easymock.IMocksControl;\n" +
						"\n" +
						"import com.google.common.collect.SetMultimap;\n" +
						"\n" +
						"import aaa.XAAA;\n" +
						"import foo.bar.XFOO;\n" +
						"\n" +
						"import static aaa.XAAA.aaa;\n" +
						"import static com.google.common.collect.Multimaps.forMap;\n" +
						"import static foo.bar.XFOO.foo;\n";
		//@formatter:on
		List<String> importsOrder = Arrays.asList("\\#org", "\\#java", "java", "javax", "org", "com", "", "\\#");


		List<String> importsList = StringUtils.trimImports(imports);
		Collections.shuffle(importsList);
		List<String> strings = sort(importsList, importsOrder);
		printAndAssert(expected, strings);
	}

	@Test
	public void test3a() throws Exception {
		//@formatter:off
		String expected = "import static aaa.XAAA.aaa;\n"
				+ "import static com.google.common.collect.Multimaps.forMap;\n" + "import static foo.bar.XFOO.foo;\n"
				+ "import static org.easymock.EasyMock.createControl;\n" + "\n" + "import java.util.ArrayList;\n"
				+ "import java.util.HashMap;\n" + "import java.util.List;\n" + "\n"
				+ "import org.easymock.IMocksControl;\n" + "\n" + "import com.google.common.collect.SetMultimap;\n"
				+ "\n" + "import aaa.XAAA;\n" + "import foo.bar.XFOO;\n";

		String imports = "import static org.easymock.EasyMock.createControl;\n" + "\n" + "import java.util.ArrayList;\n"
				+ "import java.util.HashMap;\n" + "import java.util.List;\n" + "\n"
				+ "import org.easymock.IMocksControl;\n" + "\n" + "import com.google.common.collect.SetMultimap;\n"
				+ "\n" + "import aaa.XAAA;\n" + "import foo.bar.XFOO;\n" + "\n" + "import static aaa.XAAA.aaa;\n"
				+ "import static com.google.common.collect.Multimaps.forMap;\n" + "import static foo.bar.XFOO.foo;\n";
		//@formatter:on
		List<String> importsOrder = Arrays.asList("java", "javax", "org", "com");

		List<String> importsList = StringUtils.trimImports(imports);
		Collections.shuffle(importsList);
		List<String> strings = sort(importsList, importsOrder);
		printAndAssert(expected, strings);
	}

	@Test
	public void test3b() throws Exception {
		//@formatter:off
			String expected = "import static aaa.XAAA.aaa;\n"
				+ "import static com.google.common.collect.Multimaps.forMap;\n" + "import static foo.bar.XFOO.foo;\n"
				+ "import static org.easymock.EasyMock.createControl;\n" + "\n" + "import aaa.XAAA;\n"
				+ "import com.google.common.collect.SetMultimap;\n" + "import foo.bar.XFOO;\n"
				+ "import java.util.ArrayList;\n" + "import java.util.HashMap;\n" + "import java.util.List;\n"
				+ "import org.easymock.IMocksControl;\n";

		String imports = "import static org.easymock.EasyMock.createControl;\n" + "\n" + "import java.util.ArrayList;\n"
				+ "import java.util.HashMap;\n" + "import java.util.List;\n" + "\n"
				+ "import org.easymock.IMocksControl;\n" + "\n" + "import com.google.common.collect.SetMultimap;\n"
				+ "\n" + "import aaa.XAAA;\n" + "import foo.bar.XFOO;\n" + "\n" + "import static aaa.XAAA.aaa;\n"
				+ "import static com.google.common.collect.Multimaps.forMap;\n" + "import static foo.bar.XFOO.foo;\n";
		//@formatter:on
		List<String> importsOrder = Arrays.asList();

		List<String> importsList = StringUtils.trimImports(imports);
		Collections.shuffle(importsList);
		List<String> strings = sort(importsList, importsOrder);
		printAndAssert(expected, strings);
	}

	@Test
	public void test3() throws Exception {
		//@formatter:off	
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
		List<String> strings = sort(imports1, DEFAULT_ORDER);
		printAndAssert(expected, strings);
	}

	@Test
	public void test() 	
throws Exception {
		//@formatter:off
		String expected = "import java.util.HashMap;\n" +
				"import java.util.Map;\n" +
				"\n" +
				"import org.jingle.mocquer.MockControl;\n" +
				"\n" +
				"import com.sun.rmi.rmid.ExecOptionPermission;\n" +
				"\n" +
				"import base.LoadUnitTestDataTestCase;\n" +
				"import sun.security.action.GetLongAction;\n" +
				"import tmplatform.authorisation.ApiClientLink;\n" +
				"import tmplatform.comms.common.caaa.EvasLdapInterfaceProfileWrapper;\n";

		List<String> imports = Arrays.asList("java.util.HashMap",
				"tmplatform.comms.common.caaa.EvasLdapInterfaceProfileWrapper",
				"com.sun.rmi.rmid.ExecOptionPermission", "java.util.Map", "base.LoadUnitTestDataTestCase",
				"org.jingle.mocquer.MockControl", "tmplatform.authorisation.ApiClientLink",
				"sun.security.action.GetLongAction");
		//@formatter:on

		List<String> importsOrder = Arrays.asList("java", "javax", "org", "com");

		List<String> strings = sort(imports, importsOrder);
		printAndAssert(expected, strings);
	}

	@Test
	public void test4() throws Exception {
		//@formatter:off
		String expected = "import java.util.Arrays;\n" +
                "\n" +
                "import comtrollers.deadbolt.Restrict;\n" +
                "import models.Deployment;\n" +
                "import play.jobs.Job;\n" +
                "import play.mvc.Before;\n";

		String imports = "import comtrollers.deadbolt.Restrict;\n" + "import java.util.Arrays;\n"
				+ "import play.mvc.Before;\n" + "import models.Deployment;\n" + "import play.jobs.Job;\n";

		List<String> imports1 = StringUtils.trimImports(imports);
		System.err.println(Arrays.toString(imports1.toArray()));
		List<String> strings = sort(imports1, DEFAULT_ORDER);
		printAndAssert(expected, strings);
	}
	@Test
		public void test4b() throws Exception {
			String expected = 
					"import java.util.Arrays;\n" +
					"\n" +
					"import comtrollers.deadbolt.Restrict;\n" +
					"import controllers.deadbolt.Restricts;\n" +
					"import models.Deployment;\n" +
					"import play.jobs.Job;\n" +
					"import play.mvc.Before;\n";
	
			String imports = 
					"import java.util.Arrays;\n" +
					"import comtrollers.deadbolt.Restrict;\n" +
					"import controllers.deadbolt.Restricts;\n" +
					"import models.Deployment;\n" +
					"import play.jobs.Job;\n" +
					"import play.mvc.Before;\n";
	
			List<String> imports1 = StringUtils.trimImports(imports);
			System.err.println(Arrays.toString(imports1.toArray()));
			List<String> strings = sort(imports1, DEFAULT_ORDER);
			printAndAssert(expected, strings);
		}
	@Test
	public void test5()		
			 throws Exception {
		//@formatter:off
		String expected = "import static java.lang.Integer.numberOfLeadingZeros;\n"
				+ "import static java.lang.Integer.valueOf;\n" + "\n" + "import java.sql.Date;\n"
				+ "import java.util.List;\n" + "import java.util.Map;\n"
				+ "import javax.xml.crypto.dsig.spec.HMACParameterSpec;\n" + "import org.w3c.dom.Text;\n"
				+ "import org.w3c.dom.stylesheets.StyleSheetList;\n";

		String imports = "import javax.xml.crypto.dsig.spec.HMACParameterSpec;\n" + "import org.w3c.dom.Text;\n"
				+ "import java.util.List;\n" + "import static java.lang.Integer.numberOfLeadingZeros;\n"
				+ "import java.sql.Date;\n" + "\n" + "import static java.lang.Integer.valueOf;\n"
				+ "import java.util.Map;\n" + "import org.w3c.dom.stylesheets.StyleSheetList;\n";
		//@formatter:on

		List<String> importsOrder = Collections.emptyList();

		List<String> imports1 = StringUtils.trimImports(imports);
		System.err.println(Arrays.toString(imports1.toArray()));
		List<String> strings = sort(imports1, importsOrder);
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
	//			//@formatter:on

//	List<String> importsOrder = Collections.emptyList();
//	ImportSorterAdapter importSorter = new ImportSorterAdapter(importsOrder);
//	MockDocument document1 = new MockDocument(document);
//	importSorter.sortImports(document1, psiFile);
//	System.err.println(document1.getText());
//	}

	@Test
	public void test9() throws Exception {
		//@formatter:off	
		String expected = "import android.content.Context;\n" + "import android.view.LayoutInflater;\n"
				+ "import android.view.View;\n" + "import android.widget.TextView;\n";

		String imports = "\n" + "import android.widget.TextView;\n" + "import android.view.LayoutInflater;\n"
				+ "import android.view.View;\n" + "import android.content.Context;";

		List<String> imports1 = StringUtils.trimImports(imports);
		System.err.println(Arrays.toString(imports1.toArray()));
		List<String> strings = sort(imports1, DEFAULT_ORDER);
		printAndAssert(expected, strings);
	}

	@Test
	public void test7() throws Exception {
		//@formatter:off
		String imports = "import java.util.Calendar;";

		String expected = "import java.util.Calendar;\n";
		//@formatter:on

		List<String> importsOrder = Arrays.asList("com", "java", "javax", "org");

		List<String> imports1 = StringUtils.trimImports(imports);
		System.err.println(Arrays.toString(imports1.toArray()));
		List<String> strings = sort(imports1, importsOrder);
		printAndAssert(expected, strings);
	}

	@Test
	public void test10() throws Exception {
		//@formatter:off
		String imports = "\n" + "import static org.junit.Assert.assertSame;\n" + "import org.junit.Test;";

		String expected = "import org.junit.Test;\n" + "\n" + "import static org.junit.Assert.assertSame;\n";
		//@formatter:on

		List<String> importsOrder = Arrays.asList("", "\\#");

		List<String> imports1 = StringUtils.trimImports(imports);
		System.err.println(Arrays.toString(imports1.toArray()));
		List<String> strings = sort(imports1, importsOrder);
		printAndAssert(expected, strings);
	}

	@Test
	public void test11() throws Exception {
		//@formatter:off
		String imports = "import static org.junit.Assert.assertSame;\n" + "import org.junit.Test;";

		String expected = "import static org.junit.Assert.assertSame;\n" + "\n" + "import org.junit.Test;\n";
		//@formatter:on

		List<String> importsOrder = Arrays.asList("\\#", "");

		List<String> imports1 = StringUtils.trimImports(imports);
		System.err.println(Arrays.toString(imports1.toArray()));
		List<String> strings = sort(imports1, importsOrder);
		printAndAssert(expected, strings);
	}

	@Test
	public void test12() throws Exception {
		//@formatter:off
		String imports = "import static foo.JettyStart.startJetty;\n" + "import org.apache.commons.lang3.ArrayUtils;\n"
				+ "import static foo.Tomcat7Start.startTomcat;\n";

		String expected = "import static foo.JettyStart.startJetty;\n" + "\n"
				+ "import org.apache.commons.lang3.ArrayUtils;\n" + "\n"
				+ "import static foo.Tomcat7Start.startTomcat;\n";
		//@formatter:on

		List<String> importsOrder = Arrays.asList("\\#", "", "\\#foo.Tomcat7Start");

		List<String> imports1 = StringUtils.trimImports(imports);
		System.err.println(Arrays.toString(imports1.toArray()));
		List<String> strings = sort(imports1, importsOrder);
		printAndAssert(expected, strings);
	}


	@Test
	public void test8() throws Exception {
		String imports = "";

		String expected = "";

		List<String> imports1 = StringUtils.trimImports(imports);
		System.err.println(Arrays.toString(imports1.toArray()));
		List<String> strings = sort(imports1, DEFAULT_ORDER);
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