package krasa.formatter.eclipse;

import krasa.formatter.plugin.Range;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.TypedPosition;
import org.eclipse.text.edits.TextEdit;

import java.lang.reflect.Method;
import java.util.Map;

public class JsniFormattingUtilFacade {

	public static TextEdit format(IDocument document, Map javaFormattingPrefs, Map javaScriptPrefs, Range range) {
		try {
			Class<?> aClass = Classloaders.getEclipse44().loadClass("krasa.formatter.JsniFormattingUtil");
			Object o = aClass.newInstance();
			Method format = aClass.getMethod("format", IDocument.class, Map.class, Map.class, Range.class);
			return (TextEdit) format.invoke(o, document, javaFormattingPrefs, javaScriptPrefs, range);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public static TextEdit format(IDocument document, TypedPosition partition, Map<String, String> javaFormattingPrefs,
								  Map<String, String> jsMap, Object original) {
		try {
			Class<?> aClass = Classloaders.getEclipse44().loadClass("krasa.formatter.JsniFormattingUtil");
			Object o = aClass.newInstance();
			Method format = aClass.getMethod("format", IDocument.class, TypedPosition.class, Map.class, Map.class,
					String.class);
			return (TextEdit) format.invoke(o, document, partition, javaFormattingPrefs, jsMap, null);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
