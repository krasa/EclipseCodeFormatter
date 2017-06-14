/*
 * External Code Formatter Copyright (c) 2007-2009 Esko Luontola, www.orfjackal.net Licensed under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for
 * the specific language governing permissions and limitations under the License.
 */

package krasa.formatter.plugin;

import static com.intellij.openapi.fileChooser.FileChooserDescriptorFactory.createSingleFileNoJarsDescriptor;
import static com.intellij.openapi.fileChooser.FileChooserDescriptorFactory.createSingleFolderDescriptor;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import javax.swing.*;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import javax.swing.event.DocumentEvent;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;

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
import com.intellij.ui.DocumentAdapter;
import com.intellij.ui.ListCellRendererWrapper;
import com.intellij.ui.SortedComboBoxModel;
import com.intellij.ui.popup.list.ListPopupImpl;
import com.intellij.ui.popup.mock.MockConfirmation;

import krasa.formatter.exception.ParsingFailedException;
import krasa.formatter.settings.GlobalSettings;
import krasa.formatter.settings.MyConfigurable;
import krasa.formatter.settings.ProjectSettings;
import krasa.formatter.settings.Settings;
import krasa.formatter.utils.FileUtils;

/**
 * Configuration dialog for changing the {@link krasa.formatter.settings.Settings} of the plugin.
 *
 * @author Esko Luontola
 * @author Vojtech Krasa
 * @since 4.12.2007
 */
public class ProjectSettingsForm {
	private static final Logger LOG = Logger.getInstance(ProjectSettingsForm.class.getName());
	public static final String PARSING_FAILED = "PARSING FAILED";

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
	private JLabel eclipsePreferenceFileJSLabel;

	private JTextField pathToEclipsePreferenceFileJava;
	private JTextField pathToEclipsePreferenceFileJS;

	private JLabel eclipsePrefsExample;
	private JLabel eclipsePrefsExampleJS;

	private JCheckBox enableJavaFormatting;
	private JCheckBox enableJSFormatting;

	private JButton eclipsePreferenceFilePathJavaBrowse;
	private JButton eclipsePreferenceFilePathJSBrowse;

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
	private JButton DONATEButton;
	private JComboBox javaFormatterProfile;
	private JLabel javaFormatterProfileLabel;
	private JButton helpButton;
	private JButton homepage;
	private JCheckBox enableGWTNativeMethodsCheckBox;
	private JCheckBox enableJavaScriptCommentsPostProcessor;
	private JLabel formatterProfileLabelJS;
	private JComboBox formatterProfileJS;
	private JCheckBox useForLiveTemplates;

	private JCheckBox enableCppFormatting;
	private JComboBox formatterProfileCpp;
	private JTextField pathToEclipsePreferenceFileCpp;
	private JLabel eclipsePreferenceFileCppLabel;
	private JButton eclipsePreferenceFilePathCppBrowse;
	private JLabel formatterProfileLabelCpp;
	private JLabel eclipsePrefsExampleCpp;
	private JTextField pathToCustomEclipse;
	private JButton customEclipseLocationBrowse;
	private JRadioButton useEclipse44;
	private JRadioButton useEclipseNewest;
	private JRadioButton useEclipseCustom;
	private JLabel javaFormatterVersionLabel;
	private JRadioButton importOrdering451;
	private JRadioButton importOrdering452;
	private JButton profileHelp;

	private final List<Popup> visiblePopups = new ArrayList<Popup>();
	@NotNull
	private Project project;
	protected SortedComboBoxModel profilesModel;
	private MyConfigurable myConfigurable;

	private void updateComponents() {
		hidePopups();
		enabledBy(new JComponent[] { eclipseSupportedFileTypesLabel, enableJavaFormatting, enableJSFormatting,
				enableCppFormatting, doNotFormatOtherFilesRadioButton, formatOtherFilesWithExceptionsRadioButton,
				importOrderPreferenceFileExample, importOrderConfigurationFromFileRadioButton,
				importOrderConfigurationManualRadioButton, useEclipse44, useEclipseNewest, useEclipseCustom,
				formatSelectedTextInAllFileTypes, useForLiveTemplates, importOrdering451, importOrdering452 },
				useEclipseFormatter);

		enabledBy(new JComponent[] { pathToEclipsePreferenceFileJava, eclipsePrefsExample,
				eclipsePreferenceFileJavaLabel, optimizeImportsCheckBox, eclipsePreferenceFilePathJavaBrowse,
				javaFormatterProfileLabel, javaFormatterProfile, enableGWTNativeMethodsCheckBox,
				customEclipseLocationBrowse, pathToCustomEclipse, useEclipse44, useEclipseNewest, useEclipseCustom,
				javaFormatterVersionLabel, importOrdering451, importOrdering452 }, enableJavaFormatting);

		enabledBy(new JComponent[] { pathToCustomEclipse, customEclipseLocationBrowse, }, useEclipseCustom);

		enabledBy(
				new JComponent[] { importOrder, pathToImportOrderPreferenceFile, pathToImportOrderPreferenceFileBrowse,
						importOrderManualExample, importOrderLabel, importOrderPreferenceFileExample,
						importOrderConfigurationFromFileRadioButton, importOrderConfigurationManualRadioButton },
				optimizeImportsCheckBox);

		enabledBy(new JComponent[] { pathToImportOrderPreferenceFile, importOrderPreferenceFileExample,
				pathToImportOrderPreferenceFileBrowse }, importOrderConfigurationFromFileRadioButton);

		enabledBy(new JComponent[] { importOrder, importOrderManualExample, },
				importOrderConfigurationManualRadioButton);

		enabledByAny(new JComponent[] { pathToEclipsePreferenceFileJS, formatterProfileLabelJS, formatterProfileJS,
				eclipsePrefsExampleJS, eclipsePreferenceFileJSLabel, eclipsePreferenceFilePathJSBrowse,
				enableJavaScriptCommentsPostProcessor }, enableJSFormatting, enableGWTNativeMethodsCheckBox);

		enabledByAny(
				new JComponent[] { pathToEclipsePreferenceFileCpp, formatterProfileLabelCpp, formatterProfileCpp,
						eclipsePrefsExampleCpp, eclipsePreferenceFileCppLabel, eclipsePreferenceFilePathCppBrowse },
				enableCppFormatting);

		enabledBy(new JComponent[] { disabledFileTypes, disabledFileTypesHelpLabel, },
				formatOtherFilesWithExceptionsRadioButton);

		disableJavaProfilesIfNecessary();
		disableJavaScriptProfilesIfNecessary();
		disableCppProfilesIfNecessary();

		delete.setEnabled(!displayedSettings.isProjectSpecific());
		rename.setEnabled(!displayedSettings.isProjectSpecific());
		exportToProjectProfile.setEnabled(!displayedSettings.isProjectSpecific());
	}

	private void enabledByAny(@NotNull JComponent[] targets, @NotNull JToggleButton[] negated,
			@NotNull JToggleButton... control) {
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
		if (!text.endsWith("xml")) {
			javaFormatterProfile.setEnabled(false);
		}
	}

	private void disableJavaScriptProfilesIfNecessary() {
		String text = pathToEclipsePreferenceFileJS.getText();
		if (!text.endsWith("xml")) {
			formatterProfileJS.setEnabled(false);
		}
	}

	private void disableCppProfilesIfNecessary() {
		String text = pathToEclipsePreferenceFileCpp.getText();
		if (!text.endsWith("xml")) {
			formatterProfileCpp.setEnabled(false);
		}
	}

	public ProjectSettingsForm(final Project project, MyConfigurable myConfigurable) {
		this.myConfigurable = myConfigurable;
		DONATEButton.setBorder(BorderFactory.createEmptyBorder());
		DONATEButton.setContentAreaFilled(false);
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
				browseForFile(pathToEclipsePreferenceFileJava, createSingleFileNoJarsDescriptor(),
						"Select config file");
			}
		});
		pathToImportOrderPreferenceFileBrowse.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				browseForFile(pathToImportOrderPreferenceFile, createSingleFileNoJarsDescriptor(),
						"Select config file");
			}
		});
		eclipsePreferenceFilePathJSBrowse.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				browseForFile(pathToEclipsePreferenceFileJS, createSingleFileNoJarsDescriptor(), "Select config file");
			}
		});
		eclipsePreferenceFilePathCppBrowse.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				browseForFile(pathToEclipsePreferenceFileCpp, createSingleFileNoJarsDescriptor(), "Select config file");
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

		pathToEclipsePreferenceFileJS.getDocument().addDocumentListener(new DocumentAdapter() {
			@Override
			protected void textChanged(DocumentEvent e) {
				setJavaScriptFormatterProfileModel();
			}
		});

		pathToEclipsePreferenceFileCpp.getDocument().addDocumentListener(new DocumentAdapter() {
			@Override
			protected void textChanged(DocumentEvent e) {
				setCppFormatterProfileModel();
			}
		});

		newProfile.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				if (isModified(displayedSettings)) {
					createConfirmation("Profile was modified, save changes to the current profile?", "Yes", "No",
							new Runnable() {
								@Override
								public void run() {
									try {
										apply();
										createProfile();
									} catch (ConfigurationException e) {
										Messages.showMessageDialog(ProjectSettingsForm.this.rootComponent,
												e.getMessage(), e.getTitle(), Messages.getErrorIcon());
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
					ListPopup confirmation = createConfirmation(
							"Profile was modified, save changes to the current profile?", "Yes", "No", new Runnable() {
								@Override
								public void run() {
									try {
										apply();
										copyProfile();
									} catch (ConfigurationException e) {
										Messages.showMessageDialog(ProjectSettingsForm.this.rootComponent,
												e.getMessage(), e.getTitle(), Messages.getErrorIcon());
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
					ListPopup confirmation = createConfirmation(
							"Profile was modified, save changes to the current profile?", "Yes", "No", new Runnable() {
								@Override
								public void run() {
									try {
										apply();
										exportProfile();
									} catch (ConfigurationException e) {
										Messages.showMessageDialog(ProjectSettingsForm.this.rootComponent,
												e.getMessage(), e.getTitle(), Messages.getErrorIcon());
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
		setJavaScriptFormatterProfileModel();
		setCppFormatterProfileModel();

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
					String s = Messages.showInputDialog(rename, "New profile name:", "Rename profile",
							Messages.getQuestionIcon(), displayedSettings.getName(), new NonEmptyInputValidator());
					if (s != null) {
						displayedSettings.setName(s);
						apply();
					}
				} catch (ConfigurationException e1) {
					Messages.showMessageDialog(ProjectSettingsForm.this.rootComponent, e1.getMessage(), e1.getTitle(),
							Messages.getErrorIcon());
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
		DONATEButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				BareBonesBrowserLaunch.openURL(
						"https://www.paypal.com/cgi-bin/webscr?cmd=_donations&business=75YN7U7H7D7XU&lc=CZ&item_name=Eclipse%20code%20formatter%20%2d%20IntelliJ%20plugin%20%2d%20Donation&currency_code=USD&bn=PP%2dDonationsBF%3abtn_donateCC_LG%2egif%3aNonHostedGuest");
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
		useEclipse44.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				importOrdering451.setSelected(true);

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
						"<Project Specific> profile is not shared between projects. Other profiles are global - shared, synchronized and stored in the IDE."
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

	private void setJavaScriptFormatterProfileModel() {
		String selectedProfile = displayedSettings != null ? displayedSettings.getSelectedJavaScriptProfile() : null;
		formatterProfileJS.setModel(createProfilesModel(pathToEclipsePreferenceFileJS, selectedProfile));
	}

	private void setCppFormatterProfileModel() {
		String selectedProfile = displayedSettings != null ? displayedSettings.getSelectedCppProfile() : null;
		formatterProfileCpp.setModel(createProfilesModel(pathToEclipsePreferenceFileCpp, selectedProfile));
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
		if (!text.isEmpty()) {
			if (text.endsWith("xml")) {
				try {
					profilesModel.addAll(FileUtils.getProfileNamesFromConfigXML(new File(text)));
				} catch (ParsingFailedException e) {
					profilesModel.add(PARSING_FAILED);
				}
			} else {
				// not xml
			}
		} else {
			// empty
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
					Messages.showMessageDialog(ProjectSettingsForm.this.rootComponent, e.getMessage(), e.getTitle(),
							Messages.getErrorIcon());
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
		final VirtualFile toSelect = text == null || text.isEmpty() ? getProject().getBaseDir()
				: LocalFileSystem.getInstance().findFileByPath(text);

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
		useEclipse44.setSelected(in.getEclipseVersion().equals(Settings.FormatterVersion.ECLIPSE_44));
		useEclipseNewest.setSelected(in.getEclipseVersion().equals(Settings.FormatterVersion.NEWEST));
		useEclipseCustom.setSelected(in.getEclipseVersion().equals(Settings.FormatterVersion.CUSTOM));
		importOrdering451.setSelected(in.getImportOrdering().equals(Settings.ImportOrdering.ECLIPSE_44));
		importOrdering452.setSelected(in.getImportOrdering().equals(Settings.ImportOrdering.ECLIPSE_452));
		importOrderConfigurationFromFileRadioButton.setSelected(in.isImportOrderFromFile());
		importOrderConfigurationManualRadioButton.setSelected(!in.isImportOrderFromFile());
		javaFormatterProfile.setSelectedItem(in.getSelectedJavaProfile());
		formatterProfileJS.setSelectedItem(in.getSelectedJavaScriptProfile());
		formatterProfileCpp.setSelectedItem(in.getSelectedCppProfile());
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
		if (useEclipse44.isSelected()) {
			displayedSettings.setEclipseVersion(Settings.FormatterVersion.ECLIPSE_44);
		} else if (useEclipseNewest.isSelected()) {
			displayedSettings.setEclipseVersion(Settings.FormatterVersion.NEWEST);
		} else if (useEclipseCustom.isSelected()) {
			displayedSettings.setEclipseVersion(Settings.FormatterVersion.CUSTOM);
		}

		displayedSettings.setFormatOtherFileTypesWithIntelliJ(formatOtherFilesWithExceptionsRadioButton.isSelected());
		displayedSettings.setImportOrderFromFile(importOrderConfigurationFromFileRadioButton.isSelected());
		displayedSettings.setSelectedJavaProfile(profileCheck(javaFormatterProfile.getSelectedItem()));
		displayedSettings.setSelectedJavaScriptProfile(profileCheck(formatterProfileJS.getSelectedItem()));
		displayedSettings.setSelectedCppProfile(profileCheck(formatterProfileCpp.getSelectedItem()));
		getData(displayedSettings);
		return displayedSettings;
	}

	private String profileCheck(final Object selectedItem) {
		final String selectedItem1 = (String) selectedItem;
		if (PARSING_FAILED.equals(selectedItem1)) {
			return null;
		}
		return selectedItem1;
	}

	public void validate() throws ConfigurationException {
		if (pathToEclipsePreferenceFileJava.isEnabled()) {
			if (StringUtils.isBlank(pathToEclipsePreferenceFileJava.getText())) {
				throw new ConfigurationException("Path to Java config file is not valid");
			}
			if (!new File(pathToEclipsePreferenceFileJava.getText()).exists()) {
				throw new ConfigurationException("Path to Java config file is not valid - file does not exist");
			}
		}
		if (pathToEclipsePreferenceFileJS.isEnabled()) {
			if (StringUtils.isBlank(pathToEclipsePreferenceFileJS.getText())) {
				throw new ConfigurationException("Path to JS config file is not valid");
			}
			if (!new File(pathToEclipsePreferenceFileJS.getText()).exists()) {
				throw new ConfigurationException("Path to JS config file is not valid - file does not exist");
			}
		}
		if (pathToImportOrderPreferenceFile.isEnabled()) {
			if (StringUtils.isBlank(pathToImportOrderPreferenceFile.getText())) {
				throw new ConfigurationException("Path to Import Order file is not valid");
			}
			if (!new File(pathToImportOrderPreferenceFile.getText()).exists()) {
				throw new ConfigurationException("Path to Import Order file is not valid - file does not exist");
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
		if (!ObjectUtils.equals(javaFormatterProfile.getSelectedItem(), data.getSelectedJavaProfile())) {
			return true;
		}
		if (!ObjectUtils.equals(formatterProfileJS.getSelectedItem(), data.getSelectedJavaScriptProfile())) {
			return true;
		}
		if (!ObjectUtils.equals(formatterProfileCpp.getSelectedItem(), data.getSelectedCppProfile())) {
			return true;
		}
		if (useDefaultFormatter.isSelected() != data.getFormatter().equals(Settings.Formatter.DEFAULT)) {
			return true;
		}
		if (useEclipseFormatter.isSelected() != data.getFormatter().equals(Settings.Formatter.ECLIPSE)) {
			return true;
		}
		if (useEclipse44.isSelected() != data.getEclipseVersion().equals(Settings.FormatterVersion.ECLIPSE_44)) {
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

	public ListPopup createConfirmation(String title, final String yesText, String noText, final Runnable onYes,
			final Runnable onNo, int defaultOptionIndex) {

		final BaseListPopupStep<String> step = new BaseListPopupStep<String>(title, new String[] { yesText, noText }) {
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
		pathToEclipsePreferenceFileJS.setText(data.getPathToConfigFileJS());
		disabledFileTypes.setText(data.getDisabledFileTypes());
		enableJSFormatting.setSelected(data.isEnableJSFormatting());
		enableJavaFormatting.setSelected(data.isEnableJavaFormatting());
		importOrder.setText(data.getImportOrder());
		pathToImportOrderPreferenceFile.setText(data.getImportOrderConfigFilePath());
		enableJavaScriptCommentsPostProcessor.setSelected(data.isEnableJSProcessor());
		useForLiveTemplates.setSelected(data.isUseForLiveTemplates());
		enableCppFormatting.setSelected(data.isEnableCppFormatting());
		pathToEclipsePreferenceFileCpp.setText(data.getPathToConfigFileCpp());
		enableGWTNativeMethodsCheckBox.setSelected(data.isEnableGWT());
		pathToCustomEclipse.setText(data.getPathToEclipse());
	}

	public void getData(Settings data) {
		data.setOptimizeImports(optimizeImportsCheckBox.isSelected());
		data.setFormatSeletedTextInAllFileTypes(formatSelectedTextInAllFileTypes.isSelected());
		data.setPathToConfigFileJava(pathToEclipsePreferenceFileJava.getText());
		data.setPathToConfigFileJS(pathToEclipsePreferenceFileJS.getText());
		data.setDisabledFileTypes(disabledFileTypes.getText());
		data.setEnableJSFormatting(enableJSFormatting.isSelected());
		data.setEnableJavaFormatting(enableJavaFormatting.isSelected());
		data.setImportOrder(importOrder.getText());
		data.setImportOrderConfigFilePath(pathToImportOrderPreferenceFile.getText());
		data.setEnableJSProcessor(enableJavaScriptCommentsPostProcessor.isSelected());
		data.setUseForLiveTemplates(useForLiveTemplates.isSelected());
		data.setEnableCppFormatting(enableCppFormatting.isSelected());
		data.setPathToConfigFileCpp(pathToEclipsePreferenceFileCpp.getText());
		data.setEnableGWT(enableGWTNativeMethodsCheckBox.isSelected());
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
		if (pathToEclipsePreferenceFileJava.getText() != null
				? !pathToEclipsePreferenceFileJava.getText().equals(data.getPathToConfigFileJava())
				: data.getPathToConfigFileJava() != null)
			return true;
		if (pathToEclipsePreferenceFileJS.getText() != null
				? !pathToEclipsePreferenceFileJS.getText().equals(data.getPathToConfigFileJS())
				: data.getPathToConfigFileJS() != null)
			return true;
		if (disabledFileTypes.getText() != null ? !disabledFileTypes.getText().equals(data.getDisabledFileTypes())
				: data.getDisabledFileTypes() != null)
			return true;
		if (enableJSFormatting.isSelected() != data.isEnableJSFormatting())
			return true;
		if (enableJavaFormatting.isSelected() != data.isEnableJavaFormatting())
			return true;
		if (importOrder.getText() != null ? !importOrder.getText().equals(data.getImportOrder())
				: data.getImportOrder() != null)
			return true;
		if (pathToImportOrderPreferenceFile.getText() != null
				? !pathToImportOrderPreferenceFile.getText().equals(data.getImportOrderConfigFilePath())
				: data.getImportOrderConfigFilePath() != null)
			return true;
		if (enableJavaScriptCommentsPostProcessor.isSelected() != data.isEnableJSProcessor())
			return true;
		if (useForLiveTemplates.isSelected() != data.isUseForLiveTemplates())
			return true;
		if (enableCppFormatting.isSelected() != data.isEnableCppFormatting())
			return true;
		if (pathToEclipsePreferenceFileCpp.getText() != null
				? !pathToEclipsePreferenceFileCpp.getText().equals(data.getPathToConfigFileCpp())
				: data.getPathToConfigFileCpp() != null)
			return true;
		if (enableGWTNativeMethodsCheckBox.isSelected() != data.isEnableGWT())
			return true;
		if (pathToCustomEclipse.getText() != null ? !pathToCustomEclipse.getText().equals(data.getPathToEclipse())
				: data.getPathToEclipse() != null)
			return true;
		return false;
	}
}
