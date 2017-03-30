package krasa.formatter.utils;

import com.intellij.openapi.components.BaseComponent;
import com.intellij.openapi.components.ComponentConfig;
import com.intellij.openapi.extensions.ExtensionPointName;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Condition;
import com.intellij.openapi.util.UserDataHolderBase;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.ArrayUtil;
import com.intellij.util.messages.MessageBus;
import krasa.formatter.settings.Settings;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.Assert;
import org.junit.Test;
import org.picocontainer.PicoContainer;

import java.util.ArrayList;
import java.util.List;

import static krasa.formatter.utils.StringUtils.getPackage;
import static krasa.formatter.utils.StringUtils.getSimpleName;

/**
 * @author Vojtech Krasa
 */
public class StringUtilsTest {
	@Test
	public void test_getSimpleName() throws Exception {
		Assert.assertEquals("b", getSimpleName("a.a.b"));
		Assert.assertEquals("b", getSimpleName("b"));
	}

	@Test
	public void test_getPackage() throws Exception {
		Assert.assertEquals("a.a", getPackage("a.a.b"));
		Assert.assertEquals("", getPackage("b"));
		Assert.assertEquals("com.model", getPackage("com.model.Ethernet"));
	}

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
		Assert.assertFalse(strings.isEmpty());
		strings = StringUtils.trimToList(" ");
		Assert.assertFalse(strings.isEmpty());
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

		@Override
		public VirtualFile getProjectFile() {
			return null;
		}

		@Override
		@NotNull
		public String getName() {
			return "dummy";
		}

		@Override
		@Nullable
		@NonNls
		public String getPresentableUrl() {
			return null;
		}

		@Override
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

		@Override
		@NotNull
		public String getProjectFilePath() {
			return "";
		}

		@Override
		public VirtualFile getWorkspaceFile() {
			return null;
		}

		@Override
		@Nullable
		public VirtualFile getBaseDir() {
			return null;
		}

		// @Override
		@Override
		public String getBasePath() {
			return null;
		}

		@Override
		public void save() {
		}

		@Override
		public BaseComponent getComponent(String name) {
			return null;
		}

		@Override
		public <T> T getComponent(Class<T> interfaceClass) {
			return null;
		}

		@Override
		public boolean hasComponent(@NotNull Class interfaceClass) {
			return false;
		}

		@Override
		@NotNull
		public <T> T[] getComponents(Class<T> baseClass) {
			return (T[]) ArrayUtil.EMPTY_OBJECT_ARRAY;
		}

		@Override
		@NotNull
		public PicoContainer getPicoContainer() {
			throw new UnsupportedOperationException("getPicoContainer is not implement in : " + getClass());
		}

		@Override
		public <T> T getComponent(Class<T> interfaceClass, T defaultImplementation) {
			return null;
		}

		@NotNull
		// @Override
		public Class[] getComponentInterfaces() {
			return new Class[0];
		}

		@Override
		public boolean isDisposed() {
			return false;
		}

		@Override
		@NotNull
		public Condition getDisposed() {
			return new Condition() {
				@Override
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

		@Override
		public boolean isOpen() {
			return false;
		}

		@Override
		public boolean isInitialized() {
			return false;
		}

		@Override
		public boolean isDefault() {
			return false;
		}

		@Override
		public MessageBus getMessageBus() {
			return null;
		}

		@Override
		public void dispose() {
		}

		@Override
		public <T> T[] getExtensions(final ExtensionPointName<T> extensionPointName) {
			throw new UnsupportedOperationException("getExtensions()");
		}

		public ComponentConfig getConfig(Class componentImplementation) {
			throw new UnsupportedOperationException("Method getConfig not implemented in " + getClass());
		}
	}

}
