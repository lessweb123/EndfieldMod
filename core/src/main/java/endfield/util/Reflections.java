package endfield.util;

import arc.func.Boolf;
import arc.func.Prov;
import arc.struct.ObjectSet;
import arc.struct.Seq;
import arc.util.Nullable;
import arc.util.Structs;
import endfield.util.handler.ClassHandler;
import mindustry.Vars;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InaccessibleObjectException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;

import static endfield.Vars2.accessibleHelper;
import static endfield.Vars2.platformImpl;

/**
 * Reflection utilities, mainly for wrapping reflective operations to eradicate checked exceptions.
 *
 * @author LessWeb
 * @since 1.0.6
 */
public final class Reflections {
	public static final int BRIDGE = 0x00000040;
	public static final int VARARGS = 0x00000080;
	public static final int SYNTHETIC = 0x00001000;
	public static final int ANNOTATION = 0x00002000;
	public static final int ENUM = 0x00004000;
	public static final int MANDATED = 0x00008000;

	/** Don't let anyone instantiate this class. */
	private Reflections() {}

	public static FieldAccessor getFieldAccessor(Class<?> type, String name) {
		return getFieldAccessor(ClassHandler.getField(type, name));
	}

	public static FieldAccessor getFieldAccessor(Class<?> type, Boolf<Field> filler) {
		return getFieldAccessor(ClassHandler.getField(type, filler));
	}

	/**
	 * Wrap the field in the Accessor and handle all checked exceptions.
	 * <p>Allow modification of {@code final} fields, provided there are no issues arising from special platforms.
	 *
	 * @param field Field to be packaged
	 * @return New field accessor
	 * @throws NullPointerException If the field is null
	 */
	public static FieldAccessor getFieldAccessor(Field field) {
		return platformImpl.fieldAccessor(field);
	}

	public static MethodAccessor getMethodAccessor(Class<?> type, String name, Class<?>... parameterTypes) {
		return platformImpl.methodAccessor(ClassHandler.getMethod(type, name, parameterTypes));
	}

	/**
	 * Wrap the method in the Accessor and handle all checked exceptions.
	 *
	 * @param method Method to be packaged
	 * @return New method accessor
	 * @throws NullPointerException If the method is null
	 */
	public static MethodAccessor getMethodAccessor(Method method) {
		return platformImpl.methodAccessor(method);
	}

	/**
	 * Wrap the constructor in the Accessor and handle all checked exceptions.
	 *
	 * @param constructor Constructor to be packaged
	 * @return New constructor accessor
	 * @throws NullPointerException If the constructor is null
	 */
	public static <T> ConstructorAccessor<T> getConstructorAccessor(Constructor<T> constructor) {
		return platformImpl.constructorAccessor(constructor);
	}

	public static MethodInvoker getMethodInvoker(Method method) {
		return platformImpl.methodInvoker(method);
	}

	public static MethodInvoker getMethodInvoker(Constructor<?> constructor) {
		return platformImpl.methodInvoker(constructor);
	}

	public static MethodInvoker getMethodInvoker(Class<?> type, String name, Class<?>... parameterTypes) {
		return platformImpl.methodInvoker(ClassHandler.getMethod(type, name, parameterTypes));
	}

	public static MethodInvoker getMethodInvoker(Class<?> type, Class<?>... parameterTypes) {
		return platformImpl.methodInvoker(ClassHandler.getConstructor(type, parameterTypes));
	}

	/**
	 * @throws RuntimeException Any exception that occurs in reflection
	 */
	public static <T> Prov<T> supply(Class<T> type, String name, Class<?>[] parameterTypes, T object, Object... args) {
		Method method = ClassHandler.getMethod(type, name, parameterTypes);
		MethodAccessor accessor = getMethodAccessor(method);

		if (!match(parameterTypes, args))
			throw new IllegalArgumentException(Arrays.toString(toTypes(args)) + " cannot be assigned to " + Arrays.toString(parameterTypes));

		return () -> accessor.invoke(object, args);
	}

	/**
	 * Reflectively instantiates a type without throwing exceptions.
	 *
	 * @throws RuntimeException Any exception that occurs in reflection
	 */
	public static <T> Prov<T> supply(Class<T> type, Class<?>[] parameterTypes, Object... args) {
		Constructor<T> constructor = ClassHandler.getConstructor(type, parameterTypes);
		ConstructorAccessor<T> accessor = getConstructorAccessor(constructor);

		if (!match(parameterTypes, args))
			throw new IllegalArgumentException(Arrays.toString(toTypes(args)) + " cannot be assigned to " + Arrays.toString(parameterTypes));

		return () -> accessor.newInstance(args);
	}

	/**
	 * Finds class with the specified name using Mindustry's mod class loader.
	 *
	 * @param name The class' binary name, as per {@link Class#getName()}.
	 * @return The class, or {@code null} if not found.
	 */
	@SuppressWarnings("unchecked")
	public static <T> Class<T> findClass(String name) {
		try {
			return name == null ? null : (Class<T>) Class.forName(name, false, Vars.mods.mainLoader());
		} catch (ClassNotFoundException e) {
			return null;
		}
	}

	public static String methodToString(Class<?> type, String name, Class<?>... parameterTypes) {
		StringBuilder buf = new StringBuilder();
		buf.append(type.getName()).append('.').append(name);

		if (parameterTypes == null || parameterTypes.length == 0) return buf.append("()").toString();

		int max = parameterTypes.length;

		buf.append('(');
		int i = 0;
		while (true) {
			buf.append(parameterTypes[i++].getName());
			if (i == max) return buf.append(')').toString();
			buf.append(',');
		}
		// The approach of Java. But I don't like using Stream here.
		/*return type.getName() + '.' + name +
				((parameterTypes == null || parameterTypes.length == 0) ?
						"()" :
						Arrays.stream(parameterTypes)
						.map(c -> c == null ? "null" : c.getName())
						.collect(Collectors.joining(",", "(", ")")));*/
	}

	public static Seq<Class<?>> getDirectSuperclasses(Class<?> clazz) {
		Seq<Class<?>> result = new Seq<>(Class.class);
		Class<?> superclass = clazz.getSuperclass();

		if (superclass != null && superclass != Object.class) {
			result.add(superclass);
		}

		result.addAll(clazz.getInterfaces());

		return result;
	}

	public static boolean isInstanceButNotSubclass(Object obj, Class<?> type) {
		if (type.isInstance(obj)) {
			Class<?> cur = obj.getClass().getSuperclass();
			while (cur != null) {
				Class<?>[] interfaces = cur.getInterfaces();
				if (Structs.contains(interfaces, type)) return false;

				cur = cur.getSuperclass();
			}

			return true;
		}

		return false;
	}

	public static ObjectSet<Class<?>> getClassSubclassHierarchy(Class<?> clazz) {
		Class<?> current = clazz.getSuperclass();
		ObjectSet<Class<?>> hierarchy = new ObjectSet<>();
		while (current != null) {
			hierarchy.add(current);
			Class<?>[] interfaces = current.getInterfaces();
			hierarchy.addAll(interfaces);

			current = current.getSuperclass();
		}
		return hierarchy;
	}

	public static boolean isAssignable(Field sourceType, Field targetType) {
		return sourceType != null && targetType != null && targetType.getType().isAssignableFrom(sourceType.getType());
	}

	/**
	 * Compare two parameter types and check if they can be assigned to another parameter, without
	 * considering the primitive type and its corresponding wrapper class.
	 *
	 * @param sourceTypes Source parameter type
	 * @param targetTypes Parameter type to be assigned
	 * @throws NullPointerException If the parameter array is {@code null} and the elements in the array are
	 *                              {@code null}, This is normal and should not happen
	 * @since 1.0.9
	 */
	public static boolean isAssignable(Class<?>[] sourceTypes, Class<?>[] targetTypes) {
		if (sourceTypes.length != targetTypes.length) return false;

		for (int i = 0; i < sourceTypes.length; i++) {
			if (sourceTypes[i] != targetTypes[i] && !targetTypes[i].isAssignableFrom(sourceTypes[i])) return false;
		}

		return true;
	}

	/**
	 * This type of data has frequent calls, small data volume, and unnecessary performance costs when
	 * using stream processing. Using for traversal instead of stream processing.
	 */
	public static Class<?>[] wrapper(Class<?>[] clazz) {
		for (int i = 0; i < clazz.length; i++) {
			clazz[i] = wrapper(clazz[i]);
		}
		return clazz;
	}

	public static Class<?>[] unwrapped(Class<?>[] clazz) {
		for (int i = 0; i < clazz.length; i++) {
			clazz[i] = unwrapped(clazz[i]);
		}
		return clazz;
	}

	public static Class<?> wrapper(Class<?> clazz) {
		if (clazz == int.class) return Integer.class;
		if (clazz == float.class) return Float.class;
		if (clazz == long.class) return Long.class;
		if (clazz == double.class) return Double.class;
		if (clazz == byte.class) return Byte.class;
		if (clazz == short.class) return Short.class;
		if (clazz == boolean.class) return Boolean.class;
		if (clazz == char.class) return Character.class;
		if (clazz == void.class) return Void.class;
		return clazz;
	}

	public static Class<?> unwrapped(Class<?> clazz) {
		if (clazz == Integer.class) return int.class;
		if (clazz == Float.class) return float.class;
		if (clazz == Long.class) return long.class;
		if (clazz == Double.class) return double.class;
		if (clazz == Byte.class) return byte.class;
		if (clazz == Short.class) return short.class;
		if (clazz == Boolean.class) return boolean.class;
		if (clazz == Character.class) return char.class;
		if (clazz == Void.class) return void.class;
		return clazz;
	}

	public static Class<?>[] toTypes(Object... args) {
		if (args == null || args.length == 0) return Constant.EMPTY_CLASS;

		Class<?>[] types = new Class[args.length];

		for (int i = 0; i < types.length; i++) {
			Object object = args[i];
			types[i] = object == null ? void.class : object.getClass();
		}

		return types;
	}

	public static boolean match(Class<?>[] sourceTypes, Object... args) {
		if (sourceTypes.length != args.length) return false;

		for (int i = 0; i < sourceTypes.length; ++i) {
			Class<?> sourceType = sourceTypes[i];
			Object arg = args[i];

			if (sourceType.isPrimitive()) {
				if (sourceType == int.class && !(arg instanceof Integer)) return false;
				else if (sourceType == byte.class && !(arg instanceof Byte)) return false;
				else if (sourceType == char.class && !(arg instanceof Character)) return false;
				else if (sourceType == boolean.class && !(arg instanceof Boolean)) return false;
				else if (sourceType == double.class && !(arg instanceof Double)) return false;
				else if (sourceType == float.class && !(arg instanceof Float)) return false;
				else if (sourceType == long.class && !(arg instanceof Long)) return false;
				else if (sourceType == short.class && !(arg instanceof Short)) return false;
			} else if (arg != null && !sourceType.isInstance(arg)) return false;
		}
		return true;
	}

	/**
	 * This method is compatible with primitive types and their corresponding wrapper classes, but the
	 * performance overhead may be slightly higher.
	 *
	 * @param sourceTypes Source parameter type
	 * @param targetTypes Parameter type to be assigned
	 * @throws NullPointerException If the array or any of its elements is null
	 * @since 1.0.9
	 */
	public static boolean match(Class<?>[] sourceTypes, Class<?>[] targetTypes) {
		if (targetTypes.length != sourceTypes.length) return false;

		for (int i = 0; i < sourceTypes.length; i++) {
			Class<?> sourceType = sourceTypes[i];
			Class<?> targetType = targetTypes[i];

			if (targetType == void.class) continue;
			if (!sourceType.isAssignableFrom(targetType)) return false;
		}

		return true;
	}

	/**
	 * Set the accessible flag of the reflection object to {@code true}.
	 *
	 * @return Has it been successfully set as accessible
	 * @since 1.0.9
	 */
	@SuppressWarnings("deprecation")
	public static <T extends AccessibleObject & Member> boolean setAccessible(T object) {
		if (object.isAccessible()) return true;

		try {
			accessibleHelper.makeAccessible(object);
		} catch (InaccessibleObjectException e) {
			return false;
		}

		return true;
	}

	// android only
	public static <T> Class<T> setClassAccessible(Class<T> clazz) {
		int modifiers = clazz.getModifiers();
		if ((modifiers & Modifier.PUBLIC) == 0 || (modifiers & Modifier.FINAL) != 0) {
			accessibleHelper.makeClassAccessible(clazz);
		}

		return clazz;
	}

	// --------------------- kotlin only -------------------------

	public static <T> T newInstance(Constructor<T> constructor, Object[] args) throws InvocationTargetException, InstantiationException, IllegalAccessException {
		return constructor.newInstance(args);
	}

	public static Object invoke(Method method, Object object, Object[] args) throws InvocationTargetException, IllegalAccessException {
		return method.invoke(object, args);
	}

	// -------------------- reflection utils only ------------------------

	public static String getGetMessage(Field field, String type) {
		return "Attempt to get " + field.getType().getName() + " field \"" +
				field.getDeclaringClass().getName() + "." + field.getName() + "\" with illegal data type conversion to " + type;
	}

	public static String getSetMessage(Field field, @Nullable Object object) {
		return getSetMessage(field, object == null ? "" : object.getClass().getName(), "");
	}

	public static String getSetMessage(Field field, String attemptedType, String attemptedValue) {
		int modifiers = field.getModifiers();

		StringBuilder err = new StringBuilder().append("Can not set");
		if ((modifiers & Modifier.STATIC) != 0)
			err.append(" static");
		if ((modifiers & Modifier.FINAL) != 0)
			err.append(" final");
		err.append(" ").append(field.getType().getName()).append(" field ").append(field.getDeclaringClass().getName()).append(".").append(field.getName()).append(" to ");
		if (!attemptedValue.isEmpty()) {
			err.append("(").append(attemptedType).append(")").append(attemptedValue);
		} else {
			if (!attemptedType.isEmpty())
				err.append(attemptedType);
			else
				err.append("null value");
		}
		return err.toString();
	}

	// ------------------ jni only ---------------------

	public static void thrFieldException(Class<?> type, String name, String signature) throws NoSuchFieldException {
		throw new NoSuchFieldException(buildMessage(type, name, signature, false));
	}

	public static void thrMethodException(Class<?> type, String name, String signature) throws NoSuchMethodException {
		throw new NoSuchMethodException(buildMessage(type, name, signature, true));
	}

	public static void thrConstructorException(Class<?> type, String signature) throws NoSuchMethodException {
		throw new NoSuchMethodException(buildMessage(type, "<init>", signature, true));
	}

	static String buildMessage(Class<?> type, String name, String signature, boolean isMethod) {
		String className = type.getName();

		StringBuilder sb = new StringBuilder(className.length() + name.length() + 32);
		sb.append(className).append('.').append(name);

		if (signature != null && !signature.isEmpty()) {
			if (!isMethod && signature.charAt(0) != '(') {
				sb.append(' ');
			}
			sb.append(signature);
		}
		return sb.toString();
	}

	public static void checkAllocType(Class<?> type) {
		if (type == null) throw new NullPointerException("type is null");

		if (type.isPrimitive()) throw new IllegalArgumentException("Cannot instantiate primitive type");
		if (type.isArray()) throw new IllegalArgumentException("Cannot instantiate array type (use Array.newInstance instead)");

		if (type == Class.class) throw new IllegalArgumentException("Cannot instantiate class type");
		if (type == Field.class) throw new IllegalArgumentException("Cannot instantiate field type");
		if (type == Method.class) throw new IllegalArgumentException("Cannot instantiate method type");
		if (type == Constructor.class) throw new IllegalArgumentException("Cannot instantiate constructor type");

		int modifiers = type.getModifiers();

		if ((modifiers & (Modifier.ABSTRACT | Modifier.INTERFACE)) != 0) throw new IllegalArgumentException("Cannot instantiate abstract class or interface");
	}
}
