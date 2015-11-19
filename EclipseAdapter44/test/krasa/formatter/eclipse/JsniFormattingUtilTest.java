/*******************************************************************************
 * Copyright 2011 Google Inc. All Rights Reserved.
 * <p/>
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package krasa.formatter.eclipse;

import java.util.Arrays;
import java.util.Map;

import krasa.formatter.adapter.JsniFormattingUtil;
import krasa.formatter.plugin.Range;

import org.apache.commons.lang.StringUtils;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.formatter.DefaultCodeFormatterConstants;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.text.edits.TextEdit;
import org.eclipse.wst.jsdt.core.JavaScriptCore;
import org.junit.Assert;
import org.junit.Test;

/**
 * Test cases for the {@link com.google.gwt.eclipse.core.editors.java.JsniFormattingUtil} class.
 */
public class JsniFormattingUtilTest {
	private String[] testClass;

	@Test
	public void testFormat() throws Exception {
		// Use GWT indentation settings
		Map javaPrefs = TestUtils.getJavaProperties();
		javaPrefs.put(DefaultCodeFormatterConstants.FORMATTER_INDENTATION_SIZE, "2");
		javaPrefs.put(DefaultCodeFormatterConstants.FORMATTER_TAB_CHAR, JavaCore.SPACE);
		javaPrefs.put(DefaultCodeFormatterConstants.FORMATTER_TAB_SIZE, "2");
		javaPrefs.put(DefaultCodeFormatterConstants.FORMATTER_BLANK_LINES_AT_BEGINNING_OF_METHOD_BODY, "0");

		Map javaScriptPrefs = TestUtils.getJSProperties();
		javaScriptPrefs.put(
				org.eclipse.wst.jsdt.core.formatter.DefaultCodeFormatterConstants.FORMATTER_INDENTATION_SIZE, "2");
		javaScriptPrefs.put(org.eclipse.wst.jsdt.core.formatter.DefaultCodeFormatterConstants.FORMATTER_TAB_CHAR,
				JavaScriptCore.SPACE);
		javaScriptPrefs.put(org.eclipse.wst.jsdt.core.formatter.DefaultCodeFormatterConstants.FORMATTER_TAB_SIZE, "2");
		javaScriptPrefs.put(
				org.eclipse.wst.jsdt.core.formatter.DefaultCodeFormatterConstants.FORMATTER_BLANK_LINES_AT_BEGINNING_OF_METHOD_BODY,
				"0");

		// Get the IDocument for the test class

		IDocument document = new Document(createString(getTestClasses()));

		// Apply the formatting and test the result
		Range range = new Range(0, document.get().length(), true);
		JsniFormattingUtil jsniFormattingUtil = new JsniFormattingUtil();
		TextEdit formatEdit = jsniFormattingUtil.format(document, javaPrefs, javaScriptPrefs, range);
		formatEdit.apply(document);
		Assert.assertEquals(getFormattedDocument(), document.get());
		System.err.println("---------------------------");
		System.err.println(getFormattedDocument());
		System.err.println("---------------------------");
		System.err.println(document.get());
	}

	protected String[] getTestClasses() {
		testClass = new String[]{"package com.hello.client;", "", "public class FormattingUtilTest {", "",
				"  private static native void jsniMethod()/*-{",
				"    var obj = @com.hello.client.FormattingUtilTest::new()();", "    var x = 777;", "",
				"    if (x == 777) {", "     x += 207;", "     alert(\"Hello!\");", "    }", "",
				"   var text = obj.@com.hello.client.FormattingUtilTest::toString()();", "  }-*/;", "",
				"  private static native void jsniMethodWithNoIndent()/*-{",
				"var obj = @com.hello.client.FormattingUtilTest::new()();", "var x = 777;", "", "if (x == 777) {",
				"  x += 207;", "  alert(\"Hello!\");", "}", "}-*/;", "",
				"  private static native void jsniMethodWithExtraIndent()/*-{",
				"        var obj = @com.hello.client.FormattingUtilTest::new()();", "        var x = 777;", "",
				"        if (x == 777) {", "             x += 207;", "          alert(\"Hello!\");", "        }", "",
				"        var text = obj.@com.hello.client.FormattingUtilTest::toString()();", "              }-*/;",
				"", "  private static native void jsniMethodWithStartTokenOnSeparateLine()", "  /*-{",
				"  var obj = @com.hello.client.FormattingUtilTest::new()();", "            var x = 777;", "    ",
				"  if (x == 777) {", "    x += 207;", "         alert(\"Hello!\");", "                   }", "",
				"  var text = obj.@com.hello.client.FormattingUtilTest::toString()();", "                    }-*/;",
				"", "  private static native void jsniMethodWithOuterBlankLines()/*-{", "", "",
				"    var obj = @com.hello.client.FormattingUtilTest::new()();", "    var x = 777;", "      ",
				"                                    if (x == 777) {", "      x += 207;", "      alert(\"Hello!\");",
				"    }", "    ", "            var text = obj.@com.hello.client.FormattingUtilTest::toString()();",
				"                                            ", "    ", "                               }-*/;", "",
				"  private static native void emptyJsniMethod()/*-{}-*/;", "",
				"  private static native void jsniMethodWithSpace() /*-{", "                   var x = 777;",
				"                       }-*/;", "",
				"  private static native void jsniMethodWithDeclarationLineThatWraps(Object o,",
				"      String s, int x)/*-{", "                       var x = 777;", "                       }-*/;",
				"", "}"};
		return testClass;
	}

	public static String createString(String[] lines) {
		return StringUtils.join(Arrays.asList(lines), "\n");
	}

	private String getFormattedDocument() {
		return createString(new String[]{"package com.hello.client;", "", "public class FormattingUtilTest {", "",
				"  private static native void jsniMethod()/*-{",
				"    var obj = @com.hello.client.FormattingUtilTest::new()();", "    var x = 777;", "",
				"    if (x == 777) {", "      x += 207;", "      alert(\"Hello!\");", "    }", "",
				"    var text = obj.@com.hello.client.FormattingUtilTest::toString()();", "  }-*/;", "",
				"  private static native void jsniMethodWithNoIndent()/*-{",
				"    var obj = @com.hello.client.FormattingUtilTest::new()();", "    var x = 777;", "",
				"    if (x == 777) {", "      x += 207;", "      alert(\"Hello!\");", "    }", "  }-*/;", "",
				"  private static native void jsniMethodWithExtraIndent()/*-{",
				"    var obj = @com.hello.client.FormattingUtilTest::new()();", "    var x = 777;", "",
				"    if (x == 777) {", "      x += 207;", "      alert(\"Hello!\");", "    }", "",
				"    var text = obj.@com.hello.client.FormattingUtilTest::toString()();", "  }-*/;", "",
				"  private static native void jsniMethodWithStartTokenOnSeparateLine()", "  /*-{",
				"    var obj = @com.hello.client.FormattingUtilTest::new()();", "    var x = 777;", "",
				"    if (x == 777) {", "      x += 207;", "      alert(\"Hello!\");", "    }", "",
				"    var text = obj.@com.hello.client.FormattingUtilTest::toString()();", "  }-*/;", "",
				"  private static native void jsniMethodWithOuterBlankLines()/*-{", "",
				"    var obj = @com.hello.client.FormattingUtilTest::new()();", "    var x = 777;", "",
				"    if (x == 777) {", "      x += 207;", "      alert(\"Hello!\");", "    }", "",
				"    var text = obj.@com.hello.client.FormattingUtilTest::toString()();", "", "  }-*/;", "",
				"  private static native void emptyJsniMethod()/*-{}-*/;", "",
				"  private static native void jsniMethodWithSpace() /*-{", "    var x = 777;", "  }-*/;", "",
				"  private static native void jsniMethodWithDeclarationLineThatWraps(Object o,",
				"      String s, int x)/*-{", "    var x = 777;", "  }-*/;", "", "}"});
	}

}
