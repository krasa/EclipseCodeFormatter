package krasa.formatter.plugin;

import krasa.formatter.eclipse.FileDoesNotExistsException;
import krasa.formatter.settings.ProjectSettingsComponent;
import krasa.formatter.settings.Settings;
import krasa.formatter.settings.provider.ImportOrderProvider;

import org.jetbrains.annotations.NotNull;

import com.intellij.lang.ImportOptimizer;
import com.intellij.lang.java.JavaImportOptimizer;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.EmptyRunnable;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiImportList;
import com.intellij.psi.PsiImportStatementBase;
import com.intellij.psi.PsiJavaFile;
import com.intellij.psi.impl.source.codeStyle.CodeStyleManagerImpl;
import com.intellij.util.IncorrectOperationException;

/**
 * @author Vojtech Krasa
 */
public class EclipseImportOptimizer implements ImportOptimizer {
	private static final Logger LOG = Logger.getInstance("#krasa.formatter.plugin.processor.ImportOrderProcessor");

	@NotNull
	@Override
	public Runnable processFile(final PsiFile file) {
		if (!(file instanceof PsiJavaFile)) {
			return EmptyRunnable.getInstance();
		}
		return new Runnable() {
			@Override
			public void run() {
				try {
					final PsiDocumentManager manager = PsiDocumentManager.getInstance(file.getProject());
					final Document document = manager.getDocument(file);
					if (document != null) {
						manager.commitDocument(document);
					}
					process(document, file, new Range(0, 0, true));

				} catch (IncorrectOperationException e) {
					LOG.error(e);
				}
			}
		};
	}

	private boolean process(final Document document, final PsiFile psiFile, final Range range) {
		CodeStyleManagerImpl.setSequentialProcessingAllowed(false);

		optimizeImportsByIntellij(psiFile);

		Settings settings = ProjectSettingsComponent.getSettings(psiFile);
		if (supports(psiFile) && settings.isOptimizeImports() && settings.isEnabled()) {
			optimizeImportsByEclipse(document, (PsiJavaFile) psiFile, settings);
		}
		CodeStyleManagerImpl.setSequentialProcessingAllowed(true);
		return true;
	}

	private static void optimizeImportsByIntellij(PsiFile psiFile) {
		final Runnable runnable = new JavaImportOptimizer().processFile(psiFile);
		runnable.run();
		Project project = psiFile.getProject();
		try {
			final PsiDocumentManager manager = PsiDocumentManager.getInstance(project);
			final Document document = manager.getDocument(psiFile);
			if (document != null) {
				manager.commitDocument(document);
			}
			manager.doPostponedOperationsAndUnblockDocument(document);
		} catch (IncorrectOperationException e) {
			LOG.error(e);
		}
	}

	private void optimizeImportsByEclipse(Document document, PsiJavaFile psiFile, Settings settings) {
		ImportSorterAdapter importSorter = null;
		try {
			importSorter = getImportSorter(settings);
			importSorter.sortImports(document);
		} catch (FileDoesNotExistsException e) {
			throw e;
		} catch (Exception e) {
			final PsiImportList oldImportList = (psiFile).getImportList();
			StringBuilder stringBuilder = new StringBuilder();
			if (oldImportList != null) {
				PsiImportStatementBase[] allImportStatements = oldImportList.getAllImportStatements();
				for (PsiImportStatementBase allImportStatement : allImportStatements) {
					String text = allImportStatement.getText();
					stringBuilder.append(text);
				}
			}
			String message = "imports: " + stringBuilder.toString() + ", settings: "
					+ (importSorter != null ? importSorter.getImportsOrderAsString() : null);
			throw new ImportSorterException(message, e);
		}
	}

	protected ImportSorterAdapter getImportSorter(Settings settings) {
		if (settings.isImportOrderFromFile()) {
			final ImportOrderProvider importOrderProviderFromFile = settings.getImportOrderProvider();
			return new ImportSorterAdapter(importOrderProviderFromFile.get());
		} else {
			return new ImportSorterAdapter(settings.getImportOrderAsList());
		}
	}

	@Override
	public boolean supports(PsiFile file) {
		return file instanceof PsiJavaFile;
	}

}
