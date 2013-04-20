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

import org.jetbrains.annotations.NonNls;

import com.intellij.codeInsight.CodeInsightBundle;
import com.intellij.codeInsight.actions.BaseCodeInsightAction;
import com.intellij.codeInsight.actions.LayoutProjectCodeDialog;
import com.intellij.codeInsight.actions.ReformatCodeAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.ex.EditorSettingsExternalizable;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.ReadonlyStatusHandler;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiDirectoryContainer;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;

public class OptimizeImportsAction extends com.intellij.codeInsight.actions.OptimizeImportsAction {
	private static final @NonNls
	String HELP_ID = "editing.manageImports";

	public void actionPerformed(AnActionEvent event) {
		// super.actionPerformed(event);
		actionPerformedImpl(event.getDataContext());
	}

	public static void actionPerformedImpl(final DataContext dataContext) {
		final Project project = PlatformDataKeys.PROJECT.getData(dataContext);
		if (project == null) {
			return;
		}
		PsiDocumentManager.getInstance(project).commitAllDocuments();
		final Editor editor = BaseCodeInsightAction.getInjectedEditor(project,
				PlatformDataKeys.EDITOR.getData(dataContext));

		final VirtualFile[] files = PlatformDataKeys.VIRTUAL_FILE_ARRAY.getData(dataContext);

		PsiFile file = null;
		PsiDirectory dir;

		if (editor != null) {
			file = PsiDocumentManager.getInstance(project).getPsiFile(editor.getDocument());
			if (file == null)
				return;
			dir = file.getContainingDirectory();
		} else if (files != null && ReformatCodeAction.areFiles(files)) {
			final ReadonlyStatusHandler.OperationStatus operationStatus = ReadonlyStatusHandler.getInstance(project).ensureFilesWritable(
					files);
			if (!operationStatus.hasReadonlyFiles()) {
				new OptimizeImportsProcessor(project, ReformatCodeAction.convertToPsiFiles(files, project), null).run();
			}
			return;
		} else {
			Project projectContext = PlatformDataKeys.PROJECT_CONTEXT.getData(dataContext);
			Module moduleContext = LangDataKeys.MODULE_CONTEXT.getData(dataContext);

			if (projectContext != null || moduleContext != null) {
				final String text;
				if (moduleContext != null) {
					text = CodeInsightBundle.message("process.scope.module", moduleContext.getName());
				} else {
					text = CodeInsightBundle.message("process.scope.project", projectContext.getPresentableUrl());
				}
				LayoutProjectCodeDialog dialog = new LayoutProjectCodeDialog(project, null,
						CodeInsightBundle.message("process.optimize.imports"), text, false);
				dialog.show();
				if (!dialog.isOK())
					return;
				if (moduleContext != null) {
					new OptimizeImportsProcessor(project, moduleContext).run();
				} else {
					new OptimizeImportsProcessor(projectContext).run();
				}
				return;
			}

			PsiElement element = LangDataKeys.PSI_ELEMENT.getData(dataContext);
			if (element == null)
				return;
			if (element instanceof PsiDirectoryContainer) {
				dir = ((PsiDirectoryContainer) element).getDirectories()[0];
			} else if (element instanceof PsiDirectory) {
				dir = (PsiDirectory) element;
			} else {
				file = element.getContainingFile();
				if (file == null)
					return;
				dir = file.getContainingDirectory();
			}
		}

		boolean processDirectory;
		boolean includeSubdirectories;

		if (ApplicationManager.getApplication().isUnitTestMode()) {
			includeSubdirectories = processDirectory = false;
		} else if (!EditorSettingsExternalizable.getInstance().getOptions().SHOW_OPIMIZE_IMPORTS_DIALOG && file != null) {
			includeSubdirectories = processDirectory = false;
		} else {
			// final LayoutCodeDialog dialog = new LayoutCodeDialog(project,
			// CodeInsightBundle.message("process.optimize.imports"), file, dir, null, HELP_ID);
			// dialog.show();
			// if (!dialog.isOK()) return;
			// // EditorSettingsExternalizable.getInstance().getOptions().SHOW_OPIMIZE_IMPORTS_DIALOG =
			// !dialog.isDoNotAskMe();
			// EditorSettingsExternalizable.getInstance().getOptions().SHOW_OPIMIZE_IMPORTS_DIALOG = false;
			// ReformatCodeAction.updateShowDialogSetting(dialog, "\"Optimize Imports\" dialog disabled");
			// processDirectory = dialog.isProcessDirectory();
			// includeSubdirectories = dialog.isIncludeSubdirectories();
			processDirectory = file == null;
			includeSubdirectories = true;
		}

		if (processDirectory) {
			new OptimizeImportsProcessor(project, dir, includeSubdirectories).run();
		} else {
			new OptimizeImportsProcessor(project, file).run();
		}
	}

}
