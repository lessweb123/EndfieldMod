package endfield.android;

import dalvik.system.PathClassLoader;

public final class CoreOjClassLoader extends PathClassLoader {
	private final boolean useHelperStubs;

	static String getBootClassPath() {
		String bootClassPath = System.getProperty("java.boot.class.path", "");
		assert bootClassPath != null;
		return bootClassPath;
	}

	private static String getCoreOjPath() {
		return getBootClassPath().split(":", 2)[0];
	}

	CoreOjClassLoader() {
		this(getCoreOjPath(), null, true);
	}

	CoreOjClassLoader(String path) {
		this(path, null, true);
	}

	CoreOjClassLoader(String path, ClassLoader parent) {
		this(path, parent, false);
	}

	private CoreOjClassLoader(String path, ClassLoader parent, boolean useHelperStubs) {
		super(path, parent);
		this.useHelperStubs = useHelperStubs;
	}

	@Override
	public Class<?> loadClass(String name) throws ClassNotFoundException {
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
