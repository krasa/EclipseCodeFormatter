package krasa.formatter.plugin;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;

import krasa.formatter.settings.ProjectComponent;

/**
 * @author Vojtech Krasa
 */
public class Notifier {

	public static final String NO_FILE_TO_FORMAT = "No file to format";
	public static final String ECLIPSE_CURRENT = "Eclipse 4.7 Oxygen";

	public void notifyFailedFormatting(PsiFile psiFile, boolean formattedByIntelliJ, Exception e) {
		String error = e.getMessage() == null ? "" : e.getMessage();
		notifyFailedFormatting(psiFile, formattedByIntelliJ, error);
	}

	public void notifyFailedFormatting(PsiFile psiFile, boolean formattedByIntelliJ, final String reason) {
		String content;
		if (!formattedByIntelliJ) {
			content = psiFile.getName() + " failed to format with Eclipse code formatter. " + reason + "\n";
		} else {
			content = psiFile.getName() + " failed to format with IntelliJ code formatter.\n" + reason;
		}
		Notification notification = ProjectComponent.GROUP_DISPLAY_ID_ERROR.createNotification(content,
				NotificationType.ERROR);
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
			content = psiFile.getName() + " formatted successfully by Eclipse code formatter";
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
		String content = "Formatting failed due to new Import optimizer.";
		Notification notification = ProjectComponent.GROUP_DISPLAY_ID_ERROR.createNotification(content,
				NotificationType.ERROR);
		showNotification(notification, project);

	}

	public static void notifyDeletedSettings(final Project project) {
		String content = "Eclipse formatter settings profile was deleted for project " + project.getName()
				+ ". Check the configuration.";
		final Notification notification = ProjectComponent.GROUP_DISPLAY_ID_ERROR.createNotification(content,
				NotificationType.ERROR);
		ApplicationManager.getApplication().invokeLater(new Runnable() {
			@Override
			public void run() {
				Notifications.Bus.notify(notification, project);
			}
		});
	}

	public static void notifyOldJRE(final Project project) {
		String content = ECLIPSE_CURRENT + " formatter requires JRE 1.8+, using formatter from Eclipse 4.4. "
				+ "You can configure to use 4.4 in the plugin settings to avoid this warning, or use a custom eclipse location for Eclipse 4.5 (JDK 1.7).";

		final Notification notification = ProjectComponent.GROUP_DISPLAY_ID_ERROR.createNotification(content,
				NotificationType.WARNING);
		if (ApplicationManager.getApplication() != null) {// tests hack
			ApplicationManager.getApplication().invokeLater(new Runnable() {
				@Override
				public void run() {
					Notifications.Bus.notify(notification, project);
				}
			});
		}
	}

	public void configurationError(Exception e, Project project) {
		Notification notification = ProjectComponent.GROUP_DISPLAY_ID_ERROR.createNotification(
				"Eclipse Formatter configuration error: " + e.getMessage(), NotificationType.ERROR);
		showNotification(notification, project);
	}
}
