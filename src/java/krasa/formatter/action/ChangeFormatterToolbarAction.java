package krasa.formatter.action;

import javax.swing.*;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.IconLoader;

import krasa.formatter.settings.ProjectComponent;
import krasa.formatter.settings.Settings;

/**
 * @author Vojtech Krasa
 */
public class ChangeFormatterToolbarAction extends AnAction {
	private static final Logger LOG = Logger.getInstance(ChangeFormatterToolbarAction.class.getName());

	public static final Icon ICON = IconLoader.getIcon("/krasa/formatter/eclipse.gif");
	public static final Icon ICON1 = IconLoader.getIcon("/krasa/formatter/IDEA.gif");

	@Override
	public void actionPerformed(AnActionEvent e) {
		Settings settings;
		Project project = e.getProject();
		if (project != null) {
			ProjectComponent projectComponent = ProjectComponent.getInstance(project);
			settings = projectComponent.getSelectedProfile();
			settings.setFormatter(Settings.Formatter.DEFAULT == settings.getFormatter() ? Settings.Formatter.ECLIPSE
					: Settings.Formatter.DEFAULT);
			projectComponent.installOrUpdate(settings);
			updateIcon(settings, e.getPresentation());
		}
	}

	private Settings getSettings(AnActionEvent e) {
		 Settings settings = null;
		Project project = e.getProject();
		if (project != null) {
			ProjectComponent instance = ProjectComponent.getInstance(project);
			settings = instance.getSelectedProfile();
		}
		return settings;
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
			final Settings settings = getSettings(e);
			if (settings != null) {
				updateIcon(settings, e.getPresentation());
			}
		} catch (Throwable e1) {
			e.getPresentation().setEnabled(false);
		}

	}

}
