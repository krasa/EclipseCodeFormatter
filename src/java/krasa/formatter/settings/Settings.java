/*
 * External Code Formatter Copyright (c) 2007-2009 Esko Luontola, www.orfjackal.net Licensed under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for
 * the specific language governing permissions and limitations under the License.
 */

package krasa.formatter.settings;

import com.intellij.util.xmlb.annotations.Transient;
import krasa.formatter.eclipse.ConfigFileLocator;
import krasa.formatter.settings.provider.ImportOrderProvider;
import krasa.formatter.settings.provider.JavaPropertiesProvider;
import org.jetbrains.annotations.NotNull;

/**
 * @author Esko Luontola
 * @author Vojtech Krasa
 * @since 4.12.2007
 */
public class Settings {
	public static final String LINE_SEPARATOR = "\n";

	private String name = null;
	private Long id = null;

	private String pathToConfigFileJS = "";
	private String pathToConfigFileCpp = "";
	private boolean enableJavaFormatting = true;
	private boolean enableJSFormatting = false;
	private boolean enableCppFormatting = false;

	@NotNull
	private Formatter formatter = Formatter.DEFAULT;
	@NotNull
	private String pathToConfigFileJava = "";
	private boolean optimizeImports = true;
	private boolean importOrderFromFile = false;

	private String disabledFileTypes = "";
	private boolean formatOtherFileTypesWithIntelliJ = true;
	private boolean formatSeletedTextInAllFileTypes = true;

	private Integer notifyFromTextLenght = 300;
	private String importOrder = "java;javax;org;com;";
	private String importOrderConfigFilePath = "";
	private String selectedJavaProfile = null;
	private boolean enableGWT = false;

	@Transient
	protected transient JavaPropertiesProvider javaPropertiesProvider;
	@Transient
	protected transient ImportOrderProvider importOrderProvider;
	@Transient
	private transient DisabledFileTypeSettings disabledFileTypeSettings;

	private boolean enableJSProcessor;
	private String selectedJavaScriptProfile;
	private String selectedCppProfile;
	private boolean useForLiveTemplates = false;
	@Deprecated
	private boolean useOldEclipseJavaFormatter = false;
	private FormatterVersion eclipseVersion = FormatterVersion.NEWEST;
	private ImportOrdering importOrdering = ImportOrdering.ECLIPSE_452;
	private ConfigType configType = ConfigType.CUSTOM;
	@Deprecated
	private String pathToEclipse = "";

	public Settings() {
	}

	public ConfigType getConfigType() {
		return configType;
	}

	public void setConfigType(ConfigType configType) {
		this.configType = configType;
	}

	@Deprecated
	public String getPathToEclipse() {
		return pathToEclipse;
	}

	public FormatterVersion getEclipseVersion() {
		return eclipseVersion;
	}

	public void setEclipseVersion(FormatterVersion eclipseVersion) {
		this.eclipseVersion = eclipseVersion;
		switch (eclipseVersion) {

		case ECLIPSE_44:
			useOldEclipseJavaFormatter = true;
			break;
		case NEWEST:
			useOldEclipseJavaFormatter = false;
			break;
		case CUSTOM:
			useOldEclipseJavaFormatter = false;
			break;
		}
	}

	public ImportOrdering getImportOrdering() {
		return importOrdering;
	}

	public void setImportOrdering(ImportOrdering importOrdering) {
		this.importOrdering = importOrdering;
		if (this.importOrdering == ImportOrdering.ECLIPSE_451) {
			this.importOrdering = ImportOrdering.ECLIPSE_44;
		}
	}

	@Deprecated
	public void setPathToEclipse(String pathToEclipse) {
		this.pathToEclipse = pathToEclipse;
	}

	public Settings(Long id, String name) {
		this.id = id;
		this.name = name;
	}

	public DisabledFileTypeSettings getDisabledFileTypeSettings() {
		if (disabledFileTypeSettings == null) {
			disabledFileTypeSettings = new DisabledFileTypeSettings(disabledFileTypes);
		}
		return disabledFileTypeSettings;
	}

	public String getSelectedJavaProfile() {
		return selectedJavaProfile;
	}

	public void setSelectedJavaProfile(String selectedJavaProfile) {
		javaPropertiesProvider = null;
		this.selectedJavaProfile = selectedJavaProfile;
	}

	public String getPathToConfigFileJS() {
		return pathToConfigFileJS;
	}

	public void setPathToConfigFileJS(final String pathToConfigFileJS) {
		this.pathToConfigFileJS = pathToConfigFileJS;
	}

	public String getPathToConfigFileCpp() {
		return pathToConfigFileCpp;
	}

	public void setPathToConfigFileCpp(String pathToConfigFileCpp) {
		this.pathToConfigFileCpp = pathToConfigFileCpp;
	}

	public boolean isEnableJavaFormatting() {
		return enableJavaFormatting;
	}

	public void setEnableJavaFormatting(final boolean enableJavaFormatting) {
		this.enableJavaFormatting = enableJavaFormatting;
	}

	public String getSelectedCppProfile() {
		return selectedCppProfile;
	}

	public void setSelectedCppProfile(String selectedCppProfile) {
		this.selectedCppProfile = selectedCppProfile;
	}

	public boolean isEnableJSFormatting() {
		return enableJSFormatting;
	}

	public boolean isEnableCppFormatting() {
		return enableCppFormatting;
	}

	public void setEnableCppFormatting(boolean enableCppFormatting) {
		this.enableCppFormatting = enableCppFormatting;
	}

	public void setEnableJSFormatting(final boolean enableJSFormatting) {
		this.enableJSFormatting = enableJSFormatting;
	}

	public String getImportOrder() {
		return importOrder;
	}

	public void setImportOrder(final String importOrder) {
		this.importOrder = importOrder;
	}

	public String getImportOrderConfigFilePath() {
		return importOrderConfigFilePath;
	}

	public void setImportOrderConfigFilePath(final String importOrderConfigFilePath) {
		importOrderProvider = null;
		this.importOrderConfigFilePath = importOrderConfigFilePath;
	}

	public boolean isEnableGWT() {
		return enableGWT;
	}

	public void setEnableGWT(final boolean enableGWT) {
		this.enableGWT = enableGWT;
	}

	public JavaPropertiesProvider getJavaProperties() {
		if (javaPropertiesProvider == null) {
			String pathToConfigFileJava = new ConfigFileLocator().resolveConfigFilePath(this.getPathToConfigFileJava());
			javaPropertiesProvider = new JavaPropertiesProvider(pathToConfigFileJava, getSelectedJavaProfile());
		}
		return javaPropertiesProvider;
	}

	public ImportOrderProvider getImportOrderProvider() {
		if (importOrderProvider == null) {
			importOrderProvider = new ImportOrderProvider(this);
		}
		return importOrderProvider;
	}

	public boolean isEnableJSProcessor() {
		return enableJSProcessor;
	}

	public void setEnableJSProcessor(final boolean enableJSProcessor) {
		this.enableJSProcessor = enableJSProcessor;
	}

	public String getSelectedJavaScriptProfile() {
		return selectedJavaScriptProfile;
	}

	public void setSelectedJavaScriptProfile(String selectedJavaScriptProfile) {
		this.selectedJavaScriptProfile = selectedJavaScriptProfile;
	}

	public boolean isEnabled() {
		return getFormatter() == Formatter.ECLIPSE;
	}

	boolean isNotSaved() {
		return getId() == null && getName() == null;
	}

	public boolean isUseForLiveTemplates() {
		return useForLiveTemplates;
	}

	public void setUseForLiveTemplates(final boolean useForLiveTemplates) {
		this.useForLiveTemplates = useForLiveTemplates;
	}

	@Deprecated
	public boolean isUseOldEclipseJavaFormatter() {
		return useOldEclipseJavaFormatter;
	}

	@Deprecated
	public void setUseOldEclipseJavaFormatter(final boolean useOldEclipseJavaFormatter) {
		this.useOldEclipseJavaFormatter = useOldEclipseJavaFormatter;
		if (useOldEclipseJavaFormatter) {
			eclipseVersion = FormatterVersion.ECLIPSE_44;
		}
	}

	public boolean isProjectSpecific() {
		return this instanceof ProjectSpecificProfile;
	}

	public static enum FormatterVersion {
		ECLIPSE_44, NEWEST, CUSTOM
	}

	public static enum ImportOrdering {
		/**
		 * not matching imports -> between groups
		 */
		ECLIPSE_44, @Deprecated
		ECLIPSE_451,
		/**
		 * not matching imports -> on the end, actually since Eclipse 4.5.1 :( oops
		 */
		ECLIPSE_452,
	}

	public static enum ConfigType {
		RESOLVE, ECLIPSE, ECLIPSE_2_1, JAVA_CONVENTIONS, CUSTOM,
	}

	public static enum Formatter {
		DEFAULT, ECLIPSE
	}

	public static enum Location {
		PROJECT, APPLICATION
	}

	public boolean isFormatSeletedTextInAllFileTypes() {
		return formatSeletedTextInAllFileTypes;
	}

	public void setFormatSeletedTextInAllFileTypes(boolean formatSeletedTextInAllFileTypes) {
		this.formatSeletedTextInAllFileTypes = formatSeletedTextInAllFileTypes;
	}

	public boolean isFormatOtherFileTypesWithIntelliJ() {
		return formatOtherFileTypesWithIntelliJ;
	}

	public void setFormatOtherFileTypesWithIntelliJ(boolean formatOtherFileTypesWithIntelliJ) {
		this.formatOtherFileTypesWithIntelliJ = formatOtherFileTypesWithIntelliJ;
	}

	public Integer getNotifyFromTextLenght() {
		return notifyFromTextLenght;
	}

	public void setNotifyFromTextLenght(Integer notifyFromTextLenght) {
		this.notifyFromTextLenght = notifyFromTextLenght;
	}

	public boolean isOptimizeImports() {
		return optimizeImports;
	}

	public String getDisabledFileTypes() {
		return disabledFileTypes;
	}

	public void setDisabledFileTypes(String disabledFileTypes) {
		disabledFileTypeSettings = null;
		this.disabledFileTypes = disabledFileTypes;
	}

	public void setOptimizeImports(boolean optimizeImports) {
		this.optimizeImports = optimizeImports;
	}

	@NotNull
	public Formatter getFormatter() {
		return formatter;
	}

	public void setFormatter(@NotNull Formatter formatter) {
		this.formatter = formatter;
	}

	@NotNull
	public String getPathToConfigFileJava() {
		return pathToConfigFileJava;
	}

	public void setPathToConfigFileJava(@NotNull String pathToConfigFileJava) {
		javaPropertiesProvider = null;
		this.pathToConfigFileJava = pathToConfigFileJava;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public boolean isImportOrderFromFile() {
		return importOrderFromFile;
	}

	public void setImportOrderFromFile(boolean importOrderFromFile) {
		this.importOrderFromFile = importOrderFromFile;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;

		Settings settings = (Settings) o;

		if (id != null ? !id.equals(settings.id) : settings.id != null)
			return false;

		return true;
	}

	@Override
	public int hashCode() {
		return id != null ? id.hashCode() : 0;
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder("Settings{");
		sb.append("id=").append(id);
		sb.append(", name='").append(name).append('\'');
		sb.append('}');
		return sb.toString();
	}
}
