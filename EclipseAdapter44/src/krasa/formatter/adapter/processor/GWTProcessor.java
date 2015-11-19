package krasa.formatter.adapter.processor;

import java.util.Properties;

import krasa.formatter.adapter.JsniFormattingUtil;
import krasa.formatter.plugin.Range;
import krasa.formatter.processor.Processor;
import krasa.formatter.settings.Settings;
import krasa.formatter.utils.FileUtils;

import org.eclipse.jface.text.IDocument;
import org.eclipse.text.edits.TextEdit;

import com.intellij.openapi.editor.Document;
import com.intellij.psi.PsiFile;

/**
 * @author Vojtech Krasa
 */
public class GWTProcessor implements Processor {
	private Settings settings;

	public GWTProcessor(Settings settings) {
		this.settings = settings;
	}

	@Override
	public boolean process(Document documentIJ, PsiFile file, Range range) {
		if (FileUtils.isJava(file) && settings.isEnableGWT()) {
			try {
				IDocument document = new org.eclipse.jface.text.Document(documentIJ.getText());
				Properties javaFormattingPrefs = settings.getJavaProperties().get();
				Properties jsFormattingPrefs = settings.getJSProperties().get();

				JsniFormattingUtil jsniFormattingUtil = new JsniFormattingUtil();
				TextEdit formatEdit = jsniFormattingUtil.format(document, javaFormattingPrefs, jsFormattingPrefs,
						range);
				formatEdit.apply(document);
				documentIJ.setText(document.get());
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
		return true;
	}

}
