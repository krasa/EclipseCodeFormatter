package krasa.formatter.plugin;

import com.intellij.openapi.fileTypes.StdFileTypes;
import com.intellij.psi.*;
import krasa.formatter.settings.Settings;
import krasa.formatter.utils.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author Vojtech Krasa
 */
public class ImportSorterAdapter {
	public static final String N = Settings.LINE_SEPARATOR;

	private Settings.ImportOrdering importOrdering;
	private List<String> importsOrder;

	public ImportSorterAdapter(Settings.ImportOrdering importOrdering, List<String> importsOrder) {
		this.importOrdering = importOrdering;
		this.importsOrder = new ArrayList<String>(importsOrder);
	}

	public String getImportsOrderAsString() {
		return Arrays.toString(importsOrder.toArray());
	}

	public void sortImports(PsiJavaFile file) {
		List<String> imports = new ArrayList<String>();
		List<PsiElement> nonImports = new ArrayList<PsiElement>();

		PsiImportList importList = file.getImportList();
		if (importList == null) {
			return;
		}

		PsiElement[] children = importList.getChildren();
		for (int i = 0; i < children.length; i++) {
			PsiElement child = children[i];
			if (child instanceof PsiImportStatementBase) {
				imports.add(child.getText());
			} else if (!(child instanceof PsiWhiteSpace)) { //todo wild guess
				nonImports.add(child);
			}
		}

		List<String> sort = getImportsSorter().sort(StringUtils.trimImports(imports));

		StringBuilder text = new StringBuilder();
		for (int i = 0; i < sort.size(); i++) {
			text.append(sort.get(i));
		}
		for (int i = 0; i < nonImports.size(); i++) {
			PsiElement psiElement = nonImports.get(i);
			text.append("\n").append(psiElement.getText());
		}

		PsiFileFactory factory = PsiFileFactory.getInstance(file.getProject());
		String ext = StdFileTypes.JAVA.getDefaultExtension();
		final PsiJavaFile dummyFile = (PsiJavaFile) factory.createFileFromText("_Dummy_." + ext, StdFileTypes.JAVA,
				text);

		PsiImportList newImportList = dummyFile.getImportList();
		PsiImportList result = (PsiImportList) newImportList.copy();
		PsiImportList oldList = file.getImportList();
		if (oldList.isReplaceEquivalent(result))
			return;
		if (!nonImports.isEmpty()) {
			PsiElement firstPrevious = newImportList.getPrevSibling();
			while (firstPrevious != null && firstPrevious.getPrevSibling() != null) {
				firstPrevious = firstPrevious.getPrevSibling();
			}
			for (PsiElement element = firstPrevious;
				 element != null && element != newImportList; element = element.getNextSibling()) {
				result.add(element.copy());
			}
			for (PsiElement element = newImportList.getNextSibling();
				 element != null; element = element.getNextSibling()) {
				result.add(element.copy());
			}
		}
		importList.replace(result);
	}

	@NotNull
	private ImportsSorter getImportsSorter() {
		switch (importOrdering) {
			case ECLIPSE_451:
				return new ImportsSorter451(importsOrder);
			case ECLIPSE_452:
				return new ImportsSorter452(importsOrder);
		}
		throw new RuntimeException(String.valueOf(importOrdering));
	}

}
