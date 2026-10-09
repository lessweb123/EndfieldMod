package endfield.util;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

public interface MethodInvokeHelper {
	<T> T invoke(Object object, String name, Object... args);

	<T> T invokeStatic(Class<?> clazz, String name, Object... args);

	<T> T newInstance(Class<T> clazz, Object... args);

	<T> T invokeTyped(Object object, String name, Class<?>[] parameterTypes, Object... args);

	<T> T invokeStaticTyped(Class<?> clazz, String name, Class<?>[] parameterTypes, Object... args);

	<T> T newInstanceTyped(Class<T> clazz, Class<?>[] parameterTypes, Object... args);

	<T> T invoke(Method method, Object object, Object... args);

	<T> T invokeStatic(Method method, Object... args);

	<T> T newInstance(Constructor<T> constructor, Object... args);

	/** Clear the method cache of all classes. */
	default void clear() {}

	/** Clear the method cache of the specified class. */
	default void clear(Class<?> clazz) {}
}
