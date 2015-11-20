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
package krasa.formatter.adapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import krasa.formatter.plugin.Range;
import krasa.formatter.settings.Settings;

import org.eclipse.jdt.core.formatter.IndentManipulation;
import org.eclipse.jdt.internal.formatter.DefaultCodeFormatter;
import org.eclipse.jface.text.*;
import org.eclipse.text.edits.MultiTextEdit;
import org.eclipse.text.edits.ReplaceEdit;
import org.eclipse.text.edits.TextEdit;
import org.eclipse.wst.jsdt.core.ToolFactory;
import org.eclipse.wst.jsdt.core.formatter.CodeFormatter;

import com.google.gwt.eclipse.core.editors.java.GWTPartitions;
import com.google.gwt.eclipse.core.validators.java.JsniParser;

/**
 * Utility methods for formatting JSNI methods. This is not a full-blown JavaScript pretty-printer, but it does apply
 * the correct outer indentation to JSNI blocks, to correct the JDT bug which slides them to the right.
 */
@SuppressWarnings("restriction")
public class JsniFormattingUtil {

	private static class JsniJavaRefReplacementResult {
		private String jsni;
		private Map<String, String> replacements;

		public JsniJavaRefReplacementResult(String jsni, Map<String, String> replacements) {
			this.jsni = jsni;
			this.replacements = replacements;
		}

		public String getJsni() {
			return jsni;
		}

		public Map<String, String> getReplacements() {
			return replacements;
		}
	}

	/**
	 * Returns a text edit that formats the given document according to the given settings.
	 *
	 * @param document
	 *            The document to format.
	 * @param javaFormattingPrefs
	 *            The formatting preferences for Java, used to determine the method level indentation.
	 * @param javaScriptFormattingPrefs
	 *            The formatting preferences for JavaScript. See org.eclipse.wst.jsdt.internal.formatter
	 *            .DefaultCodeFormatterOptions and org.eclipse.wst.jsdt.core.formatter.DefaultCodeFormatterConstants
	 * @param originalJsniMethods
	 *            The original jsni methods to use if the formatter fails to format the method. The original jsni
	 *            Strings must be in the same order that the jsni methods occur in the document. This is to work around
	 *            the Java formatter blasting the jsni tabbing for the format-on-save action. May be null.
	 * @return A text edit that when applied to the document, will format the jsni methods.
	 */
	@SuppressWarnings("unchecked")
	public TextEdit format(IDocument document, Map javaFormattingPrefs, Map javaScriptFormattingPrefs,  Range range) {
		TextEdit combinedEdit = new MultiTextEdit();
		ITypedRegion[] regions = computePartitioning(document, range);

		// Format all JSNI blocks in the document
		int i = 0;
		for (ITypedRegion region : regions) {
			if (region.getType().equals(GWTPartitions.JSNI_METHOD)) {
				String originalJsniMethod = null;
				TextEdit edit = format(document, new TypedPosition(region), javaFormattingPrefs,
						javaScriptFormattingPrefs, originalJsniMethod);
				if (edit != null) {
					combinedEdit.addChild(edit);
				}
				i++;
			}
		}
		return combinedEdit;

	}

	private static ITypedRegion[] computePartitioning(IDocument document, Range range) {
		ArrayList<ITypedRegion> iTypedRegions = new ArrayList<ITypedRegion>();
		String str = document.get();
		String prefix = "/*-";
		String postfix = "-*/";
		int startIndex = 0;
		int endIndex = 0;

		while (startIndex != -1) {
			startIndex = str.indexOf(prefix, startIndex);
			endIndex = str.indexOf(postfix, startIndex);

			if (startIndex != -1 && endIndex != -1) {
				endIndex = endIndex + 3;
				if (isInRange(range, startIndex, endIndex)) {
					iTypedRegions.add(new TypedRegion(startIndex, endIndex - startIndex, GWTPartitions.JSNI_METHOD));
				}
				startIndex += prefix.length();
			}
		}
		return iTypedRegions.toArray(new ITypedRegion[iTypedRegions.size()]);

	}

	private static boolean isInRange(Range range, int startIndex, int endIndex) {
		boolean b = range.getStartOffset() < startIndex && range.getEndOffset() > endIndex;
		boolean b1 = startIndex < range.getEndOffset() && range.getEndOffset() < endIndex;
		boolean b2 = startIndex < range.getStartOffset() && range.getStartOffset() < endIndex;
		return range.isWholeFile() || b || b1 || b2;
	}

	public TextEdit format(IDocument document, TypedPosition partition, Map<String, String> javaFormattingPrefs,
						   Map<String, String> javaScriptFormattingPrefs, String original) {
		try {
			// Extract the JSNI block out of the document
			int offset = partition.getOffset();
			int length = partition.getLength();

			// Determine the line delimiter, indent string, and tab/indent widths
			String lineDelimiter = Settings.LINE_SEPARATOR;
			int tabWidth = IndentManipulation.getTabWidth(javaFormattingPrefs);
			int indentWidth = IndentManipulation.getIndentWidth(javaFormattingPrefs);

			// Get indentation level of the first line of the JSNI block (this should
			// be the line containing the JSNI method declaration)
			int methodDeclarationOffset = getMethodDeclarationOffset(document, offset);

			int jsniLine1 = document.getLineOfOffset(methodDeclarationOffset);
			int methodIndentLevel = getLineIndentLevel(document, jsniLine1, tabWidth, indentWidth);
			DefaultCodeFormatter defaultCodeFormatter = new DefaultCodeFormatter(javaFormattingPrefs);
			String indentLine = defaultCodeFormatter.createIndentationString(methodIndentLevel);

			// Extract the JSNI body out of the block and split it up by line
			String jsniSource = document.get(offset, length);
			String body = JsniParser.extractMethodBody(jsniSource);

			String formattedJs;

			// JSNI Java references mess up the JS formatter, so replace them
			// with place holder values
			JsniJavaRefReplacementResult replacementResults = replaceJsniJavaRefs(body);
			body = replacementResults.getJsni();
			CodeFormatter codeFormatter = ToolFactory.createCodeFormatter(javaScriptFormattingPrefs);

			TextEdit formatEdit = codeFormatter.format(CodeFormatter.K_STATEMENTS, body, 0, body.length(),
					methodIndentLevel + 1, lineDelimiter);

			if (formatEdit != null) {

				body = restoreJsniJavaRefs(replacementResults);

				Document d = new Document(body);
				formatEdit.apply(d);

				formattedJs = d.get();

				if (!formattedJs.startsWith(lineDelimiter)) {
					formattedJs = lineDelimiter + formattedJs;
				}

				if (!formattedJs.endsWith(lineDelimiter)) {
					formattedJs = formattedJs + lineDelimiter;
				}

				formattedJs = formattedJs + indentLine;

				formattedJs = "/*-{" + formattedJs + "}-*/";

			} else {

				if (original == null) {
					return null;
				}

				formattedJs = original; // formatting failed, use the original string
			}

			return new ReplaceEdit(offset, length, formattedJs);

		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private static int getMethodDeclarationOffset(IDocument document, int offset) {
		int methodDeclarationOffset = offset;

		// jsniMethodWithDeclarationLineThatWraps
		String s = document.get();
		int i = s.lastIndexOf("\n", offset);
		String substring = s.substring(i + 1, offset);
		substring = substring.trim();
		if (!substring.isEmpty() && !substring.contains("(")) {
			methodDeclarationOffset = i - 2;
		}
		return methodDeclarationOffset;
	}

	private static int getLineIndentLevel(IDocument document, int line, int tabWidth, int indentWidth)
			throws BadLocationException {
		int lineOffset = document.getLineOffset(line);
		return getLineIndentLevel(document.get(lineOffset, document.getLineLength(line)), tabWidth, indentWidth);
	}

	private static int getLineIndentLevel(String line, int tabWidth, int indentWidth) {
		return IndentManipulation.measureIndentUnits(line, tabWidth, indentWidth);
	}

	private static String makeJsToken(String s) {
		int hashCode = s.hashCode();
		// js variable names can't have negative signs in them
		String jsToken = "_" + (hashCode < 0 ? "N" + Math.abs(hashCode) : hashCode);

		// pad the hash so it's the same length as the original reference so that
		// things like formatting line wrap works
		while (jsToken.length() < s.length()) {
			jsToken = jsToken + "_";
		}
		return jsToken;
	}

	private static JsniJavaRefReplacementResult replaceJsniJavaRefs(String jsni) {

		Map<String, String> replacements = new HashMap<String, String>();

		Pattern p = Pattern.compile("@[a-zA-Z0-9._$]+::[a-zA-Z0-9_$]+(\\(.*?\\)\\(.*?\\))?");

		Matcher m = p.matcher(jsni);

		while (m.find()) {
			int start = m.start();
			int end = m.end();

			String ref = jsni.substring(start, end);
			String jsToken = makeJsToken(ref);

			// if the map already contains the js token, and the token's original jsni
			// ref is not the one we've found, js-tokenize the token
			while (replacements.containsKey(jsToken) && !replacements.get(jsToken).equals(ref)) {
				jsToken = makeJsToken(jsToken);
			}

			replacements.put(jsToken, ref);
		}

		for (Entry<String, String> kvp : replacements.entrySet()) {
			jsni = jsni.replace(kvp.getValue(), kvp.getKey());
		}

		return new JsniJavaRefReplacementResult(jsni, replacements);
	}

	private static String restoreJsniJavaRefs(JsniJavaRefReplacementResult result) {

		String jsni = result.getJsni();
		for (Entry<String, String> kvp : result.getReplacements().entrySet()) {
			jsni = jsni.replace(kvp.getKey(), kvp.getValue());
		}

		return jsni;
	}
}
