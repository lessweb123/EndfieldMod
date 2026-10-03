package endfield.desktop;

import java.lang.invoke.MethodHandles.Lookup;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * Not very practical and there is a better solution, temporarily only for experimental purposes.
 *
 * @since 1.0.10
 */
public final class DesktopNativeHelper {
	private DesktopNativeHelper() {}

	/**
	 * @return {@code Lookup} object with trust permission.
	 */
	public static native Lookup getLookup();

	public static native void setAccessible(AccessibleObject object, boolean flag);

	/**
	 * Search for fields in the class based on their name and JVM signature.
	 * <pre>{@code
	 * //java.lang.invoke.MethodHandles.Lookup.IMPL_LOOKUP
	 * getField(Lookup.class, "IMPL_LOOKUP", "Ljava/lang/invoke/MethodHandles$Lookup;", true);
	 * }</pre>
	 * The returned field does not need to call {@code setAccessible(true)} to make it accessible.
	 * <p>If the field cannot be found, a {@code NoSuchFieldException} will be thrown.
	 *
	 * @param name name of field
	 * @param signature signature of field
	 */
	public static native Field getField(Class<?> clazz, String name, String signature, boolean isStatic);

	/**
	 * Search for methods in the class based on their name and JVM signature.
	 * <p>Not supporting obtaining the {@code <init>} method.
	 * <pre>{@code
	 * //java.lang.Module.addExports0
	 * getMethod(Module.class, "addExports0", "(Ljava/lang/Module;Ljava/lang/String;Ljava/lang/Module;)V", true);
	 * }</pre>
	 * The returned method does not need to call {@code setAccessible(true)} to make it accessible.
	 * <p>If no method is found, a {@code NoSuchMethodException} will be thrown.
	 *
	 * @param name name of method
	 * @param signature signature of method
	 */
	public static native Method getMethod(Class<?> clazz, String name, String signature, boolean isStatic);

	/**
	 * Search for constructor in the class based on JVM signature.
	 * <pre>{@code
	 * //java.lang.Object()
	 * getConstructor(Object.class, "()V");
	 * }</pre>
	 * The returned constructor does not need to call {@code setAccessible(true)} to make it accessible.
	 * <p>If the constructor cannot be found, a {@code NoSuchMethodException} will be thrown.
	 *
	 * @param signature signature of constructor
	 */
	public static native <T> Constructor<T> getConstructor(Class<T> clazz, String signature);
}
