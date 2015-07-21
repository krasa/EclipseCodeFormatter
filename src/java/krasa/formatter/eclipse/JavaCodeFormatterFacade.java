package krasa.formatter.eclipse;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.SystemInfo;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.pom.java.LanguageLevel;
import com.intellij.psi.PsiFile;
import com.intellij.psi.impl.JavaPsiImplementationHelper;
import krasa.formatter.common.ModifiableFile;
import krasa.formatter.exception.FormattingFailedException;
import krasa.formatter.plugin.Notifier;
import krasa.formatter.settings.Settings;
import krasa.formatter.settings.provider.JavaPropertiesProvider;
import org.eclipse.jdt.core.formatter.CodeFormatter;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.text.edits.TextEdit;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Properties;

/**
 * @author Vojtech Krasa
 */
public class JavaCodeFormatterFacade extends CodeFormatterFacade {

	private static final Logger LOG = Logger.getInstance(JavaCodeFormatterFacade.class.getName());

	private boolean useOldEclipseJavaFormatter;
	private Project project;
	protected Object codeFormatter;
	private LanguageLevel effectiveLanguageLevel;
	private JavaPropertiesProvider javaPropertiesProvider;
	protected ModifiableFile.Monitor lastState;

	public JavaCodeFormatterFacade(JavaPropertiesProvider javaPropertiesProvider, boolean useOldEclipseJavaFormatter,
								   Project project) {
		this.javaPropertiesProvider = javaPropertiesProvider;
		this.useOldEclipseJavaFormatter = useOldEclipseJavaFormatter;
		this.project = project;
	}

	private Object getCodeFormatter(LanguageLevel level) throws FileDoesNotExistsException {
		if (codeFormatter == null || javaPropertiesProvider.wasChanged(lastState)
				|| this.effectiveLanguageLevel != level) {
			return newCodeFormatter(level);
		}
		return codeFormatter;
	}

	private Object newCodeFormatter(LanguageLevel level) {
		lastState = javaPropertiesProvider.getModifiedMonitor();
		Properties options = javaPropertiesProvider.get();
		String substring = level.name().replace("_", ".").substring(4);
		// test
		new Double(substring);
		options.setProperty("org.eclipse.jdt.core.compiler.source", substring);
		options.setProperty("org.eclipse.jdt.core.compiler.codegen.targetPlatform", substring);
		options.setProperty("org.eclipse.jdt.core.compiler.compliance", substring);
		this.effectiveLanguageLevel = level;
		try {
			ClassLoader classLoader;
			if (useOldEclipseJavaFormatter) {
				classLoader = Classloaders.getEclipse44();
			} else {
				if (SystemInfo.isJavaVersionAtLeast("1.7")) {
					classLoader = Classloaders.getNewEclipse();
				} else {
					classLoader = Classloaders.getEclipse44();
					Notifier.notifyOldJRE(project);
				}
			}
			Class<?> aClass = Class.forName("org.eclipse.jdt.internal.formatter.DefaultCodeFormatter", true,
					classLoader);
			Constructor<?> constructor = aClass.getConstructor(Map.class);
			codeFormatter = constructor.newInstance(toMap(options));
		} catch (Throwable e) {
			// rethrow to have this plugin as a cause of the error report
			throw new RuntimeException(e);
		}
		return codeFormatter;
	}

	@NotNull
	private LanguageLevel getLanguageLevel(@NotNull PsiFile psiFile) {
		JavaPsiImplementationHelper instance = JavaPsiImplementationHelper.getInstance(project);
		LanguageLevel languageLevel = null;
		try {
			Method getClassesLanguageLevel = instance.getClass().getMethod("getClassesLanguageLevel",
					VirtualFile.class);
			languageLevel = (LanguageLevel) getClassesLanguageLevel.invoke(instance, psiFile.getVirtualFile());
		} catch (Exception e) {
			try {
				Method getClassesLanguageLevel = instance.getClass().getMethod("getEffectiveLanguageLevel",
						VirtualFile.class);
				languageLevel = (LanguageLevel) getClassesLanguageLevel.invoke(instance, psiFile.getVirtualFile());
			} catch (Exception e1) {
				LOG.error("Please report this", e);
				LOG.error("Please report this", e1);
			}
		}
		if (languageLevel == null) {
			languageLevel = LanguageLevel.JDK_1_7;
		}
		return languageLevel;
	}

	public String format(String text, int startOffset, int endOffset, PsiFile psiFile)
			throws FileDoesNotExistsException {
		LanguageLevel level = getLanguageLevel(psiFile);

		LOG.debug("#formatInternal");
		if (endOffset > text.length()) {
			endOffset = text.length();
		}
		IDocument doc = new Document();
		doc.set(text);
		/**
		 * Format <code>source</code>, and returns a text edit that correspond to the difference between the given
		 * string and the formatted string.
		 * <p>
		 * It returns null if the given string cannot be formatted.
		 * </p>
		 * <p>
		 * If the offset position is matching a whitespace, the result can include whitespaces. It would be up to the
		 * caller to get rid of preceding whitespaces.
		 * </p>
		 *
		 * @param kind
		 *            Use to specify the kind of the code snippet to format. It can be any of these:
		 *            <ul>
		 *            <li>{@link #K_EXPRESSION}</li>
		 *            <li>{@link #K_STATEMENTS}</li>
		 *            <li>{@link #K_CLASS_BODY_DECLARATIONS}</li>
		 *            <li>{@link #K_COMPILATION_UNIT}<br>
		 *            <b>Since 3.4</b>, the comments can be formatted on the fly while using this kind of code snippet
		 *            <br>
		 *            (see {@link #F_INCLUDE_COMMENTS} for more detailed explanation on this flag)</li>
		 *            <li>{@link #K_UNKNOWN}</li>
		 *            <li>{@link #K_SINGLE_LINE_COMMENT}</li>
		 *            <li>{@link #K_MULTI_LINE_COMMENT}</li>
		 *            <li>{@link #K_JAVA_DOC}</li>
		 *            </ul>
		 * @param source
		 *            the source to format
		 * @param offset
		 *            the given offset to start recording the edits (inclusive).
		 * @param length
		 *            the given length to stop recording the edits (exclusive).
		 * @param indentationLevel
		 *            the initial indentation level, used to shift left/right the entire source fragment. An initial
		 *            indentation level of zero or below has no effect.
		 * @param lineSeparator
		 *            the line separator to use in formatted source, if set to <code>null</code>, then the platform
		 *            default one will be used.
		 * @return the text edit
		 * @throws IllegalArgumentException
		 *             if offset is lower than 0, length is lower than 0 or length is greater than source length.
		 */

		LOG.debug("#starting to format by eclipse formatter");

		Object codeFormatter = getCodeFormatter(level);
		TextEdit edit;
		try {
			edit = (TextEdit) codeFormatter.getClass().getMethod("format", int.class, String.class, int.class,
					int.class, int.class, String.class).invoke(codeFormatter,
					CodeFormatter.K_COMPILATION_UNIT | CodeFormatter.F_INCLUDE_COMMENTS, text, startOffset,
					endOffset - startOffset, 0, Settings.LINE_SEPARATOR);
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
		if (edit == null) {
			throw new FormattingFailedException(getErrorMessage(level));
		}
		LOG.debug("reformatting done");
		try {
			edit.apply(doc);
		} catch (BadLocationException e) {
			throw new RuntimeException(e);
		}
		return doc.get();

	}

	private String getErrorMessage(LanguageLevel effectiveLanguageLevel) {
		return "languageLevel=" + effectiveLanguageLevel;
	}

}
