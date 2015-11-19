package krasa.formatter.eclipse;

import java.util.HashMap;

import junit.framework.Assert;
import krasa.formatter.adapter.JsniFormattingUtil;

import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.TypedPosition;
import org.eclipse.text.edits.TextEdit;
import org.junit.Test;

/**
 * @author Vojtech Krasa
 */
public class GWTTest {
	public static final String INPUT = "package aaa.shared;\n"
			+ "\n"
			+ "import krasa.JavaScriptObject;\n"
			+ "\n"
			+ "public class FieldVerifier {\n"
			+ "\n"
			+ "\tprivate native JavaScriptObject jsInit() /*-{\tvar self = this;\t(function() {\talert(\"Hello\");\t})();\t}-*/;\n"
			+ "}";

	public static final String FORMATTED = "package aaa.shared;\n" + "\n" + "import krasa.JavaScriptObject;\n" + "\n"
			+ "public class FieldVerifier {\n" + "\n" + "\tprivate native JavaScriptObject jsInit() /*-{\n"
			+ "\t\tvar self = this;\n" + "\t\t(function() {\n" + "\t\t\talert(\"Hello\");\n" + "\t\t})();\n"
			+ "\t}-*/;\n" + "}";

	@Test
	public void testName() throws Exception {
		HashMap<String, String> javaFormattingPrefs = TestUtils.getJavaProperties();

		int i = INPUT.indexOf("/*-");
		int i2 = INPUT.indexOf("-*/");
		TypedPosition partition = new TypedPosition(i, i2 - i + 3, "");
		HashMap<String, String> jsMap = TestUtils.getJSProperties();

		IDocument document = new Document(INPUT);
		JsniFormattingUtil jsniFormattingUtil = new JsniFormattingUtil();
		TextEdit format1 = jsniFormattingUtil.format(document, partition, javaFormattingPrefs, jsMap, null);
		// TextEdit format1 = JsniFormattingUtil.format(document,javaFormattingPrefs, jsMap, null);
		format1.apply(document);
		Assert.assertEquals(FORMATTED, document.get());
		System.err.println(FORMATTED);
	}

}
