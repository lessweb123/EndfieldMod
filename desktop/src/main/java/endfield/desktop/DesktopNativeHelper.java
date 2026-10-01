package endfield.desktop;

import java.lang.invoke.MethodHandles.Lookup;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

final class DesktopNativeHelper {
	private DesktopNativeHelper() {}

	public static void init() {}

	// MethodHandles.Lookup.IMPL_LOOKUP
	static native Lookup getLookup();

	static native Field getField(Class<?> clazz, String name, String signature, boolean isStatic);

	static native Method getMethod(Class<?> clazz, String name, String signature, boolean isStatic);
}
