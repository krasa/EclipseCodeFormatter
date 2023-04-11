package krasa.formatter.processor;

import com.intellij.openapi.editor.Document;
import com.intellij.psi.PsiFile;
import krasa.formatter.plugin.Range;

/**
 * @author Vojtech Krasa
 */
public interface Processor {
	boolean process(Document document, PsiFile psiFile, Range range);

}
