package endfield.android;

import dalvik.system.PathClassLoader;

public final class BootClassLoader extends PathClassLoader {
	private final boolean useHelperStubs;

	static String getBootClassPath() {
		String bootClassPath = System.getProperty("java.boot.class.path", "");
		assert bootClassPath != null;
		return bootClassPath;
	}

	private static String getCoreOjPath() {
		return getBootClassPath().split(":", 2)[0];
	}

	BootClassLoader() {
		this(getCoreOjPath(), null, true);
	}

	BootClassLoader(String path) {
		this(path, null, true);
	}

	BootClassLoader(String path, ClassLoader parent) {
		this(path, parent, false);
	}

	private BootClassLoader(String path, ClassLoader parent, boolean useHelperStubs) {
		super(path, parent);
		this.useHelperStubs = useHelperStubs;
	}

	@Override
	public Class<?> loadClass(String name) throws ClassNotFoundException {
		if ("java.lang.Object".equals(name)) return Object.class;

		try {
			return findClass(name);
		} catch (ClassNotFoundException ignored) {
			return super.loadClass(name);
		}
	}

	@Override
	public Package[] getPackages() {
		return super.getPackages();
	}
}
