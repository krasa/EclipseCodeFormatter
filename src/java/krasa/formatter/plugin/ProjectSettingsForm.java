/*
 * External Code Formatter Copyright (c) 2007-2009 Esko Luontola, www.orfjackal.net Licensed under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for
 * the specific language governing permissions and limitations under the License.
 */

package krasa.formatter.plugin;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.swing.*;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import javax.swing.event.DocumentEvent;

import krasa.formatter.settings.GlobalSettings;
import krasa.formatter.settings.Settings;
import krasa.formatter.utils.FileUtils;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;

import com.centerkey.utils.BareBonesBrowserLaunch;
import com.intellij.ide.ui.ListCellRendererWrapper;
import com.intellij.openapi.application.ex.ApplicationEx;
import com.intellij.openapi.application.ex.ApplicationManagerEx;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.fileChooser.FileChooser;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.ui.popup.JBPopup;
import com.intellij.openapi.ui.popup.JBPopupListener;
import com.intellij.openapi.ui.popup.LightweightWindowEvent;
import com.intellij.openapi.ui.popup.ListPopup;
import com.intellij.openapi.ui.popup.PopupStep;
import com.intellij.openapi.ui.popup.util.BaseListPopupStep;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.DocumentAdapter;
import com.intellij.ui.SortedComboBoxModel;
import com.intellij.ui.popup.PopupFactoryImpl;
import com.intellij.ui.popup.list.ListPopupImpl;
import com.intellij.ui.popup.mock.MockConfirmation;

/**
 * Configuration dialog for changing the {@link krasa.formatter.settings.Settings} of the plugin.
 * 
 * @author Esko Luontola
 * @author Vojtech Krasa
 * @since 4.12.2007
 */
public class ProjectSettingsForm {
	private static final Logger LOG = Logger.getInstance(ProjectSettingsForm.class.getName());

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
	private JButton delete;
	private Settings displayedSettings;
	private Settings projectSettings;
	private JButton DONATEButton;
	private JComboBox javaFormatterProfile;
	private JLabel javaFormatterProfileLabel;
	private JButton helpButton;
	private JButton homepage;
	private JCheckBox enableGWTNativeMethodsCheckBox;
	private JCheckBox enableJavaScriptCommentsPostProcessor;
	private JLabel javaScriptFormatterProfileLabel;
	private JComboBox javaScriptFormatterProfile;

	private final List<Popup> visiblePopups = new ArrayList<Popup>();
	@NotNull
	private Project project;
	protected SortedComboBoxModel<Settings> profilesModel;
	protected AtomicBoolean profileChangedValidation = new AtomicBoolean();

	private void updateComponents() {
		hidePopups();
		enabledBy(new JComponent[] { eclipseSupportedFileTypesLabel, enableJavaFormatting, enableJSFormatting,
				doNotFormatOtherFilesRadioButton, formatOtherFilesWithExceptionsRadioButton,
				importOrderPreferenceFileExample, importOrderConfigurationFromFileRadioButton,
				importOrderConfigurationManualRadioButton, formatSelectedTextInAllFileTypes, }, useEclipseFormatter);

		enabledBy(new JComponent[] { pathToEclipsePreferenceFileJava, eclipsePrefsExample,
				eclipsePreferenceFileJavaLabel, optimizeImportsCheckBox, eclipsePreferenceFilePathJavaBrowse,
				javaFormatterProfileLabel, javaFormatterProfile, enableGWTNativeMethodsCheckBox }, enableJavaFormatting);

		enabledBy(new JComponent[] { importOrder, pathToImportOrderPreferenceFile,
				pathToImportOrderPreferenceFileBrowse, importOrderManualExample, importOrderLabel,
				importOrderPreferenceFileExample, importOrderConfigurationFromFileRadioButton,
				importOrderConfigurationManualRadioButton }, optimizeImportsCheckBox);

		enabledBy(new JComponent[] { pathToImportOrderPreferenceFile, importOrderPreferenceFileExample,
				pathToImportOrderPreferenceFileBrowse }, importOrderConfigurationFromFileRadioButton);

		enabledBy(new JComponent[] { importOrder, importOrderManualExample, },
				importOrderConfigurationManualRadioButton);

		enabledByAny(new JComponent[] { pathToEclipsePreferenceFileJS, javaScriptFormatterProfileLabel,
				javaScriptFormatterProfile, eclipsePrefsExampleJS, eclipsePreferenceFileJSLabel,
				eclipsePreferenceFilePathJSBrowse, enableJavaScriptCommentsPostProcessor }, enableJSFormatting,
				enableGWTNativeMethodsCheckBox);

		enabledBy(new JComponent[] { disabledFileTypes, disabledFileTypesHelpLabel, },
				formatOtherFilesWithExceptionsRadioButton);

		disableJavaProfilesIfNecessary();
		disableJavaScriptProfilesIfNecessary();
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
			javaScriptFormatterProfile.setEnabled(false);
		}
	}

	public ProjectSettingsForm(final Project project) {
		DONATEButton.setBorder(BorderFactory.createEmptyBorder());
		DONATEButton.setContentAreaFilled(false);
		this.project = project;
		JToggleButton[] modifiableButtons = new JToggleButton[] { useDefaultFormatter, useEclipseFormatter,
				optimizeImportsCheckBox, enableJavaFormatting, doNotFormatOtherFilesRadioButton,
				formatOtherFilesWithExceptionsRadioButton, formatSelectedTextInAllFileTypes, enableJSFormatting,
				importOrderConfigurationManualRadioButton, importOrderConfigurationFromFileRadioButton,
				enableGWTNativeMethodsCheckBox };
		for (JToggleButton button : modifiableButtons) {
			button.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					updateComponents();
				}
			});
		}

		JTextField[] modifiableFields = new JTextField[] { pathToEclipsePreferenceFileJava,
				pathToEclipsePreferenceFileJS, disabledFileTypes, importOrder, pathToImportOrderPreferenceFile };
		for (JTextField field : modifiableFields) {
			field.getDocument().addDocumentListener(new DocumentAdapter() {
				protected void textChanged(DocumentEvent e) {
					updateComponents();
				}
			});
		}

		eclipsePreferenceFilePathJavaBrowse.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				browseForFile(pathToEclipsePreferenceFileJava);
			}
		});
		pathToImportOrderPreferenceFileBrowse.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				browseForFile(pathToImportOrderPreferenceFile);
			}
		});
		eclipsePreferenceFilePathJSBrowse.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				browseForFile(pathToEclipsePreferenceFileJS);
			}
		});

		rootComponent.addAncestorListener(new AncestorListener() {
			public void ancestorAdded(AncestorEvent event) {
				// Called when component becomes visible, to ensure that the
				// popups
				// are visible when the form is shown for the first time.
				updateComponents();
			}

			public void ancestorRemoved(AncestorEvent event) {
			}

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

		newProfile.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(final ActionEvent e) {
				if (isModified(displayedSettings)) {
					createConfirmation("Profile was modified, save changes to current profile?", "Yes", "No",
							new Runnable() {
								@Override
								public void run() {
									try {
										exportDisplayedSettings();
										createProfile();
									} catch (ConfigurationException e) {
										Messages.showErrorDialog(project, e.getMessage(), e.getTitle());
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
				refreshProfilesModel();
				profiles.setSelectedItem(settings);
			}
		});
		copyProfile.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (isModified(displayedSettings)) {
					ListPopup confirmation = createConfirmation(
							"Profile was modified, save changes to current profile?", "Yes", "No", new Runnable() {
								@Override
								public void run() {
									try {
										exportDisplayedSettings();
										copyProfile();
									} catch (ConfigurationException e) {
										Messages.showErrorDialog(project, e.getMessage(), e.getTitle());
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
				Settings settings = GlobalSettings.getInstance().copySettings(displayedSettings);
				refreshProfilesModel();
				profiles.setSelectedItem(settings);

			}
		});
		setJavaFormatterProfileModel();
		setJavaScriptFormatterProfileModel();

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
				}
			}

		});

		profiles.setRenderer(new ListCellRendererWrapper(profiles.getRenderer()) {
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
				final JTextField content = new JTextField();
				content.setText(displayedSettings.getName());
				JBPopup balloon = PopupFactoryImpl.getInstance().createComponentPopupBuilder(content, content).createPopup();
				balloon.setMinimumSize(new Dimension(200, 20));
				balloon.addListener(new JBPopupListener() {
					@Override
					public void beforeShown(LightweightWindowEvent event) {
					}

					@Override
					public void onClosed(LightweightWindowEvent event) {
						displayedSettings.setName(content.getText());
					}
				});
				balloon.showUnderneathOf(rename);
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
					Settings defaultSettings = GlobalSettings.getInstance().getDefaultSettings();
					importFromInternal(defaultSettings);
					profiles.setSelectedItem(defaultSettings);
				}

			}
		});
		DONATEButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				BareBonesBrowserLaunch.openURL("https://www.paypal.com/cgi-bin/webscr?cmd=_donations&business=75YN7U7H7D7XU&lc=CZ&item_name=Eclipse%20code%20formatter%20%2d%20IntelliJ%20plugin%20%2d%20Donation&currency_code=USD&bn=PP%2dDonationsBF%3abtn_donateCC_LG%2egif%3aNonHostedGuest");
			}
		});
		helpButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				BareBonesBrowserLaunch.openURL("http://code.google.com/p/eclipse-code-formatter-intellij-plugin/wiki/HowTo");
			}
		});
		;
		homepage.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				BareBonesBrowserLaunch.openURL("http://plugins.intellij.net/plugin/?idea&id=6546");
			}
		});
	}

	private void setJavaFormatterProfileModel() {
		String selectedProfile = displayedSettings != null ? displayedSettings.getSelectedJavaProfile() : null;
		javaFormatterProfile.setModel(createProfilesModel(pathToEclipsePreferenceFileJava, selectedProfile));
	}

	private void setJavaScriptFormatterProfileModel() {
		String selectedProfile = displayedSettings != null ? displayedSettings.getSelectedJavaProfile() : null;
		javaScriptFormatterProfile.setModel(createProfilesModel(pathToEclipsePreferenceFileJS, selectedProfile));
	}

	private ComboBoxModel createProfilesModel(JTextField pathToEclipsePreferenceFile, String selectedProfile) {
		SortedComboBoxModel<String> profilesModel = new SortedComboBoxModel<String>(new Comparator<String>() {
			@Override
			public int compare(String o1, String o2) {
				return o1.compareTo(o2);
			}
		});
		String text = pathToEclipsePreferenceFile.getText();
		if (!text.isEmpty()) {
			if (text.endsWith("xml")) {
				profilesModel.addAll(FileUtils.getProfileNamesFromConfigXML(new File(text)));
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

	private SortedComboBoxModel<Settings> createProfilesModel() {
		SortedComboBoxModel<Settings> settingsSortedComboBoxModel = new SortedComboBoxModel<Settings>(
				new Comparator<Settings>() {
					@Override
					public int compare(Settings o1, Settings o2) {
						return o1.getName().compareTo(o2.getName());
					}
				});
		settingsSortedComboBoxModel.setAll(GlobalSettings.getInstance().getSettingsList());
		return settingsSortedComboBoxModel;
	}

	@NotNull
	private Project getProject() {
		return ProjectSettingsForm.this.project;
	}

	private void showConfirmationDialogOnProfileChange() {
		if (!profileChangedValidation.get()) {
			createConfirmation("Profile was modified, save changes?", "Yes", "No", new Runnable() {
						@Override
						public void run() {
							try {
								exportDisplayedSettings();
								importFrom(getSelectedItem());
							} catch (ConfigurationException e) {
								profileChangedValidation.set(true);
								Messages.showErrorDialog(project, e.getMessage(), e.getTitle());
								profiles.setSelectedItem(getSelectedItem());
							}
						}
					}, new Runnable() {
						@Override
						public void run() {
							importFromInternal(getSelectedItem());
						}
					}, 0
			).showInCenterOf(profiles);
		} else {
			profileChangedValidation.set(false);
		}
	}

	private boolean isSameId() {
		// return !displayedSettings.getId().equals(getSelectedItem().getId());
		return displayedSettings.getId().equals(projectSettings.getId());
	}

	private void refreshProfilesModel() {
		profilesModel.setAll(GlobalSettings.getInstance().getSettingsList());
	}

	private Settings getSelectedItem() {
		Object selectedItem = profiles.getSelectedItem();
		return (Settings) selectedItem;
	}

	private void browseForFile(@NotNull final JTextField target) {
		final FileChooserDescriptor descriptor = FileChooserDescriptorFactory.createSingleFileNoJarsDescriptor();
		descriptor.setHideIgnored(false);

		descriptor.setTitle("Select config file");
		String text = target.getText();
		final VirtualFile toSelect = text == null || text.isEmpty() ? getProject().getBaseDir()
				: LocalFileSystem.getInstance().findFileByPath(text);

		// 10.5 does not have #chooseFile
		VirtualFile[] virtualFile = FileChooser.chooseFiles(getProject(), descriptor, toSelect);
		if (virtualFile != null && virtualFile.length > 0) {
			target.setText(virtualFile[0].getPath());
		}

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

	private void showPopup(@NotNull JComponent parent, @NotNull String message) {
		if (!parent.isShowing() || !parent.isEnabled()) {
			return; // if getLocationOnScreen is called when the component is
			// not showing, an exception is thrown
		}
		JToolTip tip = new JToolTip();
		tip.setTipText(message);
		Dimension tipSize = tip.getPreferredSize();

		Point location = parent.getLocationOnScreen();
		int x = (int) location.getX();
		int y = (int) (location.getY() - tipSize.getHeight());

		Popup popup = PopupFactory.getSharedInstance().getPopup(parent, tip, x, y);
		popup.show();
		visiblePopups.add(popup);
	}

	private void hidePopups() {
		for (Iterator<Popup> it = visiblePopups.iterator(); it.hasNext();) {
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
		boolean displayedSettingsIsNull = displayedSettings == null;
		if (displayedSettingsIsNull) {
			projectSettings = in;
		}
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
		importOrderConfigurationFromFileRadioButton.setSelected(in.isImportOrderFromFile());
		importOrderConfigurationManualRadioButton.setSelected(!in.isImportOrderFromFile());
		javaFormatterProfile.setSelectedItem(in.getSelectedJavaProfile());
		javaScriptFormatterProfile.setSelectedItem(in.getSelectedJavaScriptProfile());
		setData(in);
		updateComponents();
	}

	public Settings exportDisplayedSettings() throws ConfigurationException {
		validate();

		if (useEclipseFormatter.isSelected()) {
			displayedSettings.setFormatter(Settings.Formatter.ECLIPSE);
		} else {
			displayedSettings.setFormatter(Settings.Formatter.DEFAULT);
		}
		displayedSettings.setFormatOtherFileTypesWithIntelliJ(formatOtherFilesWithExceptionsRadioButton.isSelected());
		displayedSettings.setImportOrderFromFile(importOrderConfigurationFromFileRadioButton.isSelected());
		displayedSettings.setSelectedJavaProfile((String) javaFormatterProfile.getSelectedItem());
		displayedSettings.setSelectedJavaScriptProfile((String) javaScriptFormatterProfile.getSelectedItem());
		getData(displayedSettings);
		return displayedSettings;
	}

	private void validate() throws ConfigurationException {
		if (useEclipseFormatter.isSelected()) {
			if (enableJavaFormatting.isSelected()) {
				if (StringUtils.isBlank(pathToEclipsePreferenceFileJava.getText())) {
					throw new ConfigurationException("Path to Java config file is not valid");
				}
				if (!new File(pathToEclipsePreferenceFileJava.getText()).exists()) {
					throw new ConfigurationException("Path to Java config file is not valid - file does not exist");
				}
			}
			if (enableJSFormatting.isSelected()) {
				if (StringUtils.isBlank(pathToEclipsePreferenceFileJS.getText())) {
					throw new ConfigurationException("Path to JS config file is not valid");
				}
				if (!new File(pathToEclipsePreferenceFileJS.getText()).exists()) {
					throw new ConfigurationException("Path to JS config file is not valid - file does not exist");
				}
			}
		}
	}

	private boolean customIsModified(Settings data) {
		if (!ObjectUtils.equals(javaFormatterProfile.getSelectedItem(), data.getSelectedJavaProfile())) {
			return true;
		}
		if (!ObjectUtils.equals(javaScriptFormatterProfile.getSelectedItem(), data.getSelectedJavaScriptProfile())) {
			return true;
		}
		if (useDefaultFormatter.isSelected() != data.getFormatter().equals(Settings.Formatter.DEFAULT)) {
			return true;
		}
		if (useEclipseFormatter.isSelected() != data.getFormatter().equals(Settings.Formatter.ECLIPSE)) {
			return true;
		}
		if (formatOtherFilesWithExceptionsRadioButton.isSelected() != data.isFormatOtherFileTypesWithIntelliJ()) {
			return true;
		}
		if (doNotFormatOtherFilesRadioButton.isSelected() != !data.isFormatOtherFileTypesWithIntelliJ()) {
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
			public PopupStep onChosen(String selectedValue, final boolean finalChoice) {
				if (selectedValue.equals(yesText)) {
					onYes.run();
				} else {
					onNo.run();
				}
				return FINAL_CHOICE;
			}

			public void canceled() {
			}

			public boolean isMnemonicsNavigationEnabled() {
				return true;
			}
		};
		step.setDefaultOptionIndex(defaultOptionIndex);

		final ApplicationEx app = ApplicationManagerEx.getApplicationEx();
		return app == null || !app.isUnitTestMode() ? new ListPopupImpl(step) : new MockConfirmation(step, yesText);
	}

	private void log(String message) {
		System.err.println(message);
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
		enableGWTNativeMethodsCheckBox.setSelected(data.isEnableGWT());
		enableJavaScriptCommentsPostProcessor.setSelected(data.isEnableJSProcessor());
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
		data.setEnableGWT(enableGWTNativeMethodsCheckBox.isSelected());
		data.setEnableJSProcessor(enableJavaScriptCommentsPostProcessor.isSelected());
	}

	public boolean isModified(Settings data) {
		// TODO THIS IS VERY IMPORTANT
		if (customIsModified(data)) {
			return true;
		}

		if (optimizeImportsCheckBox.isSelected() != data.isOptimizeImports()) {
			return true;
		}
		if (formatSelectedTextInAllFileTypes.isSelected() != data.isFormatSeletedTextInAllFileTypes()) {
			return true;
		}
		if (pathToEclipsePreferenceFileJava.getText() != null ? !pathToEclipsePreferenceFileJava.getText().equals(
				data.getPathToConfigFileJava()) : data.getPathToConfigFileJava() != null) {
			return true;
		}
		if (pathToEclipsePreferenceFileJS.getText() != null ? !pathToEclipsePreferenceFileJS.getText().equals(
				data.getPathToConfigFileJS()) : data.getPathToConfigFileJS() != null) {
			return true;
		}
		if (disabledFileTypes.getText() != null ? !disabledFileTypes.getText().equals(data.getDisabledFileTypes())
				: data.getDisabledFileTypes() != null) {
			return true;
		}
		if (enableJSFormatting.isSelected() != data.isEnableJSFormatting()) {
			return true;
		}
		if (enableJavaFormatting.isSelected() != data.isEnableJavaFormatting()) {
			return true;
		}
		if (importOrder.getText() != null ? !importOrder.getText().equals(data.getImportOrder())
				: data.getImportOrder() != null) {
			return true;
		}
		if (pathToImportOrderPreferenceFile.getText() != null ? !pathToImportOrderPreferenceFile.getText().equals(
				data.getImportOrderConfigFilePath()) : data.getImportOrderConfigFilePath() != null) {
			return true;
		}
		if (enableGWTNativeMethodsCheckBox.isSelected() != data.isEnableGWT()) {
			return true;
		}
		if (enableJavaScriptCommentsPostProcessor.isSelected() != data.isEnableJSProcessor()) {
			return true;
		}
		return false;
	}
}
