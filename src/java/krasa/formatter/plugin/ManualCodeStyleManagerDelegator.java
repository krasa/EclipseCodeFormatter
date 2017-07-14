package krasa.formatter.plugin;

import com.intellij.formatting.FormattingMode;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiFile;
import com.intellij.psi.codeStyle.CodeStyleManager;
import com.intellij.psi.codeStyle.FormattingModeAwareIndentAdjuster;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

public class ManualCodeStyleManagerDelegator extends DelegatingCodeStyleManager implements FormattingModeAwareIndentAdjuster {
	private static final Logger log = Logger.getInstance(ManualCodeStyleManagerDelegator.class.getName());

	private final EclipseCodeStyleManager eclipseCodeStyleManager;


	public ManualCodeStyleManagerDelegator(@NotNull CodeStyleManager original, EclipseCodeStyleManager eclipseCodeStyleManager) {
		super(original);
		this.eclipseCodeStyleManager = eclipseCodeStyleManager;
	}

	@Override
	public void reformatTextWithContext(@NotNull PsiFile psiFile, @NotNull Collection<TextRange> collection) throws IncorrectOperationException {
		eclipseCodeStyleManager.reformatTextWithContext(psiFile, collection);
	}

	@Override
	public void reformatText(@NotNull PsiFile psiFile, @NotNull Collection<TextRange> textRanges) throws IncorrectOperationException {
		eclipseCodeStyleManager.reformatText(psiFile, textRanges);
	}

	@Override
	public void reformatText(@NotNull PsiFile psiFile, int startOffset, int endOffset) throws IncorrectOperationException {
		eclipseCodeStyleManager.reformatText(psiFile, startOffset, endOffset);
	}

	@Override
	public int adjustLineIndent(@NotNull Document document, int offset, FormattingMode formattingMode) {
		if (original instanceof FormattingModeAwareIndentAdjuster) {
			return ((FormattingModeAwareIndentAdjuster) original).adjustLineIndent(document, offset, formattingMode);
		} else {
			return offset;
		}
	}

	@Override
	public FormattingMode getCurrentFormattingMode() {
		if (original instanceof FormattingModeAwareIndentAdjuster) {
			return ((FormattingModeAwareIndentAdjuster) original).getCurrentFormattingMode();
		} else {
			return FormattingMode.REFORMAT;
		}
	}
}
