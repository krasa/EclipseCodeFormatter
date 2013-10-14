/*
 * Eclipse Code Formatter Copyright (c) 2007-2009 Esko Luontola, www.orfjackal.net Licensed under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for
 * the specific language governing permissions and limitations under the License.
 */

package krasa.formatter.plugin;

import java.util.Collection;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.intellij.lang.ASTNode;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Computable;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.codeStyle.CodeStyleManager;
import com.intellij.psi.codeStyle.Indent;
import com.intellij.util.IncorrectOperationException;
import com.intellij.util.ThrowableRunnable;

/**
 * Wrapper for intercepting the method calls to a {@link CodeStyleManager} instance.
 * 
 * @author Esko Luontola
 * @since 2.12.2007
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

	@NotNull
	public Project getProject() {
		return original.getProject();
	}

	@NotNull
	public PsiElement reformat(@NotNull PsiElement element) throws IncorrectOperationException {
		return original.reformat(element);
	}

	@NotNull
	public PsiElement reformat(@NotNull PsiElement element, boolean canChangeWhiteSpacesOnly)
			throws IncorrectOperationException {
		return original.reformat(element, canChangeWhiteSpacesOnly);
	}

	public PsiElement reformatRange(@NotNull PsiElement element, int startOffset, int endOffset)
			throws IncorrectOperationException {
		return original.reformatRange(element, startOffset, endOffset);
	}

	public PsiElement reformatRange(@NotNull PsiElement element, int startOffset, int endOffset,
			boolean canChangeWhiteSpacesOnly) throws IncorrectOperationException {
		return original.reformatRange(element, startOffset, endOffset, canChangeWhiteSpacesOnly);
	}

	public void reformatText(@NotNull PsiFile element, int startOffset, int endOffset)
			throws IncorrectOperationException {
		original.reformatText(element, startOffset, endOffset);
	}

	public void adjustLineIndent(@NotNull PsiFile file, TextRange rangeToAdjust) throws IncorrectOperationException {
		original.adjustLineIndent(file, rangeToAdjust);
	}

	public int adjustLineIndent(@NotNull PsiFile file, int offset) throws IncorrectOperationException {
		return original.adjustLineIndent(file, offset);
	}

	public int adjustLineIndent(@NotNull Document document, int offset) {
		return original.adjustLineIndent(document, offset);
	}

	public boolean isLineToBeIndented(@NotNull PsiFile file, int offset) {
		return original.isLineToBeIndented(file, offset);
	}

	@Nullable
	public String getLineIndent(@NotNull PsiFile file, int offset) {
		return original.getLineIndent(file, offset);
	}

	public Indent getIndent(String text, FileType fileType) {
		return original.getIndent(text, fileType);
	}

	public String fillIndent(Indent indent, FileType fileType) {
		return original.fillIndent(indent, fileType);
	}

	public Indent zeroIndent() {
		return original.zeroIndent();
	}

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
	// 11.1
	// @Override
	public void reformatText(@NotNull PsiFile psiFile, @NotNull Collection<TextRange> textRanges)
			throws IncorrectOperationException {
		original.reformatText(psiFile, textRanges);
	}
}
