package endfield.util;

public class MockClassHelper implements ClassHelper {
	@Override
	public <T> T allocateInstance(Class<? extends T> clazz) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Class<?> defineClass(String name, byte[] bytes, ClassLoader loader) {
		throw new UnsupportedOperationException();
	}
}
