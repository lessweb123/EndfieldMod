package endfield.util.handler;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

import static endfield.Vars2.methodInvokeHelper;

/**
 * A utility set for method invocation, including invocation, instantiation, etc.
 *
 * @since 1.0.9
 */
public final class MethodHandler {
	private MethodHandler() {}

	/**
	 * Call a method on an object with its given name and parameter type, which is not affected by access
	 * modifiers. The null in the parameter will be treated as a universal bit, that is, the null bit can be
	 * matched with any type.
	 *
	 * @param object Call the target object executed by the method
	 * @param name   method name
	 * @param args   List of parameters passed to method
	 * @return Return value of the target method
	 */
	public static <T> T invoke(Object object, String name, Object... args) {
		return methodInvokeHelper.invoke(object, name, args);
	}

	/**
	 * Call a static method with a specified name and parameter type in the type specified by the
	 * processor, which is not affected by access modifiers. The null in the parameter will be treated as a
	 * generic bit, meaning that any type can be matched with a null bit.
	 *
	 * @param name method name
	 * @param args List of parameters passed to method
	 * @return Return value of the target method
	 */
	public static <T> T invokeStatic(Class<?> clazz, String name, Object... args) {
		return methodInvokeHelper.invokeStatic(clazz, name, args);
	}

	/**
	 * Instantiate the class specified by this processor to obtain an instance of that type. The null in the
	 * passed parameters will be treated as a universal bit, meaning that any type can be matched with a
	 * null bit.
	 *
	 * @param args List of parameters passed to constructor
	 */
	public static <T> T newInstance(Class<T> clazz, Object... args) {
		return methodInvokeHelper.newInstance(clazz, args);
	}

	public static <T> T invokeTyped(Object object, String name, Class<?>[] parameterTypes, Object... args) {
		return methodInvokeHelper.invokeTyped(object, name, parameterTypes, args);
	}

	public static <T> T invokeStaticTyped(Class<?> clazz, String name, Class<?>[] parameterTypes, Object... args) {
		return methodInvokeHelper.invokeStaticTyped(clazz, name, parameterTypes, args);
	}

	public static <T> T newInstanceTyped(Class<T> clazz, Class<?>[] parameterTypes, Object... args) {
		return methodInvokeHelper.newInstanceTyped(clazz, parameterTypes, args);
	}

	/**
	 * Directly call the method without throwing an exception.
	 */
	public static <T> T invoke(Object object, Method method, Object... args) {
		return methodInvokeHelper.invoke(method, object, args);
	}

	/**
	 * Directly call the method without throwing an exception.
	 */
	public static <T> T invokeStatic(Method method, Object... args) {
		return methodInvokeHelper.invokeStatic(method, args);
	}

	/**
	 * Directly call the constructor without throwing an exception.
	 */
	public static <T> T newInstance(Constructor<T> constructor, Object... args) {
		return methodInvokeHelper.newInstance(constructor, args);
	}
}
