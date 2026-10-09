package endfield.util;

import arc.func.Prov;
import endfield.util.holder.ObjectHolder;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.function.Function;

import static endfield.util.GetKt.sneakyThrow;

public class ReflectionMethodInvokeHelper implements MethodInvokeHelper {
	protected static final CollectionObjectMap<Class<?>, CollectionObjectMap<String, CollectionObjectMap<FunctionType, Method>>> methodPool = new CollectionObjectMap<>(Class.class, CollectionObjectMap.class);
	protected static final CollectionObjectMap<Class<?>, CollectionObjectMap<FunctionType, Constructor<?>>> constructorPool = new CollectionObjectMap<>(Class.class, CollectionObjectMap.class);

	protected static final CollectionObjectMap<Class<?>, Method[]> methodsMap = new CollectionObjectMap<>(Class.class, Method[].class);
	protected static final CollectionObjectMap<Class<?>, Constructor<?>[]> constructorsMap = new CollectionObjectMap<>(Class.class, Constructor[].class);

	protected static final Prov<CollectionObjectMap<String, CollectionObjectMap<FunctionType, Method>>> prov2 = () -> new CollectionObjectMap<>(String.class, CollectionObjectMap.class);
	protected static final Prov<CollectionObjectMap<FunctionType, Method>> prov3 = () -> new CollectionObjectMap<>(FunctionType.class, Method.class);
	protected static final Prov<CollectionObjectMap<FunctionType, Constructor<?>>> prov4 = () -> new CollectionObjectMap<>(FunctionType.class, Constructor.class);

	protected static final Function<Class<?>, Method[]> function2 = Class::getDeclaredMethods;
	protected static final Function<Class<?>, Constructor<?>[]> function3 = Class::getDeclaredConstructors;

	protected Method getMethod(Class<?> clazz, String name, FunctionType types) {
		CollectionObjectMap<FunctionType, Method> map = methodPool.get(clazz, prov2).get(name, prov3);

		FunctionType type = FunctionType.inst(types);
		Method res = map.get(type);

		if (res != null) return res;

		for (ObjectHolder<FunctionType, Method> entry : map) {
			if (entry.key.match(types)) return entry.value;
		}

		Class<?> current = clazz;

		while (current != null) {
			try {
				res = current.getDeclaredMethod(name, types.paramType());
				res.setAccessible(true);
				map.put(FunctionType.from(res), res);
				return res;
			} catch (Throwable ignored) {}

			current = current.getSuperclass();
		}

		current = clazz;

		while (current != null) {
			for (Method method : methodsMap.computeIfAbsent(current, function2)) {
				if (!method.getName().equals(name)) continue;

				FunctionType t;
				if ((t = FunctionType.from(method)).match(types)) {
					method.setAccessible(true);
					map.put(t, method);
					return method;
				}
				t.recycle();
			}

			current = current.getSuperclass();
		}

		throw sneakyThrow(new NoSuchMethodException("no such method " + name + " in class: " + clazz + " with assignable parameter: " + types));
	}

	@SuppressWarnings("unchecked")
	protected <T> Constructor<T> getConstructor(Class<T> clazz, FunctionType types) {
		CollectionObjectMap<FunctionType, Constructor<?>> map = constructorPool.get(clazz, prov4);

		Constructor<T> res = (Constructor<T>) map.get(types);
		if (res != null) return res;

		for (ObjectHolder<FunctionType, Constructor<?>> entry : map) {
			if (entry.key.match(types)) return (Constructor<T>) entry.value;
		}

		try {
			res = clazz.getConstructor(types.paramType());
			res.setAccessible(true);
			map.put(FunctionType.from(res), res);
			return res;
		} catch (NoSuchMethodException ignored) {}

		for (Constructor<?> constructor : constructorsMap.computeIfAbsent(clazz, function3)) {
			FunctionType functionType;
			if ((functionType = FunctionType.from(constructor)).match(types)) {
				map.put(functionType, constructor);
				res = (Constructor<T>) constructor;
				res.setAccessible(true);

				break;
			}
			functionType.recycle();
		}

		if (res != null) return res;

		throw sneakyThrow(new NoSuchMethodException("no such constructor in class: " + clazz + " with assignable parameter: " + types));
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
		FunctionType type = FunctionType.inst(args);
		try {
			return (T) getMethod(object.getClass(), name, type).invoke(object, args);
		} catch (IllegalAccessException | InvocationTargetException e) {
			throw sneakyThrow(e);
		} finally {
			type.recycle();
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T invokeStatic(Class<?> clazz, String name, Object... args) {
		FunctionType type = FunctionType.inst(args);
		try {
			return (T) getMethod(clazz, name, type).invoke(null, args);
		} catch (IllegalAccessException | InvocationTargetException e) {
			throw sneakyThrow(e);
		} finally {
			type.recycle();
		}
	}

	@Override
	public <T> T newInstance(Class<T> clazz, Object... args) {
		FunctionType funcType = FunctionType.inst(args);
		try {
			return getConstructor(clazz, funcType).newInstance(args);
		} catch (IllegalAccessException | InvocationTargetException | InstantiationException e) {
			throw sneakyThrow(e);
		} finally {
			funcType.recycle();
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T invokeTyped(Object object, String name, Class<?>[] parameterTypes, Object... args) {
		FunctionType type = FunctionType.inst(parameterTypes);
		try {
			return (T) getMethod(object.getClass(), name, type).invoke(object, args);
		} catch (IllegalAccessException | InvocationTargetException e) {
			throw sneakyThrow(e);
		} finally {
			type.recycle();
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T invokeStaticTyped(Class<?> clazz, String name, Class<?>[] parameterTypes, Object... args) {
		FunctionType type = FunctionType.inst(parameterTypes);
		try {
			return (T) getMethod(clazz, name, type).invoke(null, args);
		} catch (IllegalAccessException | InvocationTargetException e) {
			throw sneakyThrow(e);
		} finally {
			type.recycle();
		}
	}

	@Override
	public <T> T newInstanceTyped(Class<T> clazz, Class<?>[] parameterTypes, Object... args) {
		FunctionType funcType = FunctionType.inst(parameterTypes);
		try {
			return getConstructor(clazz, funcType).newInstance(args);
		} catch (IllegalAccessException | InvocationTargetException | InstantiationException e) {
			throw sneakyThrow(e);
		} finally {
			funcType.recycle();
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
