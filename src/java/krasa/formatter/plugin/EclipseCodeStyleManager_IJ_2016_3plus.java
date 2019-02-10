package krasa.formatter.plugin;

import java.util.Collections;
import java.util.List;

import org.jetbrains.annotations.NotNull;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiFile;
import com.intellij.psi.codeStyle.CodeStyleManager;
import com.intellij.util.IncorrectOperationException;

import krasa.formatter.settings.Settings;

public class EclipseCodeStyleManager_IJ_2016_3plus extends EclipseCodeStyleManager {

	private static final Logger LOG = Logger.getInstance(EclipseCodeStyleManager_IJ_2016_3plus.class.getName());

	public EclipseCodeStyleManager_IJ_2016_3plus(@NotNull CodeStyleManager original, @NotNull Settings settings) {
		super(original, settings);
	}

	// 16.3
	// @Override
	public void reformatTextWithContext(@NotNull PsiFile psiFile, @NotNull com.intellij.psi.codeStyle.ChangedRangesInfo changedRangesInfo)
			throws IncorrectOperationException {
		if (shouldReformatByEclipse(psiFile)) {
			List<TextRange> allChangedRanges = changedRangesInfo.allChangedRanges;
			reformatText(psiFile, allChangedRanges);
			// TODO check if file is being commited. rather than faking TextRange
		} else if (shouldSkipFormatting(psiFile, Collections.singletonList(new TextRange(0, psiFile.getTextLength())))) {
			notifier.notifyFormattingWasDisabled(psiFile);
		} else {
			original.reformatTextWithContext(psiFile, changedRangesInfo);
		}
	}

}
