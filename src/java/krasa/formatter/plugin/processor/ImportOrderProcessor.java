package krasa.formatter.plugin.processor;

import krasa.formatter.common.ModifiableFile;
import krasa.formatter.eclipse.FileDoesNotExistsException;
import krasa.formatter.plugin.ImportSorterAdapter;
import krasa.formatter.plugin.ImportSorterException;
import krasa.formatter.plugin.Range;
import krasa.formatter.settings.Settings;
import krasa.formatter.settings.provider.ImportOrderProvider;
import krasa.formatter.utils.FileUtils;

import com.intellij.openapi.editor.Document;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiImportList;
import com.intellij.psi.PsiImportStatementBase;
import com.intellij.psi.PsiJavaFile;
import com.intellij.psi.impl.source.codeStyle.CodeStyleManagerImpl;

/**
 * @author Vojtech Krasa
 */
public class ImportOrderProcessor implements Processor {
	private Settings settings;
	protected ImportSorterAdapter importSorter;
	protected ImportOrderProvider importOrderProviderFromFile;
	protected ModifiableFile.Monitor modifiedMonitor;

	public ImportOrderProcessor(Settings settings, ImportOrderProvider importOrderProvider) {
		this.settings = settings;
		this.importOrderProviderFromFile = importOrderProvider;
	}

	@Override
	public boolean process(final Document document, final PsiFile psiFile, final Range range) {
		CodeStyleManagerImpl.setSequentialProcessingAllowed(false);
		if (FileUtils.isJava(psiFile) && settings.isOptimizeImports() && range.isWholeFile()) {
			FileUtils.optimizeImportsByIntellij(psiFile);
			try {
				getImportSorter().sortImports(document);
			} catch (FileDoesNotExistsException e) {
				throw e;
			} catch (Exception e) {
				final PsiImportList oldImportList = ((PsiJavaFile) psiFile).getImportList();
				StringBuilder stringBuilder = new StringBuilder();
				if (oldImportList != null) {
					PsiImportStatementBase[] allImportStatements = oldImportList.getAllImportStatements();
					for (PsiImportStatementBase allImportStatement : allImportStatements) {
						String text = allImportStatement.getText();
						stringBuilder.append(text);
					}
				}
				String message = "imports: " + stringBuilder.toString() + ", settings: " + settings.getImportOrder();
				throw new ImportSorterException(message, e);
			}
		}
		CodeStyleManagerImpl.setSequentialProcessingAllowed(true);
		return true;
	}

	protected ImportSorterAdapter getImportSorter() {
		if (settings.isImportOrderFromFile()) {
			if (importSorter == null || importOrderProviderFromFile.wasChanged(modifiedMonitor)) {
				modifiedMonitor = importOrderProviderFromFile.getModifiedMonitor();
				importSorter = new ImportSorterAdapter(importOrderProviderFromFile.get());
			}
		} else if (importSorter == null) {
			importSorter = new ImportSorterAdapter(settings.getImportOrderAsList());
		}
		return importSorter;
	}

}
