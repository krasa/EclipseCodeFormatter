package krasa.formatter.plugin;

import com.intellij.lang.ImportOptimizer;
import com.intellij.lang.java.JavaImportOptimizer;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.util.EmptyRunnable;
import com.intellij.psi.*;
import com.intellij.psi.impl.source.codeStyle.CodeStyleManagerImpl;
import krasa.formatter.eclipse.FileDoesNotExistsException;
import krasa.formatter.exception.ParsingFailedException;
import krasa.formatter.settings.*;
import krasa.formatter.settings.provider.ImportOrderProvider;
import org.jetbrains.annotations.NotNull;

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
		final Runnable runnable = new JavaImportOptimizer().processFile(file);
		return new Runnable() {
			@Override
			public void run() {
				try {
					runnable.run();
					
					Settings settings = ProjectSettingsComponent.getSettings(file);
					if (supports(file) && settings.isOptimizeImports() && settings.isEnabled()) {
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
