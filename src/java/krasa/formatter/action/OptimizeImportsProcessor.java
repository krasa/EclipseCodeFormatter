/*
 * Copyright 2000-2011 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package krasa.formatter.action;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.FutureTask;

import krasa.formatter.plugin.Range;
import krasa.formatter.plugin.processor.ImportOrderProcessor;
import krasa.formatter.settings.ProjectSettingsComponent;
import krasa.formatter.settings.Settings;
import krasa.formatter.settings.provider.ImportOrderProvider;

import org.jetbrains.annotations.NotNull;

import com.intellij.codeInsight.CodeInsightBundle;
import com.intellij.codeInsight.actions.AbstractLayoutCodeProcessor;
import com.intellij.lang.ImportOptimizer;
import com.intellij.lang.LanguageImportStatements;
import com.intellij.lang.java.JavaImportOptimizer;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.EmptyRunnable;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiFile;
import com.intellij.util.IncorrectOperationException;

public class OptimizeImportsProcessor extends AbstractLayoutCodeProcessor {
	private static final String PROGRESS_TEXT = CodeInsightBundle.message("progress.text.optimizing.imports");
	private static final String COMMAND_NAME = CodeInsightBundle.message("process.optimize.imports");

	public OptimizeImportsProcessor(Project project) {
		super(project, COMMAND_NAME, PROGRESS_TEXT, false);
	}

	public OptimizeImportsProcessor(Project project, Module module) {
		super(project, module, COMMAND_NAME, PROGRESS_TEXT, false);
	}

	public OptimizeImportsProcessor(Project project, PsiDirectory directory, boolean includeSubdirs) {
		super(project, directory, includeSubdirs, PROGRESS_TEXT, COMMAND_NAME, false);
	}

	public OptimizeImportsProcessor(Project project, PsiFile file) {
		super(project, file, PROGRESS_TEXT, COMMAND_NAME, false);
	}

	public OptimizeImportsProcessor(Project project, PsiFile[] files, Runnable postRunnable) {
		this(project, files, COMMAND_NAME, postRunnable);
	}

	public OptimizeImportsProcessor(Project project, PsiFile[] files, String commandName, Runnable postRunnable) {
		super(project, files, PROGRESS_TEXT, commandName, postRunnable, false);
	}

	@NotNull
	protected FutureTask<Boolean> preprocessFile(@NotNull final PsiFile file, boolean processChangedTextOnly)
			throws IncorrectOperationException {
		final Set<ImportOptimizer> optimizers = LanguageImportStatements.INSTANCE.forFile(file);
		final List<Runnable> runnables = new ArrayList<Runnable>();
		List<PsiFile> files = file.getViewProvider().getAllFiles();
		for (ImportOptimizer optimizer : optimizers) {
			for (PsiFile psiFile : files) {
				if (optimizer.supports(psiFile)) {
					if (isJavaImportsOptimizer(optimizer)) {
						runnables.add(new Runnable() {
							@Override
							public void run() {
								Settings settings = ProjectSettingsComponent.getInstance(myProject).getSettings();
								ImportOrderProcessor importOrderProcessor = new ImportOrderProcessor(settings,
										new ImportOrderProvider(settings));
								Document document = PsiDocumentManager.getInstance(myProject).getDocument(file);
								importOrderProcessor.process(document, file, new Range(0, document.getText().length(),
										true));
							}
						});
					} else {
						runnables.add(optimizer.processFile(psiFile));
					}
				}
			}
		}
		Runnable runnable = runnables.isEmpty() ? EmptyRunnable.getInstance() : new Runnable() {
			@Override
			public void run() {
				for (Runnable runnable : runnables) {
					runnable.run();
				}
			}
		};
		return new FutureTask<Boolean>(runnable, true);
	}

	private boolean isJavaImportsOptimizer(ImportOptimizer optimizer) {
		return optimizer instanceof JavaImportOptimizer;
	}
}
