package krasa.formatter.utils;

import java.util.ArrayList;
import java.util.List;

import krasa.formatter.settings.Settings;

import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.Assert;
import org.junit.Test;
import org.picocontainer.PicoContainer;

import com.intellij.openapi.components.BaseComponent;
import com.intellij.openapi.components.ComponentConfig;
import com.intellij.openapi.extensions.ExtensionPointName;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Condition;
import com.intellij.openapi.util.UserDataHolderBase;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.ArrayUtil;
import com.intellij.util.messages.MessageBus;

/**
 * @author Vojtech Krasa
 */
public class StringUtilsTest {
	@Test
	public void testBetterMatching() throws Exception {
		String order1 = "com.foo";
		String order2 = "com.kuk";
		String s = StringUtils.betterMatching(order2, order1, "com.foo.goo");
		Assert.assertEquals(order1, s);
		s = StringUtils.betterMatching(order1, order2, "com.foo.goo");
		Assert.assertEquals(order1, s);
	}

	@Test
	public void testTrimToList() throws Exception {
		List<String> strings = StringUtils.trimToList("");
		Assert.assertTrue(strings.isEmpty());
		strings = StringUtils.trimToList(" ");
		Assert.assertTrue(strings.isEmpty());
	}

	@Test
	public void testGenerateName() throws Exception {
		ArrayList<Settings> settingsList = new ArrayList<Settings>();
		Project instance = new MyDummyProject();
		String s = StringUtils.generateName(settingsList, 1, instance.getName());
		Assert.assertEquals("dummy", s);
		settingsList.add(new Settings(1L, "dummy"));

		s = StringUtils.generateName(settingsList, 1, instance.getName());
		Assert.assertEquals("dummy (1)", s);
		settingsList.add(new Settings(1L, "dummy (1)"));

		s = StringUtils.generateName(settingsList, 1, instance.getName());
		Assert.assertEquals("dummy (2)", s);
	}

	class MyDummyProject extends UserDataHolderBase implements Project {

		public MyDummyProject() {
		}

		public VirtualFile getProjectFile() {
			return null;
		}

		@NotNull
		public String getName() {
			return "dummy";
		}

		@Nullable
		@NonNls
		public String getPresentableUrl() {
			return null;
		}

		@NotNull
		@NonNls
		public String getLocationHash() {
			return "dummy";
		}

		@Nullable
		@NonNls
		public String getLocation() {
			throw new UnsupportedOperationException("Method getLocation not implemented in " + getClass());
		}

		@NotNull
		public String getProjectFilePath() {
			return "";
		}

		public VirtualFile getWorkspaceFile() {
			return null;
		}

		@Nullable
		public VirtualFile getBaseDir() {
			return null;
		}

		// @Override
		public String getBasePath() {
			return null;
		}

		public void save() {
		}

		public BaseComponent getComponent(String name) {
			return null;
		}

		public <T> T getComponent(Class<T> interfaceClass) {
			return null;
		}

		public boolean hasComponent(@NotNull Class interfaceClass) {
			return false;
		}

		@NotNull
		public <T> T[] getComponents(Class<T> baseClass) {
			return (T[]) ArrayUtil.EMPTY_OBJECT_ARRAY;
		}

		@NotNull
		public PicoContainer getPicoContainer() {
			throw new UnsupportedOperationException("getPicoContainer is not implement in : " + getClass());
		}

		public <T> T getComponent(Class<T> interfaceClass, T defaultImplementation) {
			return null;
		}

		@NotNull
		// @Override
		public Class[] getComponentInterfaces() {
			return new Class[0];
		}

		public boolean isDisposed() {
			return false;
		}

		@NotNull
		public Condition getDisposed() {
			return new Condition() {
				public boolean value(final Object o) {
					return isDisposed();
				}
			};
		}

		@NotNull
		public ComponentConfig[] getComponentConfigurations() {
			return new ComponentConfig[0];
		}

		@Nullable
		public Object getComponent(final ComponentConfig componentConfig) {
			return null;
		}

		public boolean isOpen() {
			return false;
		}

		public boolean isInitialized() {
			return false;
		}

		public boolean isDefault() {
			return false;
		}

		public MessageBus getMessageBus() {
			return null;
		}

		public void dispose() {
		}

		public <T> T[] getExtensions(final ExtensionPointName<T> extensionPointName) {
			throw new UnsupportedOperationException("getExtensions()");
		}

		public ComponentConfig getConfig(Class componentImplementation) {
			throw new UnsupportedOperationException("Method getConfig not implemented in " + getClass());
		}
	}

}
