package krasa.formatter.plugin;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;

import krasa.formatter.settings.Settings;

import com.intellij.openapi.editor.Document;
import krasa.formatter.utils.StringUtils;

/**
 * @author Vojtech Krasa
 */
public class ImportSorter {
	public static final int START_INDEX_OF_IMPORTS_PACKAGE_DECLARATION = 7;
	public static final String N = Settings.LINE_SEPARATOR;

	private List<String> importsOrder;

	public ImportSorter(List<String> importsOrder) {
		this.importsOrder = new ArrayList<String>(importsOrder);
	}

	public void sortImports(Document document) {
		String documentText = document.getText();
		// parse file
		Scanner scanner = new Scanner(documentText);
		int firstImportLine = 0;
		int lastImportLine = 0;
		int line = 0;
		List<String> imports = new ArrayList<String>();
		while (scanner.hasNext()) {
			line++;
			String next = scanner.nextLine();
			if (next == null) {
				break;
			}
			if (next.startsWith("import ")) {
				int i = next.indexOf(".");
				if (isNotValidImport(i)) {
					continue;
				}
				if (firstImportLine == 0) {
					firstImportLine = line;
				}
				lastImportLine = line;
				int endIndex = next.indexOf(";");
				imports.add(next.substring(START_INDEX_OF_IMPORTS_PACKAGE_DECLARATION,
						endIndex != -1 ? endIndex : next.length()));
			}
		}

		List<String> sortedImports = sortByEclipseStandard(imports);
		applyImportsToDocument(document, firstImportLine, lastImportLine, sortedImports);
	}

	private void applyImportsToDocument(final Document document, int firstImportLine, int lastImportLine,
			List<String> strings) {
		Scanner scanner;
		boolean importsAlreadyAppended = false;
		scanner = new Scanner(document.getText());
		int curentLine = 0;
		final StringBuilder sb = new StringBuilder();
		while (scanner.hasNext()) {
			curentLine++;
			String next = scanner.nextLine();
			if (next == null) {
				break;
			}
			if (curentLine >= firstImportLine && curentLine <= lastImportLine) {
				if (!importsAlreadyAppended) {
					for (String string : strings) {
						sb.append(string);
					}
				}
				importsAlreadyAppended = true;
			} else {
				append(sb, next);
			}
		}
		document.setText(sb.toString());
	}

	private void append(StringBuilder sb, String next) {
		sb.append(next);
		sb.append(Settings.LINE_SEPARATOR);
	}

	private boolean isNotValidImport(int i) {
		return i <= START_INDEX_OF_IMPORTS_PACKAGE_DECLARATION;
	}
	protected List<String> sortByEclipseStandard(List<String> imports) {
		ImportsTemplate importsTemplate = new ImportsTemplate( importsOrder);
		importsTemplate.filterMatchingImports(imports);
		importsTemplate.mergeNotMatchingItems(false);
		importsTemplate.mergeNotMatchingItems(true);
		importsTemplate.mergeMatchingItems();

		return importsTemplate.getResult();
	}

		
}
