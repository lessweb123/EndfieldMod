package endfield.util;

import endfield.Vars2;

import java.lang.invoke.MethodHandles.Lookup;

public class MockPlatformImpl implements PlatformImpl {
	public MockPlatformImpl() {
		init();
	}

	protected void init() {
		Vars2.fieldAccessHelper = new ReflectionFieldAccessHelper();
		Vars2.methodInvokeHelper = new ReflectionMethodInvokeHelper();
		Vars2.classHelper = new MockClassHelper();
		Vars2.accessibleHelper = new MockAccessibleHelper();
	}

	@Override
	public Lookup lookup(Class<?> clazz) {
		return Reflects.publicLookup;
	}

	@Override
	public <T> T clone(T object) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void put(long srcAddress, long destAddress, long bytes) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void put(Object src, int srcOffset, Object dst, int dstOffset, long bytes) {
		throw new UnsupportedOperationException();
	}

	@Override
	public int arrayBaseOffset(Class<?> arrayClass) {
		throw new UnsupportedOperationException();
	}

	@Override
	public int arrayIndexScale(Class<?> arrayClass) {
		throw new UnsupportedOperationException();
	}
}
