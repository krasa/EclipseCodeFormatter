package krasa.formatter.eclipse;

import com.intellij.psi.PsiFile;
import krasa.formatter.common.ModifiableFile;
import krasa.formatter.exception.FormattingFailedException;
import krasa.formatter.plugin.InvalidPropertyFile;
import krasa.formatter.settings.Settings;
import krasa.formatter.settings.provider.CppPropertiesProvider;
import org.eclipse.cdt.core.ToolFactory;
import org.eclipse.cdt.core.formatter.CodeFormatter;
import org.eclipse.jface.text.*;
import org.eclipse.text.edits.TextEdit;

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

	public TextEdit format(int kind, String source, int offset, int length, int indentationLevel, String lineSeparator) {
		return getCodeFormatter().format(kind, source, offset, length, indentationLevel, lineSeparator);
	}
}
