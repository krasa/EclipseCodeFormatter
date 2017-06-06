package krasa.formatter.action;

import com.intellij.ide.actions.QuickSwitchSchemeAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.project.Project;

import krasa.formatter.settings.ProjectComponent;
import krasa.formatter.settings.Settings;

/**
 * @author Vojtech Krasa
 */
public class QuickChangeCodeFormatterAction extends QuickSwitchSchemeAction {

	@Override
	protected void fillActions(final Project project, DefaultActionGroup group, DataContext dataContext) {
		Settings.Formatter formatter = ProjectComponent.getInstance(project).getSelectedProfile().getFormatter();
		for (final Settings.Formatter lf : Settings.Formatter.values()) {
			group.add(new DumbAwareAction(lf.name(), "", lf == formatter ? ourCurrentAction : ourNotCurrentAction) {
				@Override
				public void actionPerformed(AnActionEvent e) {
					changeFormatter(project, lf);
				}
			});
		}
	}

	private void changeFormatter(Project project, Settings.Formatter formatter) {
		ProjectComponent instance = ProjectComponent.getInstance(project);
		final Settings state = instance.getSelectedProfile();
		state.setFormatter(formatter);
		instance.installOrUpdate(state);
	}

	@Override
	protected boolean isEnabled() {
		return true;
	}
}
