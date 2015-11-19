package krasa.formatter.adapter;

import krasa.formatter.common.ModifiableFile;
import krasa.formatter.eclipse.CodeFormatterFacade;
import krasa.formatter.exception.FileDoesNotExistsException;
import krasa.formatter.exception.FormattingFailedException;
import krasa.formatter.plugin.InvalidPropertyFile;
import krasa.formatter.settings.Settings;
import krasa.formatter.settings.provider.CppPropertiesProvider;

import org.eclipse.cdt.core.ToolFactory;
import org.eclipse.cdt.core.formatter.CodeFormatter;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.text.edits.TextEdit;

import com.intellij.psi.PsiFile;

/**
 * @author Vojtech Krasa
 */
public class CppCodeFormatterFacade extends CodeFormatterFacade {
	protected CodeFormatter codeFormatter;

	private CppPropertiesProvider propertiesProvider;
	protected ModifiableFile.Monitor modifiedMonitor;

	public CppCodeFormatterFacade(CppPropertiesProvider propertiesProvider) {
		this.propertiesProvider = propertiesProvider;
	}

	private CodeFormatter getCodeFormatter() throws FileDoesNotExistsException {
		if (codeFormatter == null || propertiesProvider.wasChanged(modifiedMonitor)) {
			return newCodeFormatter();
		}
		return codeFormatter;
	}

	private CodeFormatter newCodeFormatter() throws InvalidPropertyFile {
		modifiedMonitor = propertiesProvider.getModifiedMonitor();
		codeFormatter = ToolFactory.createDefaultCodeFormatter(toMap(propertiesProvider.get()));
		return codeFormatter;
	}

	@Override
	public String format(String text, int startOffset, int endOffset, PsiFile psiFile)
			throws FileDoesNotExistsException {
		IDocument doc = new Document();
		try {
			// format the file (the meat and potatoes)
			doc.set(text);
			TextEdit edit = getCodeFormatter().format(CodeFormatter.K_UNKNOWN, text, startOffset,
					endOffset - startOffset, 0, Settings.LINE_SEPARATOR);
			if (edit != null) {
				edit.apply(doc);
			} else {
				throw new FormattingFailedException();
			}

			return doc.get();
		} catch (BadLocationException e) {
			throw new RuntimeException(e);
		}
	}

}
