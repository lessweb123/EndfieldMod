package endfield.util;

import arc.func.Prov;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.function.Function;

import static endfield.util.GetKt.sneakyThrow;

public class ReflectionMethodInvokeHelper implements MethodInvokeHelper {
	protected static final CollectionObjectMap<Class<?>, CollectionOrderedMap<FunctionType, Method>> methodPool = new CollectionObjectMap<>(Class.class, CollectionOrderedMap.class);
	protected static final CollectionObjectMap<Class<?>, CollectionOrderedMap<FunctionType, Constructor<?>>> constructorPool = new CollectionObjectMap<>(Class.class, CollectionOrderedMap.class);

	protected static final CollectionObjectMap<Class<?>, Method[]> methodsMap = new CollectionObjectMap<>(Class.class, Method[].class);
	protected static final CollectionObjectMap<Class<?>, Constructor<?>[]> constructorsMap = new CollectionObjectMap<>(Class.class, Constructor[].class);

	protected static final Prov<CollectionOrderedMap<FunctionType, Method>> prov2 = () -> new CollectionOrderedMap<>(FunctionType.class, Method.class);
	protected static final Prov<CollectionOrderedMap<FunctionType, Constructor<?>>> prov3 = () -> new CollectionOrderedMap<>(FunctionType.class, Constructor.class);

	protected static final Function<Class<?>, Method[]> function2 = Class::getDeclaredMethods;
	protected static final Function<Class<?>, Constructor<?>[]> function3 = Class::getDeclaredConstructors;

	protected Method getMethod(Class<?> clazz, FunctionType functionType) {
		CollectionOrderedMap<FunctionType, Method> map = methodPool.get(clazz, prov2);

		Method res = map.get(functionType);

		if (res != null) return res;

		for (var entry : map) {
			if (entry.key.match(functionType)) return entry.value;
		}

		Class<?> current = clazz;

		while (current != null) {
			try {
				res = current.getDeclaredMethod(functionType.name(), functionType.parameterTypes());
				res.setAccessible(true);
				map.put(FunctionType.from(res), res);
				return res;
			} catch (NoSuchMethodException ignored) {}

			current = current.getSuperclass();
		}

		current = clazz;

		while (current != null) {
			for (Method method : methodsMap.computeIfAbsent(current, function2)) {
				if (!method.getName().equals(functionType.name())) continue;

				FunctionType type;
				if ((type = FunctionType.from(method)).match(functionType)) {
					method.setAccessible(true);
					map.put(type, method);
					return method;
				}
				type.recycle();
			}

			current = current.getSuperclass();
		}

		throw sneakyThrow(new NoSuchMethodException("no such method " + functionType + " in class: " + clazz));
	}

	@SuppressWarnings("unchecked")
	protected <T> Constructor<T> getConstructor(Class<T> clazz, FunctionType functionType) {
		CollectionOrderedMap<FunctionType, Constructor<?>> map = constructorPool.get(clazz, prov3);

		Constructor<T> res = (Constructor<T>) map.get(functionType);
		if (res != null) return res;

		for (var entry : map) {
			if (entry.key.match(functionType)) return (Constructor<T>) entry.value;
		}

		try {
			res = clazz.getConstructor(functionType.parameterTypes());
			res.setAccessible(true);
			map.put(FunctionType.from(res), res);
			return res;
		} catch (NoSuchMethodException ignored) {}

		for (Constructor<?> constructor : constructorsMap.computeIfAbsent(clazz, function3)) {
			FunctionType type;
			if ((type = FunctionType.from(constructor)).match(functionType)) {
				map.put(type, constructor);
				res = (Constructor<T>) constructor;
				res.setAccessible(true);
				return res;
			}
			type.recycle();
		}

		throw sneakyThrow(new NoSuchMethodException("no such constructor " + functionType + " in class: " + clazz));
	}

	@Override
	public void clear() {
		methodsMap.clear();
		constructorsMap.clear();
	}

	@Override
	public void clear(Class<?> clazz) {
		methodsMap.remove(clazz);
		constructorsMap.remove(clazz);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T invoke(Object object, String name, Object... args) {
		FunctionType type = FunctionType.args(name, args);
		try {
			return (T) getMethod(object.getClass(), type).invoke(object, args);
		} catch (IllegalAccessException | InvocationTargetException e) {
			throw sneakyThrow(e);
		} finally {
			type.recycle();
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T invokeStatic(Class<?> clazz, String name, Object... args) {
		FunctionType type = FunctionType.args(name, args);
		try {
			return (T) getMethod(clazz, type).invoke(null, args);
		} catch (IllegalAccessException | InvocationTargetException e) {
			throw sneakyThrow(e);
		} finally {
			type.recycle();
		}
	}

	@Override
	public <T> T newInstance(Class<T> clazz, Object... args) {
		FunctionType type = FunctionType.args("<init>", args);
		try {
			return getConstructor(clazz, type).newInstance(args);
		} catch (IllegalAccessException | InvocationTargetException | InstantiationException e) {
			throw sneakyThrow(e);
		} finally {
			type.recycle();
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T invokeTyped(Object object, String name, Class<?>[] parameterTypes, Object... args) {
		FunctionType type = FunctionType.inst(name, parameterTypes);
		try {
			return (T) getMethod(object.getClass(), type).invoke(object, args);
		} catch (IllegalAccessException | InvocationTargetException e) {
			throw sneakyThrow(e);
		} finally {
			type.recycle();
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T invokeStaticTyped(Class<?> clazz, String name, Class<?>[] parameterTypes, Object... args) {
		FunctionType type = FunctionType.inst(name, parameterTypes);
		try {
			return (T) getMethod(clazz, type).invoke(null, args);
		} catch (IllegalAccessException | InvocationTargetException e) {
			throw sneakyThrow(e);
		} finally {
			type.recycle();
		}
	}

	@Override
	public <T> T newInstanceTyped(Class<T> clazz, Class<?>[] parameterTypes, Object... args) {
		FunctionType type = FunctionType.inst("<init>", parameterTypes);
		try {
			return getConstructor(clazz, type).newInstance(args);
		} catch (IllegalAccessException | InvocationTargetException | InstantiationException e) {
			throw sneakyThrow(e);
		} finally {
			type.recycle();
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T invoke(Method method, Object object, Object... args) {
		try {
			return (T) method.invoke(object, args);
		} catch (IllegalAccessException | InvocationTargetException e) {
			throw sneakyThrow(e);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T invokeStatic(Method method, Object... args) {
		try {
			return (T) method.invoke(null, args);
		} catch (IllegalAccessException | InvocationTargetException e) {
			throw sneakyThrow(e);
		}
	}

	@Override
	public <T> T newInstance(Constructor<T> constructor, Object... args) {
		try {
			return constructor.newInstance(args);
		} catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
			throw sneakyThrow(e);
		}
	}
}
