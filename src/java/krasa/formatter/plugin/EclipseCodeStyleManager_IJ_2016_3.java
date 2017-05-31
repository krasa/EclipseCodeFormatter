package krasa.formatter.plugin;

import java.util.List;

import org.jetbrains.annotations.NotNull;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiFile;
import com.intellij.psi.codeStyle.CodeStyleManager;
import com.intellij.util.IncorrectOperationException;

import krasa.formatter.settings.Settings;

public class EclipseCodeStyleManager_IJ_2016_3 extends EclipseCodeStyleManager {

	private static final Logger LOG = Logger.getInstance(EclipseCodeStyleManager_IJ_2016_3.class.getName());

	public EclipseCodeStyleManager_IJ_2016_3(@NotNull CodeStyleManager original, @NotNull Settings settings) {
		super(original, settings);
	}

	// 16.3
	// @Override
	public void reformatTextWithContext(@NotNull PsiFile psiFile,
			@NotNull com.intellij.psi.codeStyle.ChangedRangesInfo changedRangesInfo)
			throws IncorrectOperationException {
		if (!shouldReformatByEclipse(psiFile)) {
			original.reformatTextWithContext(psiFile, changedRangesInfo);
		} else {
			List<TextRange> allChangedRanges = changedRangesInfo.allChangedRanges;
			reformatText(psiFile, allChangedRanges);
		}
	}

}
