package krasa.formatter.plugin.processor;

import krasa.formatter.plugin.Range;

import com.intellij.openapi.editor.Document;
import com.intellij.psi.PsiFile;

/**
 * @author Vojtech Krasa
 */
public interface Processor {
	public boolean process(Document document, PsiFile psiFile, Range range);

}
