package krasa.formatter.action;

import javax.swing.*;

import krasa.formatter.settings.ProjectSettingsComponent;
import krasa.formatter.settings.Settings;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.DataKeys;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.IconLoader;

/**
 * @author Vojtech Krasa
 */
public class ChangeFormatterToolbarAction extends AnAction {
	private static final Logger LOG = Logger.getInstance(ChangeFormatterToolbarAction.class.getName());

	public static final Icon ICON = IconLoader.getIcon("/krasa/formatter/eclipse.gif");
	public static final Icon ICON1 = IconLoader.getIcon("/krasa/formatter/IDEA.gif");

	public void actionPerformed(AnActionEvent e) {
		Settings settings;
		Project project = getProject(e);
		if (project != null) {
			ProjectSettingsComponent instance = ProjectSettingsComponent.getInstance(project);
			settings = instance.getSettings();
			settings.setFormatter(Settings.Formatter.DEFAULT == settings.getFormatter() ? Settings.Formatter.ECLIPSE
					: Settings.Formatter.DEFAULT);
			instance.install(settings);
			updateIcon(settings, e.getPresentation());
		}
	}

	private Settings getSettings(AnActionEvent e) {
		 Settings settings = null;
		Project project = getProject(e);
		if (project != null) {
			ProjectSettingsComponent instance = ProjectSettingsComponent.getInstance(project);
			settings = instance.getSettings();
		}
		return settings;
	}

	private Project getProject(AnActionEvent e) {
		try {
			return e.getProject();
		} catch (Throwable e1) {
			// old version
		}
		DataContext dataContext = e.getDataContext();
		return DataKeys.PROJECT.getData(dataContext);
	}

	private void updateIcon(Settings state, Presentation presentation) {
		if (state.getFormatter() == Settings.Formatter.DEFAULT) {
			presentation.setIcon(ICON1);
		} else {
			presentation.setIcon(ICON);
		}
	}

	@Override
	public void update(AnActionEvent e) {
		super.update(e);
		try {
			Presentation presentation = e.getPresentation();
			final Settings state = getSettings(e);
			if (state != null) {
				updateIcon(state, presentation);
			}
		} catch (Throwable e1) {
			e.getPresentation().setEnabled(false);
		}

	}

}
