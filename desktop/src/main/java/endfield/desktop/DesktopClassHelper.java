package endfield.desktop;

import arc.func.Boolf;
import endfield.util.ClassHelper;
import endfield.util.Reflects;
import org.jetbrains.annotations.Nullable;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodType;
import java.lang.invoke.VarHandle;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Objects;
import java.util.function.Function;

import static endfield.desktop.DesktopImpl.lookup;
import static endfield.desktop.Unsafer.unsafe;
import static endfield.util.GetKt.sneakyThrow;

public class DesktopClassHelper implements ClassHelper {
	static final MethodHandle getFields, getMethods, getConstructors;
	static final VarHandle mtypes, ctypes, ptypes;

	static final Function<Class<?>, Field[]> function4;
	static final Function<Class<?>, Method[]> function5;
	static final Function<Class<?>, Constructor<?>[]> function6;

	/*static final CollectionObjectMap<Class<?>, Field[]> fieldsMap;
	static final CollectionObjectMap<Class<?>, Method[]> methodsMap;
	static final CollectionObjectMap<Class<?>, Constructor<?>[]> constructorsMap;*/

	static {
		try {
			getFields = lookup.findVirtual(Class.class, "getDeclaredFields0", MethodType.methodType(Field[].class, boolean.class));
			getMethods = lookup.findVirtual(Class.class, "getDeclaredMethods0", MethodType.methodType(Method[].class, boolean.class));
			getConstructors = lookup.findVirtual(Class.class, "getDeclaredConstructors0", MethodType.methodType(Constructor[].class, boolean.class));

			mtypes = lookup.findVarHandle(Method.class, "parameterTypes", Class[].class);
			ctypes = lookup.findVarHandle(Constructor.class, "parameterTypes", Class[].class);
			ptypes = lookup.findVarHandle(MethodType.class, "ptypes", Class[].class);
		} catch (NoSuchMethodException | IllegalAccessException | NoSuchFieldException e) {
			throw sneakyThrow(e);
		}

		function4 = clazz -> {
			try {
				return (Field[]) getFields.invokeExact(clazz, false);
			} catch (Throwable e) {
				throw sneakyThrow(e);
			}
		};
		function5 = clazz -> {
			try {
				return (Method[]) getMethods.invokeExact(clazz, false);
			} catch (Throwable e) {
				throw sneakyThrow(e);
			}
		};
		function6 = clazz -> {
			try {
				return (Constructor<?>[]) getConstructors.invokeExact(clazz, false);
			} catch (Throwable e) {
				throw sneakyThrow(e);
			}
		};
	}

	@Override
	public @Nullable Field findField(Class<?> clazz, String name) {
		Field[] fields = getFields(clazz);
		for (Field field : fields) {
			if (field.getName().equals(name)) return field;
		}
		return null;
	}

	@Override
	public @Nullable Method findMethod(Class<?> clazz, String name, Class<?>... parameterTypes) {
		Method[] methods = getMethods(clazz);
		for (Method method : methods) {
			if (method.getName().equals(name) && Arrays.equals((Class<?>[]) mtypes.get(method), parameterTypes)) return method;
		}
		return null;
	}

	@Override
	public <T> @Nullable Constructor<T> findConstructor(Class<T> clazz, Class<?>... parameterTypes) {
		Constructor<T>[] constructors = getConstructors(clazz);
		for (Constructor<T> constructor : constructors) {
			if (Arrays.equals((Class<?>[]) ctypes.get(constructor), parameterTypes)) return constructor;
		}
		return null;
	}

	@Override
	public Field getField(Class<?> clazz, String name) {
		Field[] fields = getFields(clazz);
		for (Field field : fields) {
			if (field.getName().equals(name)) return field;
		}

		throw sneakyThrow(new NoSuchFieldException(name));
	}

	@Override
	public Method getMethod(Class<?> clazz, String name, Class<?>... parameterTypes) {
		Method[] methods = getMethods(clazz);
		for (Method method : methods) {
			if (method.getName().equals(name) && Arrays.equals((Class<?>[]) mtypes.get(method), parameterTypes)) return method;
		}

		throw sneakyThrow(new NoSuchMethodException(Reflects.methodToString(clazz, name, parameterTypes)));
	}

	@Override
	public <T> Constructor<T> getConstructor(Class<T> clazz, Class<?>... parameterTypes) {
		Constructor<T>[] constructors = getConstructors(clazz);
		for (Constructor<T> constructor : constructors) {
			if (Arrays.equals((Class<?>[]) ctypes.get(constructor), parameterTypes)) return constructor;
		}

		throw sneakyThrow(new NoSuchMethodException(Reflects.methodToString(clazz, "<init>", parameterTypes)));
	}

	@Override
	public Field[] getFields(Class<?> clazz) {
		try {
			return (Field[]) getFields.invokeExact(clazz, false);
		} catch (Throwable e) {
			throw sneakyThrow(e);
		}
	}

	@Override
	public Method[] getMethods(Class<?> clazz) {
		try {
			return (Method[]) getMethods.invokeExact(clazz, false);
		} catch (Throwable e) {
			throw sneakyThrow(e);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> Constructor<T>[] getConstructors(Class<T> clazz) {
		try {
			return (Constructor<T>[]) getConstructors.invokeExact(clazz, false);
		} catch (Throwable e) {
			throw sneakyThrow(e);
		}
	}

	@Override
	public @Nullable Field findField(Class<?> clazz, Boolf<Field> filler) {
		Field[] fields = getFields(clazz);
		for (Field field : fields) {
			if (filler.get(field)) {
				return field;
			}
		}
		return null;
	}

	@Override
	public @Nullable Method findMethod(Class<?> clazz, Boolf<Method> filler) {
		Method[] methods = getMethods(clazz);
		for (Method method : methods) {
			if (filler.get(method)) return method;
		}
		return null;
	}

	@Override
	public <T> @Nullable Constructor<T> findConstructor(Class<T> clazz, Boolf<Constructor<T>> filler) {
		Constructor<T>[] constructors = getConstructors(clazz);
		for (Constructor<T> constructor : constructors) {
			if (filler.get(constructor)) return constructor;
		}
		return null;
	}

	@Override
	public Field getField(Class<?> clazz, Boolf<Field> filler) {
		Field[] fields = getFields(clazz);
		for (Field field : fields) {
			if (filler.get(field)) {
				return field;
			}
		}

		throw sneakyThrow(new NoSuchFieldException("Field not found"));
	}

	@Override
	public Method getMethod(Class<?> clazz, Boolf<Method> filler) {
		Method[] methods = getMethods(clazz);
		for (Method method : methods) {
			if (filler.get(method)) return method;
		}

		throw sneakyThrow(new NoSuchMethodException("Method not found"));
	}

	@Override
	public <T> Constructor<T> getConstructor(Class<T> clazz, Boolf<Constructor<T>> filler) {
		Constructor<T>[] constructors = getConstructors(clazz);
		for (Constructor<T> constructor : constructors) {
			if (filler.get(constructor)) return constructor;
		}

		throw sneakyThrow(new NoSuchMethodException("Constructor not found"));
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T allocateInstance(Class<? extends T> clazz) {
		Objects.requireNonNull(clazz);

		try {
			return (T) unsafe.allocateInstance(clazz);
		} catch (InstantiationException e) {
			throw sneakyThrow(e);
		}
	}

	@Override
	public Class<?> defineClass(String name, byte[] bytes, ClassLoader loader) throws ClassFormatError {
		return unsafe.defineClass(name, bytes, 0, bytes.length, loader, null);
	}
}
