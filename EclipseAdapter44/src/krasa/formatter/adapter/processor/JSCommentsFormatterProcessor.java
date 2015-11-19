package krasa.formatter.adapter.processor;

import java.util.ArrayList;

import krasa.formatter.adapter.JSCodeFormatterFacade;
import krasa.formatter.plugin.Range;
import krasa.formatter.processor.Processor;
import krasa.formatter.settings.Settings;
import krasa.formatter.utils.FileUtils;

import org.eclipse.jdt.core.formatter.IndentManipulation;
import org.eclipse.jface.text.*;
import org.eclipse.text.edits.MultiTextEdit;
import org.eclipse.text.edits.ReplaceEdit;
import org.eclipse.text.edits.TextEdit;
import org.eclipse.wst.jsdt.core.formatter.CodeFormatter;

import com.google.gwt.eclipse.core.editors.java.GWTPartitions;
import com.intellij.openapi.editor.Document;
import com.intellij.psi.PsiFile;

/**
 * @author Vojtech Krasa
 */
public class JSCommentsFormatterProcessor implements Processor {
	private Settings settings;
	private JSCodeFormatterFacade jsCodeFormatterFacade;

	public JSCommentsFormatterProcessor(Settings settings) {
		this.settings = settings;
		jsCodeFormatterFacade = new JSCodeFormatterFacade(settings.getJSProperties());
	}

	@Override
	public boolean process(Document documentIJ, PsiFile file, Range range) {
		if (FileUtils.isJavaScript(file) && settings.isEnableJSProcessor()) {
			try {
				String text = documentIJ.getText();
				IDocument document = new org.eclipse.jface.text.Document(text);

				TextEdit formatEdit = format(document, range);

				formatEdit.apply(document);
				documentIJ.setText(document.get());
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
		return true;
	}

	private TextEdit format(IDocument document, Range range) {
		TextEdit combinedEdit = new MultiTextEdit();
		ITypedRegion[] regions = computePartitioning(document, range);

		// Format all JSNI blocks in the document
		int i = 0;
		for (ITypedRegion region : regions) {
			if (region.getType().equals(GWTPartitions.JSNI_METHOD)) {
				TextEdit edit = format(document, new TypedPosition(region));
				if (edit != null) {
					combinedEdit.addChild(edit);
				}
				i++;
			}
		}
		return combinedEdit;

	}

	public TextEdit format(IDocument document, TypedPosition partition) {
		try {
			// Extract the JSNI block out of the document
			int offset = partition.getOffset();
			int length = partition.getLength();

			// Determine the line delimiter, indent string, and tab/indent widths
			String lineDelimiter = Settings.LINE_SEPARATOR;
			int tabWidth = IndentManipulation.getTabWidth(settings.getJSProperties().get());
			int indentWidth = IndentManipulation.getIndentWidth(settings.getJSProperties().get());

			// Get indentation level of the first line of the JSNI block (this should
			// be the line containing the JSNI method declaration)
			int methodDeclarationOffset = getMethodDeclarationOffset(document, offset);

			int jsniLine1 = document.getLineOfOffset(methodDeclarationOffset);
			int methodIndentLevel = getLineIndentLevel(document, jsniLine1, tabWidth, indentWidth);
			// String indentLine = defaultCodeFormatter.createIndentationString(methodIndentLevel);

			// Extract the JSNI body out of the block and split it up by line
			String jsniSource = document.get(offset, length);

			String formattedJs;

			TextEdit formatEdit = jsCodeFormatterFacade.format(CodeFormatter.K_JAVA_DOC, jsniSource, 0,
					jsniSource.length(), methodIndentLevel, lineDelimiter);

			org.eclipse.jface.text.Document d = new org.eclipse.jface.text.Document(jsniSource);
			formatEdit.apply(d);

			formattedJs = d.get();

			// if (!formattedJs.startsWith(lineDelimiter)) {
			// formattedJs = indentLine+formattedJs ;
			// formattedJs = lineDelimiter + formattedJs;
			// }
			//
			if (!formattedJs.endsWith(lineDelimiter)) {
				formattedJs = formattedJs + lineDelimiter;
			}

			// formattedJs = formattedJs + indentLine;

			return new ReplaceEdit(offset, length, formattedJs);

		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private static int getLineIndentLevel(IDocument document, int line, int tabWidth, int indentWidth)
			throws BadLocationException {
		int lineOffset = document.getLineOffset(line);
		return getLineIndentLevel(document.get(lineOffset, document.getLineLength(line)), tabWidth, indentWidth);
	}

	private static int getLineIndentLevel(String line, int tabWidth, int indentWidth) {
		return IndentManipulation.measureIndentUnits(line, tabWidth, indentWidth);
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

	private static ITypedRegion[] computePartitioning(IDocument document, Range range) {
		ArrayList<ITypedRegion> iTypedRegions = new ArrayList<ITypedRegion>();
		String str = document.get();
		String prefix = "/**";
		String postfix = "*/";
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
}
