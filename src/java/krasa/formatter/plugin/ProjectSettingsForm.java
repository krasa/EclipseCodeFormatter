/*
 * External Code Formatter Copyright (c) 2007-2009 Esko Luontola, www.orfjackal.net Licensed under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for
 * the specific language governing permissions and limitations under the License.
 */

package krasa.formatter.plugin;

import com.centerkey.utils.BareBonesBrowserLaunch;
import com.intellij.openapi.application.ex.ApplicationEx;
import com.intellij.openapi.application.ex.ApplicationManagerEx;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.fileChooser.FileChooser;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.ui.NonEmptyInputValidator;
import com.intellij.openapi.ui.popup.ListPopup;
import com.intellij.openapi.ui.popup.PopupStep;
import com.intellij.openapi.ui.popup.util.BaseListPopupStep;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.ColoredSideBorder;
import com.intellij.ui.DocumentAdapter;
import com.intellij.ui.ListCellRendererWrapper;
import com.intellij.ui.SortedComboBoxModel;
import com.intellij.ui.popup.list.ListPopupImpl;
import com.intellij.ui.popup.mock.MockConfirmation;
import krasa.formatter.eclipse.ConfigFileLocator;
import krasa.formatter.settings.GlobalSettings;
import krasa.formatter.settings.MyConfigurable;
import krasa.formatter.settings.ProjectSettings;
import krasa.formatter.settings.Settings;
import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import javax.swing.event.DocumentEvent;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import static com.intellij.openapi.fileChooser.FileChooserDescriptorFactory.*;

/**
 * Configuration dialog for changing the {@link krasa.formatter.settings.Settings} of the plugin.
 *
 * @author Esko Luontola
 * @author Vojtech Krasa
 * @since 4.12.2007
 */
public class ProjectSettingsForm {
	private static final Logger LOG = Logger.getInstance(ProjectSettingsForm.class.getName());
	public static final String NOT_EXISTS = "NOT EXISTS";
	public static final String PARSING_FAILED = "PARSING FAILED";
	public static final String CONTAINS_NO_PROFILES = "CONTAINS NO PROFILES";
	public Border normalBorder;
	public static final ColoredSideBorder ERROR_BORDER = new ColoredSideBorder(Color.RED, Color.RED, Color.RED, Color.RED, 1);

	private JPanel rootComponent;

	private JRadioButton useDefaultFormatter;
	private JRadioButton useEclipseFormatter;

	private JLabel eclipseSupportedFileTypesLabel;

	private JTextField disabledFileTypes;
	private JLabel disabledFileTypesHelpLabel;
	private JRadioButton doNotFormatOtherFilesRadioButton;
	private JRadioButton formatOtherFilesWithExceptionsRadioButton;
	private JCheckBox formatSelectedTextInAllFileTypes;

	private JLabel eclipsePreferenceFileJavaLabel;

	private JTextField pathToEclipsePreferenceFileJava;

	private JLabel eclipsePrefsExample;

	private JCheckBox enableJavaFormatting;

	private JButton eclipsePreferenceFilePathJavaBrowse;

	private JCheckBox optimizeImportsCheckBox;
	private JLabel importOrderLabel;
	private JFormattedTextField importOrder;
	private JLabel importOrderManualExample;
	private JTextField pathToImportOrderPreferenceFile;
	private JButton pathToImportOrderPreferenceFileBrowse;
	private JLabel importOrderPreferenceFileExample;
	private JRadioButton importOrderConfigurationFromFileRadioButton;
	private JRadioButton importOrderConfigurationManualRadioButton;

	private JComboBox profiles;
	private JButton newProfile;
	private JButton copyProfile;
	private JButton rename;
	private JButton exportToProjectProfile;
	private JButton delete;
	private Settings displayedSettings;
	private JButton donateButton;
	public JComboBox javaFormatterProfile;
	private JLabel javaFormatterProfileLabel;
	private JButton helpButton;
	private JButton homepage;
	private JCheckBox useForLiveTemplates;

	private JTextField pathToCustomEclipse;
	private JButton customEclipseLocationBrowse;
	private JRadioButton useEclipseNewest;
	private JRadioButton useEclipseCustom;
	private JLabel javaFormatterVersionLabel;
	private JRadioButton importOrdering451;
	private JRadioButton importOrdering452;
	private JButton profileHelp;
	private JLabel importStyleLabel;
	private JRadioButton schemeEclipseJC;
	private JRadioButton schemeEclipse;
	private JRadioButton schemeEclipse21;
	private JRadioButton schemeEclipseFile;
	private JRadioButton schemeCurrentProject;

	private final List<Popup> visiblePopups = new ArrayList<Popup>();
	@NotNull
	private Project project;
	protected SortedComboBoxModel profilesModel;
	private MyConfigurable myConfigurable;

	private void updateComponents() {
		hidePopups();

		enabledBy(new JComponent[]{eclipseSupportedFileTypesLabel, enableJavaFormatting, doNotFormatOtherFilesRadioButton,
				formatOtherFilesWithExceptionsRadioButton,
				importOrderPreferenceFileExample, importOrderConfigurationFromFileRadioButton,
				importOrderConfigurationManualRadioButton, useEclipseNewest, useEclipseCustom,
				formatSelectedTextInAllFileTypes, useForLiveTemplates, importOrdering451, importOrdering452}, useEclipseFormatter);

		enabledBy(new JComponent[]{pathToEclipsePreferenceFileJava, schemeEclipseJC,
				schemeEclipse, schemeCurrentProject,
				schemeEclipse21,
				schemeEclipseFile, eclipsePrefsExample, eclipsePreferenceFileJavaLabel, optimizeImportsCheckBox,
				eclipsePreferenceFilePathJavaBrowse, javaFormatterProfileLabel, javaFormatterProfile, customEclipseLocationBrowse, pathToCustomEclipse,
				useEclipseNewest, useEclipseCustom, javaFormatterVersionLabel, importStyleLabel,
				importOrdering451, importOrdering452}, enableJavaFormatting);

		enabledBy(new JComponent[]{pathToCustomEclipse, customEclipseLocationBrowse,}, useEclipseCustom);

		enabledBy(new JComponent[]{importOrder, pathToImportOrderPreferenceFile, pathToImportOrderPreferenceFileBrowse, importOrderManualExample,
						importOrderLabel, importOrderPreferenceFileExample, importOrderConfigurationFromFileRadioButton, importOrderConfigurationManualRadioButton},
				optimizeImportsCheckBox);

		enabledBy(new JComponent[]{pathToImportOrderPreferenceFile, importOrderPreferenceFileExample, pathToImportOrderPreferenceFileBrowse},
				importOrderConfigurationFromFileRadioButton);

		enabledBy(new JComponent[]{importOrder, importOrderManualExample,}, importOrderConfigurationManualRadioButton);


		enabledBy(new JComponent[]{disabledFileTypes, disabledFileTypesHelpLabel,}, formatOtherFilesWithExceptionsRadioButton);

		enabledBy(new JComponent[]{pathToEclipsePreferenceFileJava, eclipsePreferenceFilePathJavaBrowse, javaFormatterProfile, eclipsePrefsExample, javaFormatterProfileLabel}, schemeEclipseFile);

		disableJavaProfilesIfNecessary();

		delete.setEnabled(!displayedSettings.isProjectSpecific());
		rename.setEnabled(!displayedSettings.isProjectSpecific());
		exportToProjectProfile.setEnabled(!displayedSettings.isProjectSpecific());
		setJavaFormatterProfileModel();

	}

	private void enabledByAny(@NotNull JComponent[] targets, @NotNull JToggleButton[] negated, @NotNull JToggleButton... control) {
		boolean b = false;

		for (JToggleButton jToggleButton : control) {
			b = b || (jToggleButton.isEnabled() && jToggleButton.isSelected());
		}
		for (JToggleButton jToggleButton : negated) {
			b = b || (jToggleButton.isEnabled() && !jToggleButton.isSelected());
		}

		for (JComponent target : targets) {
			target.setEnabled(b);
		}
	}

	private void disableJavaProfilesIfNecessary() {
		String text = pathToEclipsePreferenceFileJava.getText();
		if (!text.toLowerCase().endsWith("xml")) {
			javaFormatterProfile.setEnabled(false);
		}
	}


	public ProjectSettingsForm(final Project project, MyConfigurable myConfigurable) {
		this.myConfigurable = myConfigurable;
		donateButton.setBorder(BorderFactory.createEmptyBorder());
		donateButton.setContentAreaFilled(false);
		donateButton.putClientProperty("JButton.backgroundColor", rootComponent.getBackground());
		this.project = project;
		for (Field field : ProjectSettingsForm.class.getDeclaredFields()) {
			try {
				Object o = field.get(this);
				if (o instanceof JToggleButton) {
					JToggleButton button = (JToggleButton) o;
					button.addActionListener(new ActionListener() {
						@Override
						public void actionPerformed(ActionEvent e) {
							updateComponents();
						}
					});
				}
				if (o instanceof JTextField) {
					JTextField jTextField = (JTextField) o;
					jTextField.getDocument().addDocumentListener(new DocumentAdapter() {
						@Override
						protected void textChanged(DocumentEvent e) {
							updateComponents();
						}
					});
				}
			} catch (Throwable e) {
				throw new RuntimeException(e);
			}
		}

		eclipsePreferenceFilePathJavaBrowse.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				browseForFile(pathToEclipsePreferenceFileJava, createSingleFileOrFolderDescriptor(), "Select Eclipse workspace/project/config file");
			}
		});
		pathToImportOrderPreferenceFileBrowse.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				browseForFile(pathToImportOrderPreferenceFile, createSingleFileNoJarsDescriptor(), "Select config file");
			}
		});
		customEclipseLocationBrowse.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				browseForFile(pathToCustomEclipse, createSingleFolderDescriptor(), "Select Eclipse location");
				useEclipseCustom.setSelected(true);
			}
		});

		rootComponent.addAncestorListener(new AncestorListener() {
			@Override
			public void ancestorAdded(AncestorEvent event) {
				// Called when component becomes visible, to ensure that the
				// popups
				// are visible when the form is shown for the first time.
				updateComponents();
			}

			@Override
			public void ancestorRemoved(AncestorEvent event) {
			}

			@Override
			public void ancestorMoved(AncestorEvent event) {
			}
		});

		pathToEclipsePreferenceFileJava.getDocument().addDocumentListener(new DocumentAdapter() {
			@Override
			protected void textChanged(DocumentEvent e) {
				setJavaFormatterProfileModel();
			}
		});

		newProfile.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				if (isModified(displayedSettings)) {
					createConfirmation("Profile was modified, save changes to the current profile?", "Yes", "No", new Runnable() {
						@Override
						public void run() {
							try {
								apply();
								createProfile();
							} catch (ConfigurationException e) {
								SwingUtilities.invokeLater(() -> Messages.showMessageDialog(ProjectSettingsForm.this.rootComponent, e.getMessage(),
										e.getTitle(), Messages.getErrorIcon()));
							}
						}
					}, new Runnable() {
						@Override
						public void run() {
							importFrom(displayedSettings);
							createProfile();
						}
					}, 0).showInFocusCenter();
				} else {
					createProfile();
				}
			}

			private void createProfile() {
				Settings settings = GlobalSettings.getInstance().newSettings();
				refreshProfilesModel(ProjectSettingsForm.this.profilesModel);
				profiles.setSelectedItem(settings);
			}
		});
		copyProfile.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (isModified(displayedSettings)) {
					ListPopup confirmation = createConfirmation("Profile was modified, save changes to the current profile?", "Yes", "No", new Runnable() {
						@Override
						public void run() {
							try {
								apply();
								copyProfile();
							} catch (ConfigurationException e) {
								SwingUtilities.invokeLater(() -> Messages.showMessageDialog(ProjectSettingsForm.this.rootComponent, e.getMessage(), e.getTitle(), Messages.getErrorIcon()));
							}
						}
					}, new Runnable() {
						@Override
						public void run() {
							importFrom(displayedSettings);
							copyProfile();
						}
					}, 0);

					confirmation.showInFocusCenter();
				} else {
					copyProfile();
				}
			}

			private void copyProfile() {
				Settings settings = GlobalSettings.getInstance().copySettings(project, displayedSettings);
				refreshProfilesModel(ProjectSettingsForm.this.profilesModel);
				profiles.setSelectedItem(settings);
			}
		});
		exportToProjectProfile.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (isModified(displayedSettings)) {
					ListPopup confirmation = createConfirmation("Profile was modified, save changes to the current profile?", "Yes", "No", new Runnable() {
						@Override
						public void run() {
							try {
								apply();
								exportProfile();
							} catch (ConfigurationException e) {
								SwingUtilities.invokeLater(() -> Messages.showMessageDialog(ProjectSettingsForm.this.rootComponent, e.getMessage(), e.getTitle(), Messages.getErrorIcon()));
							}
						}
					}, new Runnable() {
						@Override
						public void run() {
							exportProfile();
						}
					}, 0);

					confirmation.showInFocusCenter();
				} else {
					exportProfile();
				}
			}

			private void exportProfile() {
				displayedSettings = null;// disables confirmation dialog from combobox listener
				profiles.setSelectedItem(ProjectSettings.getInstance(project).getState().getProjectSpecificProfile());
			}
		});
		setJavaFormatterProfileModel();

		profilesModel = createProfilesModel();
		profiles.setModel(profilesModel);
		profiles.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				// && isSameId()
				if (displayedSettings != null && getSelectedItem() != null && isModified(displayedSettings)) {
					showConfirmationDialogOnProfileChange();
				} else if (displayedSettings != null && getSelectedItem() != null) {
					importFromInternal(getSelectedItem());
				} else if (displayedSettings == null) {
					displayedSettings = getSelectedItem();
				}
			}

		});

		profiles.setRenderer(new ListCellRendererWrapper() {
			@Override
			public void customize(JList jList, Object value, int i, boolean b, boolean b1) {
				if (value != null) {
					setText(((Settings) value).getName());
				}
			}
		});
		rename.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				// Damn you're ugly
				try {
					apply();
					String s = Messages.showInputDialog(rename, "New profile name:", "Rename profile", Messages.getQuestionIcon(), displayedSettings.getName(),
							new NonEmptyInputValidator());
					if (s != null) {
						displayedSettings.setName(s);
						apply();
					}
				} catch (ConfigurationException e1) {
					Messages.showMessageDialog(ProjectSettingsForm.this.rootComponent, e1.getMessage(), e1.getTitle(), Messages.getErrorIcon());
				}
			}
		});
		delete.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				int selectedIndex = profiles.getSelectedIndex();
				GlobalSettings.getInstance().delete(getSelectedItem(), getProject());
				profiles.setModel(profilesModel = createProfilesModel());
				int itemCount = profiles.getItemCount();
				if (selectedIndex < itemCount && selectedIndex >= 0) {
					Object itemAt = profiles.getItemAt(selectedIndex);
					importFromInternal((Settings) itemAt);
					profiles.setSelectedIndex(selectedIndex);
				}
				if (selectedIndex == itemCount && selectedIndex - 1 >= 0) {
					Object itemAt = profiles.getItemAt(selectedIndex - 1);
					importFromInternal((Settings) itemAt);
					profiles.setSelectedIndex(selectedIndex - 1);
				} else {
					profiles.setSelectedIndex(0);
				}

			}
		});
		donateButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				BareBonesBrowserLaunch.openURL(
						"https://www.paypal.me/VojtechKrasa");
			}
		});
		helpButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				BareBonesBrowserLaunch.openURL("https://github.com/krasa/EclipseCodeFormatter#instructions");
			}
		});
		;
		homepage.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				BareBonesBrowserLaunch.openURL("http://plugins.intellij.net/plugin/?idea&id=6546");
			}
		});
		useEclipseNewest.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				importOrdering452.setSelected(true);
			}
		});
		profileHelp.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				Messages.showInfoMessage(project,
						"Close all projects to configure default settings.\n\n"
								+ "<Project Specific> profile is not shared between projects. Other profiles are global - shared, synchronized and stored in the IDE."
								+ "\nChange of a global profile will result in a change in all opened or closed projects using such profile."
								+ "\nThe selected global profile is also fully persisted within a project, but most of the data is used only as a backup for syncing between different computers."
								+ "\n\nPaths macros are automatically managed by the IDE. That can result in '$PROJECT_DIR$' being used for a global profile within a project config file,"
								+ "\nbut an absolute path is actually stored and used in the IDE config file.",
						"Profiles and persistence explanation");
			}
		});
	}

	private void apply() throws ConfigurationException {
		myConfigurable.apply();
	}

	private void setJavaFormatterProfileModel() {
		String selectedProfile = displayedSettings != null ? displayedSettings.getSelectedJavaProfile() : null;
		javaFormatterProfile.setModel(createProfilesModel(pathToEclipsePreferenceFileJava, selectedProfile));
	}

	private ComboBoxModel createProfilesModel(JTextField pathToEclipsePreferenceFile, String selectedProfile) {
		@SuppressWarnings("unchecked")
		SortedComboBoxModel profilesModel = new SortedComboBoxModel(new Comparator<String>() {
			@Override
			public int compare(String o1, String o2) {
				return o1.compareTo(o2);
			}
		});
		String text = pathToEclipsePreferenceFile.getText();
		if (normalBorder == null) {
			this.normalBorder = javaFormatterProfile.getBorder();
		}
		if (!text.isEmpty() && schemeEclipseFile.isSelected()) {
			ConfigFileLocator configFileLocator = new ConfigFileLocator();

			configFileLocator.validate(this, profilesModel, text);
		} else {
			javaFormatterProfile.setEnabled(false);
			javaFormatterProfile.setBorder(this.normalBorder);
		}

		List<String> items = profilesModel.getItems();
		if (items.size() > 0) {
			for (String item : items) {
				if (item.equals(selectedProfile)) {
					profilesModel.setSelectedItem(item);
				}
			}
			if (profilesModel.getSelectedItem() == null) {
				profilesModel.setSelectedItem(items.get(0));
			}
		}

		return profilesModel;
	}

	private SortedComboBoxModel createProfilesModel() {
		// noinspection unchecked
		SortedComboBoxModel settingsSortedComboBoxModel = new SortedComboBoxModel(new Comparator<Settings>() {
			@Override
			public int compare(Settings o1, Settings o2) {
				if (o1.isProjectSpecific()) {
					return -1;
				}
				if (o2.isProjectSpecific()) {
					return 1;
				}
				return o1.getName().compareTo(o2.getName());
			}
		});
		refreshProfilesModel(settingsSortedComboBoxModel);
		return settingsSortedComboBoxModel;
	}

	@NotNull
	private Project getProject() {
		return ProjectSettingsForm.this.project;
	}

	private void showConfirmationDialogOnProfileChange() {
		createConfirmation("Profile was modified, save changes?", "Yes", "No", new Runnable() {
			@Override
			public void run() {
				try {
					apply();
					importFrom(getSelectedItem());
				} catch (ConfigurationException e) {
					profiles.setSelectedItem(displayedSettings);
					SwingUtilities.invokeLater(() -> Messages.showMessageDialog(ProjectSettingsForm.this.rootComponent, e.getMessage(), e.getTitle(), Messages.getErrorIcon()));
				}
			}
		}, new Runnable() {
			@Override
			public void run() {
				importFromInternal(getSelectedItem());
			}
		}, 0).showInCenterOf(profiles);
	}

	private void refreshProfilesModel(SortedComboBoxModel profilesModel) {
		profilesModel.setAll(GlobalSettings.getInstance().getSettingsList());
		Settings projectSpecificProfile = ProjectSettings.getInstance(project).getState().getProjectSpecificProfile();
		profilesModel.add(projectSpecificProfile);
	}

	private Settings getSelectedItem() {
		Object selectedItem = profiles.getSelectedItem();
		return (Settings) selectedItem;
	}

	private boolean browseForFile(@NotNull final JTextField target, FileChooserDescriptor descriptor, String title) {
		descriptor.setHideIgnored(false);

		descriptor.setTitle(title);
		String text = target.getText();
		final VirtualFile toSelect = text == null || text.isEmpty() ? getProject().getBaseDir() : LocalFileSystem.getInstance().findFileByPath(text);

		// 10.5 does not have #chooseFile
		VirtualFile[] virtualFile = FileChooser.chooseFiles(descriptor, getProject(), toSelect);
		if (virtualFile != null && virtualFile.length > 0) {
			target.setText(virtualFile[0].getPath());
			return true;
		}
		return false;
	}

	private void enabledBy(@NotNull JComponent[] targets, @NotNull JToggleButton... control) {
		boolean b = true;
		for (JToggleButton jToggleButton : control) {
			b = b && (jToggleButton.isEnabled() && jToggleButton.isSelected());
		}
		for (JComponent target : targets) {
			target.setEnabled(b);
		}
	}

	private void enabledByAny(@NotNull JComponent[] targets, @NotNull JToggleButton... control) {
		boolean b = false;
		for (JToggleButton jToggleButton : control) {
			b = b || (jToggleButton.isEnabled() && jToggleButton.isSelected());
		}
		for (JComponent target : targets) {
			target.setEnabled(b);
		}
	}

	private void hidePopups() {
		for (Iterator<Popup> it = visiblePopups.iterator(); it.hasNext(); ) {
			Popup popup = it.next();
			popup.hide();
			it.remove();
		}
	}

	@NotNull
	public JPanel getRootComponent() {
		return rootComponent;
	}

	public void importFrom(@NotNull Settings in) {
		// this needs to be before setting displayedSettings so we can disable action listener
		// and also when we import already displayed settings == reset, no notification is needed.
		if (displayedSettings == null || in != displayedSettings) {
			profiles.setSelectedItem(in);
		}

		importFromInternal(in);
	}

	/**
	 * does not update profiles DropDown
	 */
	private void importFromInternal(Settings in) {
		displayedSettings = in;
		formatOtherFilesWithExceptionsRadioButton.setSelected(in.isFormatOtherFileTypesWithIntelliJ());
		doNotFormatOtherFilesRadioButton.setSelected(!in.isFormatOtherFileTypesWithIntelliJ());
		useDefaultFormatter.setSelected(in.getFormatter().equals(Settings.Formatter.DEFAULT));
		useEclipseFormatter.setSelected(in.getFormatter().equals(Settings.Formatter.ECLIPSE));
		useEclipseNewest.setSelected(in.getEclipseVersion().equals(Settings.FormatterVersion.NEWEST));
		useEclipseCustom.setSelected(in.getEclipseVersion().equals(Settings.FormatterVersion.CUSTOM));
		importOrdering451.setSelected(in.getImportOrdering().equals(Settings.ImportOrdering.ECLIPSE_44));
		importOrdering452.setSelected(in.getImportOrdering().equals(Settings.ImportOrdering.ECLIPSE_452));
		importOrderConfigurationFromFileRadioButton.setSelected(in.isImportOrderFromFile());
		importOrderConfigurationManualRadioButton.setSelected(!in.isImportOrderFromFile());
		javaFormatterProfile.setSelectedItem(in.getSelectedJavaProfile());

		schemeEclipse.setSelected(in.getProfileScheme().equals(Settings.ProfileScheme.ECLIPSE));
		schemeCurrentProject.setSelected(in.getProfileScheme().equals(Settings.ProfileScheme.RESOLVE));
		schemeEclipse21.setSelected(in.getProfileScheme().equals(Settings.ProfileScheme.ECLIPSE_2_1));
		schemeEclipseJC.setSelected(in.getProfileScheme().equals(Settings.ProfileScheme.JAVA_CONVENTIONS));
		schemeEclipseFile.setSelected(in.getProfileScheme().equals(Settings.ProfileScheme.CUSTOM));

		setData(in);
		updateComponents();
	}

	public Settings exportDisplayedSettings() {
		if (useEclipseFormatter.isSelected()) {
			displayedSettings.setFormatter(Settings.Formatter.ECLIPSE);
		} else {
			displayedSettings.setFormatter(Settings.Formatter.DEFAULT);
		}
		if (importOrdering451.isSelected()) {
			displayedSettings.setImportOrdering(Settings.ImportOrdering.ECLIPSE_44);
		} else if (importOrdering452.isSelected()) {
			displayedSettings.setImportOrdering(Settings.ImportOrdering.ECLIPSE_452);
		}
		if (useEclipseNewest.isSelected()) {
			displayedSettings.setEclipseVersion(Settings.FormatterVersion.NEWEST);
		} else if (useEclipseCustom.isSelected()) {
			displayedSettings.setEclipseVersion(Settings.FormatterVersion.CUSTOM);
		}

		if (schemeEclipse.isSelected()) {
			displayedSettings.setProfileScheme(Settings.ProfileScheme.ECLIPSE);
		} else if (schemeEclipse21.isSelected()) {
			displayedSettings.setProfileScheme(Settings.ProfileScheme.ECLIPSE_2_1);
		} else if (schemeEclipseJC.isSelected()) {
			displayedSettings.setProfileScheme(Settings.ProfileScheme.JAVA_CONVENTIONS);
		} else if (schemeEclipseFile.isSelected()) {
			displayedSettings.setProfileScheme(Settings.ProfileScheme.CUSTOM);
		} else if (schemeCurrentProject.isSelected()) {
			displayedSettings.setProfileScheme(Settings.ProfileScheme.RESOLVE);
		}


		displayedSettings.setFormatOtherFileTypesWithIntelliJ(formatOtherFilesWithExceptionsRadioButton.isSelected());
		displayedSettings.setImportOrderFromFile(importOrderConfigurationFromFileRadioButton.isSelected());
		displayedSettings.setSelectedJavaProfile(profileCheck(javaFormatterProfile.getSelectedItem()));
		getData(displayedSettings);
		return displayedSettings;
	}

	private String profileCheck(final Object selectedItem) {
		final String selectedItem1 = (String) selectedItem;
		if (isErrorProfile(selectedItem1)) {
			return null;
		}
		return selectedItem1;
	}

	private boolean isErrorProfile(String selectedItem1) {
		return ProjectSettingsForm.ERROR_BORDER.equals(javaFormatterProfile.getBorder()) || NOT_EXISTS.equals(selectedItem1) || PARSING_FAILED.equals(selectedItem1) || CONTAINS_NO_PROFILES.equals(selectedItem1);
	}

	public void validate() throws ConfigurationException {
		if (pathToEclipsePreferenceFileJava.isEnabled()) {
			if (StringUtils.isBlank(pathToEclipsePreferenceFileJava.getText())) {
				throw new ConfigurationException("Path to Java config file is not valid");
			}
			if (!new File(pathToEclipsePreferenceFileJava.getText()).exists()) {
				throw new ConfigurationException("Path to Java config file is not valid - the file does not exist");
			}
		}
		if (pathToImportOrderPreferenceFile.isEnabled()) {
			if (StringUtils.isBlank(pathToImportOrderPreferenceFile.getText())) {
				throw new ConfigurationException("Path to Import Order file is not valid");
			}
			if (!new File(pathToImportOrderPreferenceFile.getText()).exists()) {
				throw new ConfigurationException("Path to Import Order file is not valid - the file does not exist");
			}
		}
		if (pathToCustomEclipse.isEnabled()) {
			if (StringUtils.isBlank(pathToCustomEclipse.getText())) {
				throw new ConfigurationException("Path to custom Eclipse folder is not valid");
			}
			if (!new File(pathToCustomEclipse.getText()).exists()) {
				throw new ConfigurationException("Path to custom Eclipse folder is not valid - folder does not exist");
			}
		}
	}

	private boolean customIsModified(Settings data) {
		if (!ObjectUtils.equals(javaFormatterProfile.getSelectedItem(), data.getSelectedJavaProfile()) && !isErrorProfile(data.getSelectedJavaProfile())) {
			return true;
		}
		if (useDefaultFormatter.isSelected() != data.getFormatter().equals(Settings.Formatter.DEFAULT)) {
			return true;
		}
		if (useEclipseFormatter.isSelected() != data.getFormatter().equals(Settings.Formatter.ECLIPSE)) {
			return true;
		}
		if (useEclipseNewest.isSelected() != data.getEclipseVersion().equals(Settings.FormatterVersion.NEWEST)) {
			return true;
		}
		if (useEclipseCustom.isSelected() != data.getEclipseVersion().equals(Settings.FormatterVersion.CUSTOM)) {
			return true;
		}
		if (importOrdering451.isSelected() != data.getImportOrdering().equals(Settings.ImportOrdering.ECLIPSE_44)) {
			return true;
		}
		if (importOrdering452.isSelected() != data.getImportOrdering().equals(Settings.ImportOrdering.ECLIPSE_452)) {
			return true;
		}

		if (schemeCurrentProject.isSelected() != data.getProfileScheme().equals(Settings.ProfileScheme.RESOLVE)) {
			return true;
		}
		if (schemeEclipse.isSelected() != data.getProfileScheme().equals(Settings.ProfileScheme.ECLIPSE)) {
			return true;
		}
		if (schemeEclipseFile.isSelected() != data.getProfileScheme().equals(Settings.ProfileScheme.CUSTOM)) {
			return true;
		}
		if (schemeEclipseJC.isSelected() != data.getProfileScheme().equals(Settings.ProfileScheme.JAVA_CONVENTIONS)) {
			return true;
		}
		if (schemeEclipse21.isSelected() != data.getProfileScheme().equals(Settings.ProfileScheme.ECLIPSE_2_1)) {
			return true;
		}


		if (formatOtherFilesWithExceptionsRadioButton.isSelected() != data.isFormatOtherFileTypesWithIntelliJ()) {
			return true;
		}
		if (doNotFormatOtherFilesRadioButton.isSelected() == data.isFormatOtherFileTypesWithIntelliJ()) {
			return true;
		}
		if (importOrderConfigurationFromFileRadioButton.isSelected() != data.isImportOrderFromFile()) {
			return true;
		}
		return false;
	}

	public Settings getDisplayedSettings() {
		return displayedSettings;
	}

	public ListPopup createConfirmation(String title, final String yesText, String noText, final Runnable onYes, final Runnable onNo, int defaultOptionIndex) {

		final BaseListPopupStep<String> step = new BaseListPopupStep<String>(title, new String[]{yesText, noText}) {
			@Override
			public PopupStep onChosen(String selectedValue, final boolean finalChoice) {
				if (selectedValue.equals(yesText)) {
					onYes.run();
				} else {
					onNo.run();
				}
				return FINAL_CHOICE;
			}

			@Override
			public void canceled() {
			}

			@Override
			public boolean isMnemonicsNavigationEnabled() {
				return true;
			}
		};
		step.setDefaultOptionIndex(defaultOptionIndex);

		final ApplicationEx app = ApplicationManagerEx.getApplicationEx();
		return app == null || !app.isUnitTestMode() ? new ListPopupImpl(step) : new MockConfirmation(step, yesText);
	}

	private void createUIComponents() {
		// TODO: place custom component creation code here
	}

	public void setData(Settings data) {
		optimizeImportsCheckBox.setSelected(data.isOptimizeImports());
		formatSelectedTextInAllFileTypes.setSelected(data.isFormatSeletedTextInAllFileTypes());
		pathToEclipsePreferenceFileJava.setText(data.getPathToConfigFileJava());
		disabledFileTypes.setText(data.getDisabledFileTypes());
		enableJavaFormatting.setSelected(data.isEnableJavaFormatting());
		importOrder.setText(data.getImportOrder());
		pathToImportOrderPreferenceFile.setText(data.getImportOrderConfigFilePath());
		useForLiveTemplates.setSelected(data.isUseForLiveTemplates());
		pathToCustomEclipse.setText(data.getPathToEclipse());
	}

	public void getData(Settings data) {
		data.setOptimizeImports(optimizeImportsCheckBox.isSelected());
		data.setFormatSeletedTextInAllFileTypes(formatSelectedTextInAllFileTypes.isSelected());
		data.setPathToConfigFileJava(pathToEclipsePreferenceFileJava.getText());
		data.setDisabledFileTypes(disabledFileTypes.getText());
		data.setEnableJavaFormatting(enableJavaFormatting.isSelected());
		data.setImportOrder(importOrder.getText());
		data.setImportOrderConfigFilePath(pathToImportOrderPreferenceFile.getText());
		data.setUseForLiveTemplates(useForLiveTemplates.isSelected());
		data.setPathToEclipse(pathToCustomEclipse.getText());
	}

	public boolean isModified(Settings data) {
		// TODO THIS IS VERY IMPORTANT
		if (customIsModified(data)) {
			return true;
		}
		if (optimizeImportsCheckBox.isSelected() != data.isOptimizeImports())
			return true;
		if (formatSelectedTextInAllFileTypes.isSelected() != data.isFormatSeletedTextInAllFileTypes())
			return true;
		if (pathToEclipsePreferenceFileJava.getText() != null ? !pathToEclipsePreferenceFileJava.getText().equals(data.getPathToConfigFileJava())
				: data.getPathToConfigFileJava() != null)
			return true;
		if (disabledFileTypes.getText() != null ? !disabledFileTypes.getText().equals(data.getDisabledFileTypes()) : data.getDisabledFileTypes() != null)
			return true;
		if (enableJavaFormatting.isSelected() != data.isEnableJavaFormatting())
			return true;
		if (importOrder.getText() != null ? !importOrder.getText().equals(data.getImportOrder()) : data.getImportOrder() != null)
			return true;
		if (pathToImportOrderPreferenceFile.getText() != null ? !pathToImportOrderPreferenceFile.getText().equals(data.getImportOrderConfigFilePath())
				: data.getImportOrderConfigFilePath() != null)
			return true;
		if (useForLiveTemplates.isSelected() != data.isUseForLiveTemplates())
			return true;
		if (pathToCustomEclipse.getText() != null ? !pathToCustomEclipse.getText().equals(data.getPathToEclipse()) : data.getPathToEclipse() != null)
			return true;
		return false;
	}
}
