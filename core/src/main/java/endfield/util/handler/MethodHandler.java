package endfield.util.handler;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

import static endfield.Vars2.methodInvokeHelper;

/**
 * A utility set for method invocation, including invocation, instantiation, etc.
 *
 * @since 1.0.9
 */
public class MethodHandler<T> {
	public final Class<T> clazz;

	public MethodHandler(Class<T> c) {
		clazz = c;
	}

	/**
	 * Create a method handler using default guidelines and cache it for method invocation operations.
	 *
	 * @see #invoke(Object, String, Object...)
	 */
	public static <T, R> R invokeDefault(T object, String name, Object... args) {
		return methodInvokeHelper.invoke(object, name, args);
	}

	/**
	 * Create a method handler using default guidelines and cache it for static method invocation
	 * operations.
	 *
	 * @see #invokeStatic(String, Object...)
	 */
	public static <T, R> R invokeDefault(Class<T> clazz, String name, Object... args) {
		return methodInvokeHelper.invokeStatic(clazz, name, args);
	}

	/**
	 * Create a method handler using default guidelines and cache it for constructor calls.
	 *
	 * @see #newInstance(Object...)
	 */
	public static <T> T newInstanceDefault(Class<T> clazz, Object... args) {
		return methodInvokeHelper.newInstance(clazz, args);
	}

	public static <T, R> R invokeTypedDefault(T object, String name, Class<?>[] parameterTypes, Object... args) {
		return methodInvokeHelper.invokeTyped(object, name, parameterTypes, args);
	}

	public static <T, R> R invokeTypedDefault(Class<T> clazz, String name, Class<?>[] parameterTypes, Object... args) {
		return methodInvokeHelper.invokeStaticTyped(clazz, name, parameterTypes, args);
	}

	public static <T> T newInstanceTypedDefault(Class<T> clazz, Class<?>[] parameterTypes, Object... args) {
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
	public <R> R invoke(T object, String name, Object... args) {
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
	public <R> R invokeStatic(String name, Object... args) {
		return methodInvokeHelper.invokeStatic(clazz, name, args);
	}

	/**
	 * Instantiate the class specified by this processor to obtain an instance of that type. The null in the
	 * passed parameters will be treated as a universal bit, meaning that any type can be matched with a
	 * null bit.
	 *
	 * @param args List of parameters passed to constructor
	 */
	public T newInstance(Object... args) {
		return methodInvokeHelper.newInstance(clazz, args);
	}

	public <R> R invokeTyped(String name, Class<?>[] parameterTypes, Object... args) {
		return methodInvokeHelper.invokeStaticTyped(clazz, name, parameterTypes, args);
	}

	public T newInstanceTyped(Class<?>[] parameterTypes, Object... args) {
		return methodInvokeHelper.newInstanceTyped(clazz, parameterTypes, args);
	}
}
