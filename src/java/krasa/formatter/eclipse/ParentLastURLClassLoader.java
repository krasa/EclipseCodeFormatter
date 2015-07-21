package krasa.formatter.eclipse;

import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashSet;
import java.util.Set;

/**
 * A parent-last classloader that will try the child classloader first and then the parent. This takes a fair bit of
 * doing because java really prefers parent-first.
 * <p/>
 * For those not familiar with class loading trickery, be wary
 */
public class ParentLastURLClassLoader extends ClassLoader {

	final static Set<String> loadFromParent = new HashSet<String>();

	{
		{
			loadFromParent.add("org.eclipse.wst.jsdt.core.formatter.CodeFormatter");
		}
	}

	private ChildURLClassLoader childClassLoader;

	/**
	 * This class allows me to call findClass on a classloader
	 */
	private static class FindClassClassLoader extends ClassLoader {
		public FindClassClassLoader(ClassLoader parent) {
			super(parent);
		}

		@Override
		public Class<?> findClass(String name) throws ClassNotFoundException {
			return super.findClass(name);
		}
	}

	/**
	 * This class delegates (child then parent) for the findClass method for a URLClassLoader. We need this because
	 * findClass is protected in URLClassLoader
	 */
	private static class ChildURLClassLoader extends URLClassLoader {
		private FindClassClassLoader realParent;

		public ChildURLClassLoader(URL[] urls, FindClassClassLoader realParent) {
			super(urls, null);

			this.realParent = realParent;
		}

		@Override
		public Class<?> findClass(String name) throws ClassNotFoundException {
			try {
				if (loadFromParent.contains(name)) {
					return realParent.loadClass(name);
				}

				//calling twic #findClass with the same classname, you will get a LinkageError, this fixes it
				Class<?> loaded = super.findLoadedClass(name);
				if (loaded != null)
					return loaded;

				// first try to use the URLClassLoader findClass
				return super.findClass(name);
			} catch (ClassNotFoundException e) {
				// if that fails, we ask our real parent classloader to load the class (we give up)
				return realParent.loadClass(name);
			}
		}
	}

	public ParentLastURLClassLoader(ClassLoader parent, URL... urls) {
		super(parent);
		childClassLoader = new ChildURLClassLoader(urls,
				new ParentLastURLClassLoader.FindClassClassLoader(this.getParent()));
	}

	@Override
	protected synchronized Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
		try {
			// first we try to find a class inside the child classloader
			return childClassLoader.findClass(name);
		} catch (ClassNotFoundException e) {
			// didn't find it, try the parent
			return super.loadClass(name, resolve);
		}
	}
}
