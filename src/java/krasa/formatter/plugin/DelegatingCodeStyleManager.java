/*
 * Eclipse Code Formatter Copyright (c) 2007-2009 Esko Luontola, www.orfjackal.net Licensed under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for
 * the specific language governing permissions and limitations under the License.
 */

package krasa.formatter.plugin;

import com.intellij.lang.ASTNode;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Computable;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.codeStyle.ChangedRangesInfo;
import com.intellij.psi.codeStyle.CodeStyleManager;
import com.intellij.psi.codeStyle.Indent;
import com.intellij.util.IncorrectOperationException;
import com.intellij.util.ThrowableRunnable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

/**
 * for tracking api changes only
 */
@SuppressWarnings({ "deprecation" })
public class DelegatingCodeStyleManager extends CodeStyleManager {

	@NotNull
	protected final CodeStyleManager original;

	public DelegatingCodeStyleManager(@NotNull CodeStyleManager original) {
		this.original = original;
	}

	@NotNull
	public CodeStyleManager getOriginal() {
		return original;
	}

	@Override
	@NotNull
	public Project getProject() {
		return original.getProject();
	}

	@Override
	@NotNull
	public PsiElement reformat(@NotNull PsiElement element) throws IncorrectOperationException {
		return original.reformat(element);
	}

	@Override
	@NotNull
	public PsiElement reformat(@NotNull PsiElement element, boolean canChangeWhiteSpacesOnly)
			throws IncorrectOperationException {
		return original.reformat(element, canChangeWhiteSpacesOnly);
	}

	@Override
	public PsiElement reformatRange(@NotNull PsiElement element, int startOffset, int endOffset)
			throws IncorrectOperationException {
		return original.reformatRange(element, startOffset, endOffset);
	}

	@Override
	public PsiElement reformatRange(@NotNull PsiElement element, int startOffset, int endOffset,
			boolean canChangeWhiteSpacesOnly) throws IncorrectOperationException {
		return original.reformatRange(element, startOffset, endOffset, canChangeWhiteSpacesOnly);
	}

	@Override
	public void reformatText(@NotNull PsiFile element, int startOffset, int endOffset)
			throws IncorrectOperationException {
		original.reformatText(element, startOffset, endOffset);
	}

	@Override
	public void adjustLineIndent(@NotNull PsiFile file, TextRange rangeToAdjust) throws IncorrectOperationException {
		original.adjustLineIndent(file, rangeToAdjust);
	}

	@Override
	public int adjustLineIndent(@NotNull PsiFile file, int offset) throws IncorrectOperationException {
		return original.adjustLineIndent(file, offset);
	}

	@Override
    public int adjustLineIndent(@NotNull Document document, int i) {
        return original.adjustLineIndent(document, i);
    }
    //	2017.1 EAP
//	@Override
//    public int adjustLineIndent(@NotNull Document document, int i, FormattingMode formattingMode) {
//        return original.adjustLineIndent(document, i, formattingMode);
//    }

	@Override
	public boolean isLineToBeIndented(@NotNull PsiFile file, int offset) {
		return original.isLineToBeIndented(file, offset);
	}

	@Override
	@Nullable
	public String getLineIndent(@NotNull PsiFile file, int offset) {
		return original.getLineIndent(file, offset);
	}

	@Override
	public Indent getIndent(String text, FileType fileType) {
		return original.getIndent(text, fileType);
	}

	@Override
	public String fillIndent(Indent indent, FileType fileType) {
		return original.fillIndent(indent, fileType);
	}

	@Override
	public Indent zeroIndent() {
		return original.zeroIndent();
	}

	@Override
	public void reformatNewlyAddedElement(@NotNull ASTNode block, @NotNull ASTNode addedElement)
			throws IncorrectOperationException {
		original.reformatNewlyAddedElement(block, addedElement);
	}

	@Override
	public boolean isSequentialProcessingAllowed() {
		return original.isSequentialProcessingAllowed();
	}

	// 10.5
	// @Override
	// public String getLineIndent(@NotNull Editor editor) {
	// return original.getLineIndent(editor);
	// }

	// 11.0
	@Override
	public String getLineIndent(@NotNull Document document, int offset) {
		return original.getLineIndent(document, offset);

	}

	@Override
	public void performActionWithFormatterDisabled(Runnable r) {
		original.performActionWithFormatterDisabled(r);
	}

	@Override
	public <T extends Throwable> void performActionWithFormatterDisabled(ThrowableRunnable<T> r) throws T {
		original.performActionWithFormatterDisabled(r);
	}

	@Override
	public <T> T performActionWithFormatterDisabled(Computable<T> r) {
		return original.performActionWithFormatterDisabled(r);

	}

//	2017.1 EAP
//    @Override
//    public FormattingMode getCurrentFormattingMode() {
//        return original.getCurrentFormattingMode();
//    }

	// 11.1
	// @Override
	@Override
	public void reformatText(@NotNull PsiFile psiFile, @NotNull Collection<TextRange> textRanges)
			throws IncorrectOperationException {
		original.reformatText(psiFile, textRanges);
	}

	// 16.3
	@Override
	public void reformatTextWithContext(@NotNull PsiFile psiFile, @NotNull ChangedRangesInfo changedRangesInfo) throws IncorrectOperationException {
		original.reformatTextWithContext(psiFile, changedRangesInfo);
	}

	// 15
    @Override
    public void reformatTextWithContext(@NotNull PsiFile psiFile, @NotNull Collection<TextRange> collection) throws IncorrectOperationException {
        original.reformatTextWithContext(psiFile, collection);
    }
}
