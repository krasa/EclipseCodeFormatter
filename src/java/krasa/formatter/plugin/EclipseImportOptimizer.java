package krasa.formatter.plugin;

import com.intellij.lang.ImportOptimizer;
import com.intellij.lang.java.JavaImportOptimizer;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Document;
import com.intellij.psi.*;
import krasa.formatter.exception.FileDoesNotExistsException;
import krasa.formatter.exception.ParsingFailedException;
import krasa.formatter.settings.ProjectSettingsComponent;
import krasa.formatter.settings.Settings;
import krasa.formatter.settings.provider.ImportOrderProvider;
import krasa.formatter.utils.FileUtils;
import org.jetbrains.annotations.NotNull;

/**
 * @author Vojtech Krasa
 */
public class EclipseImportOptimizer implements ImportOptimizer {

	private static final Logger LOG = Logger.getInstance("#krasa.formatter.plugin.processor.ImportOrderProcessor");

	@NotNull
	@Override
	public Runnable processFile(final PsiFile file) {
		final Runnable intellijRunnable = new JavaImportOptimizer().processFile(file);
		if (!(file instanceof PsiJavaFile)) {
			return intellijRunnable;
		}

		if (!isEnabled(file)) {
			return intellijRunnable;
		}

		return new Runnable() {

			@Override
			public void run() {
				intellijRunnable.run();
				try {
					Settings settings = ProjectSettingsComponent.getSettings(file);
					if (isEnabled(settings)) {
						optimizeImportsByEclipse((PsiJavaFile) file, settings);
					}
				} catch (Exception e) {
					LOG.error("Eclipse Import Optimizer failed", e);
				}
			}
		};
	}

	private void optimizeImportsByEclipse(PsiJavaFile psiFile, Settings settings) {
		ImportSorterAdapter importSorter = null;
		try {
			importSorter = getImportSorter(settings);

			commitDocument(psiFile);

			importSorter.sortImports(psiFile);
		} catch (ParsingFailedException e) {
			throw e;
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

	/**
	 * very strange, https://github.com/krasa/EclipseCodeFormatter/issues/59
	 */
	private void commitDocument(PsiJavaFile psiFile) {
		PsiDocumentManager e = PsiDocumentManager.getInstance(psiFile.getProject());
		Document document = e.getDocument(psiFile);
		if (document != null) {
			e.commitDocument(document);
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
		return FileUtils.isJava(file) && isEnabled(file);
	}

	private boolean isEnabled(Settings settings) {
		return settings.isEnabled() && settings.isEnableJavaFormatting() && settings.isOptimizeImports();
	}

	private boolean isEnabled(PsiFile file) {
		Settings settings = ProjectSettingsComponent.getSettings(file);
		return isEnabled(settings);
	}

}
