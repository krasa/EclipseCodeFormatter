package krasa.formatter.plugin;

import org.jetbrains.annotations.NotNull;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.options.ShowSettingsUtil;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;

import krasa.formatter.exception.InvalidSettingsException;
import krasa.formatter.settings.ProjectComponent;
import krasa.formatter.settings.ProjectSettings;

/**
 * @author Vojtech Krasa
 */
public class Notifier {

	public static final String NO_FILE_TO_FORMAT = "No file to format";

	public void notifyFailedFormatting(PsiFile psiFile, boolean formattedByIntelliJ, Exception e) {
		String error = e.getMessage() == null ? "" : e.getMessage();
		notifyFailedFormatting(psiFile, formattedByIntelliJ, e, error);
	}

	public void notifyFailedFormatting(PsiFile psiFile, boolean formattedByIntelliJ, Exception e, final String reason) {
		String content;
		if (!formattedByIntelliJ) {
			if (e instanceof InvalidSettingsException) {
				content = psiFile.getName() + " failed to format. " + reason + "\n";
			} else {
				content = psiFile.getName() + " failed to format with Code Formatter for Eclipse. " + reason + "\n";
			}
		} else {
			content = psiFile.getName() + " failed to format with IntelliJ code formatter.\n" + reason;
		}
		Notification notification = ProjectComponent.GROUP_DISPLAY_ID_ERROR.createNotification(content,
				NotificationType.ERROR);

		if (e instanceof InvalidSettingsException) {
			notification.addAction(new AnAction("Open Settings") {
				@Override
				public void actionPerformed(@NotNull AnActionEvent anActionEvent) {
					Project eventProject = getEventProject(anActionEvent);
					ProjectSettings instance = ProjectSettings.getInstance(eventProject);
					ShowSettingsUtil.getInstance().showSettingsDialog(eventProject, "EclipseCodeFormatter");
				}
			});
		}
		showNotification(notification, psiFile.getProject());
	}

	void notifyFormattingWasDisabled(PsiFile psiFile) {
		Notification notification = ProjectComponent.GROUP_DISPLAY_ID_INFO.createNotification(
				psiFile.getName() + " - formatting was disabled for this file type", NotificationType.WARNING);
		showNotification(notification, psiFile.getProject());
	}

	void notifySuccessFormatting(PsiFile psiFile, boolean formattedByIntelliJ) {
		String content;
		if (formattedByIntelliJ) {
			content = psiFile.getName() + " formatted successfully by IntelliJ code formatter";
		} else {
			content = psiFile.getName() + " formatted successfully by Code Formatter for Eclipse";
		}
		Notification notification = ProjectComponent.GROUP_DISPLAY_ID_INFO.createNotification(content,
				NotificationType.INFORMATION);
		showNotification(notification, psiFile.getProject());
	}

	void showNotification(final Notification notification, final Project project) {
		ApplicationManager.getApplication().invokeLater(new Runnable() {
			@Override
			public void run() {
				Notifications.Bus.notify(notification, project);
			}
		});
	}

	public void notifyBrokenImportSorter(Project project) {
		String content = "Formatting failed due to a new Import optimizer.";
		Notification notification = ProjectComponent.GROUP_DISPLAY_ID_ERROR.createNotification(content, NotificationType.ERROR);
		showNotification(notification, project);

	}

	public static void notifyDeletedSettings(final Project project) {
		String content = "Eclipse formatter settings profile was deleted for project " + project.getName() + ". Check the configuration.";
		final Notification notification = ProjectComponent.GROUP_DISPLAY_ID_ERROR.createNotification(content, NotificationType.ERROR);
		ApplicationManager.getApplication().invokeLater(new Runnable() {
			@Override
			public void run() {
				Notifications.Bus.notify(notification, project);
			}
		});
	}


	public void configurationError(Exception e, Project project) {
		Notification notification = ProjectComponent.GROUP_DISPLAY_ID_ERROR.createNotification(
				"Eclipse Formatter configuration error: " + e.getMessage(), NotificationType.ERROR);
		showNotification(notification, project);
	}
}
