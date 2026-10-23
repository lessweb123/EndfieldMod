package endfield.desktop;

import arc.func.Prov;
import arc.util.Nullable;
import endfield.util.CollectionObjectMap;
import endfield.util.CollectionOrderedMap;
import endfield.util.FunctionType;
import endfield.util.MethodInvokeHelper;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodType;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;

import static endfield.desktop.DesktopClassHelper.ctypes;
import static endfield.desktop.DesktopClassHelper.function5;
import static endfield.desktop.DesktopClassHelper.function6;
import static endfield.desktop.DesktopClassHelper.mtypes;
import static endfield.desktop.DesktopClassHelper.ptypes;
import static endfield.desktop.DesktopImpl.lookup;
import static endfield.util.GetKt.sneakyThrow;

public class MethodHandleMethodInvokeHelper implements MethodInvokeHelper {
	protected static final CollectionObjectMap<Class<?>, CollectionOrderedMap<FunctionType, MethodHandle>> methodPool = new CollectionObjectMap<>(Class.class, CollectionOrderedMap.class);
	protected static final CollectionObjectMap<Class<?>, Method[]> methodsMap = new CollectionObjectMap<>(Class.class, Method[].class);
	protected static final CollectionObjectMap<Class<?>, Constructor<?>[]> constructorsMap = new CollectionObjectMap<>(Class.class, Constructor[].class);

	protected static final Prov<CollectionOrderedMap<FunctionType, MethodHandle>> prov6 = () -> new CollectionOrderedMap<>(FunctionType.class, MethodHandle.class);

	protected MethodHandle getMethod(Class<?> clazz, FunctionType functionType) throws IllegalAccessException {
		CollectionOrderedMap<FunctionType, MethodHandle> map = methodPool.get(clazz, prov6);

		MethodHandle res = map.get(functionType);

		if (res != null) return res;

		for (var entry : map) {
			if (entry.key.match(functionType)) return entry.value;
		}

		Class<?> current = clazz;

		while (current != null) {
			Method method = findMethod(current, functionType.name(), functionType.paramType());

			if (method != null) {
				res = asSpreader(method);
				map.put(from(method), res);
				return res;
			}

			current = current.getSuperclass();
		}

		current = clazz;

		while (current != null) {
			for (Method method : methodsMap.computeIfAbsent(current, function5)) {
				if (!method.getName().equals(functionType.name())) continue;

				FunctionType type;
				if ((type = from(method)).match(functionType)) {
					res = asSpreader(method);
					map.put(type, res);
					return res;
				}
				type.recycle();
			}

			current = current.getSuperclass();
		}

		throw sneakyThrow(new NoSuchMethodException("no such method " + functionType + " in class: " + clazz));
	}

	protected MethodHandle getConstructor(Class<?> clazz, FunctionType functionType) throws IllegalAccessException {
		CollectionOrderedMap<FunctionType, MethodHandle> map = methodPool.get(clazz, prov6);

		MethodHandle res = map.get(functionType);
		if (res != null) return res;

		for (var entry : map) {
			if (entry.key.match(functionType)) return entry.value;
		}

		Constructor<?> cons = findConstructor(clazz, functionType.paramType());
		if (cons != null) {
			res = asSpreader(cons);
			map.put(from(cons), res);
			return res;
		}

		for (Constructor<?> constructor : constructorsMap.computeIfAbsent(clazz, function6)) {
			FunctionType type;
			if ((type = from(constructor)).match(functionType)) {
				res = asSpreader(constructor);
				map.put(type, res);
				return res;
			}
			type.recycle();
		}

		throw sneakyThrow(new NoSuchMethodException("no such constructor " + functionType + " in class: " + clazz));
	}

	protected @Nullable Method findMethod(Class<?> type, String name, Class<?>[] paramType) {
		Method[] methods = methodsMap.computeIfAbsent(type, function5);
		for (Method method : methods) {
			if (method.getName().equals(name) && Arrays.equals((Class<?>[]) mtypes.get(method), paramType)) return method;
		}
		return null;
	}

	protected @Nullable Constructor<?> findConstructor(Class<?> type, Class<?>[] paramType) {
		Constructor<?>[] constructors = constructorsMap.computeIfAbsent(type, function6);
		for (Constructor<?> constructor : constructors) {
			if (Arrays.equals((Class<?>[]) ctypes.get(constructor), paramType)) return constructor;
		}
		return null;
	}

	protected final MethodHandle asSpreader(Method method) throws IllegalAccessException {
		MethodHandle target = lookup.unreflect(method).asFixedArity();

		int paramCount = target.type().parameterCount();

		if ((method.getModifiers() & Modifier.STATIC) != 0) {
			return target.asSpreader(Object[].class, paramCount)
					.asType(MethodType.methodType(Object.class, Object[].class));
		} else {
			if (paramCount < 1)
				throw new IllegalArgumentException("Instance method must have e receiver");
			MethodHandle spread = target.asSpreader(Object[].class, paramCount -1);
			MethodType newType = spread.type()
					.changeParameterType(0, Object.class)
					.changeReturnType(Object.class);
			return spread.asType(newType);
		}
	}

	protected final MethodHandle asSpreader(Constructor<?> constructor) throws IllegalAccessException {
		MethodHandle target = lookup.unreflectConstructor(constructor).asFixedArity();

		int paramCount = target.type().parameterCount();
		MethodHandle spread = target.asSpreader(Object[].class, paramCount);
		return spread.asType(MethodType.methodType(Object.class, Object[].class));
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
		FunctionType type = FunctionType.inst(name, args);
		try {
			return (T) getMethod(object.getClass(), type).invokeExact(object, args);
		} catch (Throwable e) {
			throw sneakyThrow(e);
		} finally {
			type.recycle();
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T invokeStatic(Class<?> clazz, String name, Object... args) {
		FunctionType type = FunctionType.inst(name, args);
		try {
			return (T) getMethod(clazz, type).invokeExact(args);
		} catch (Throwable e) {
			throw sneakyThrow(e);
		} finally {
			type.recycle();
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T newInstance(Class<T> clazz, Object... args) {
		FunctionType type = FunctionType.inst("<init>", args);
		try {
			return (T) getConstructor(clazz, type).invokeExact(args);
		} catch (Throwable e) {
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
			return (T) getMethod(object.getClass(), type).invokeExact(object, args);
		} catch (Throwable e) {
			throw sneakyThrow(e);
		} finally {
			type.recycle();
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T invokeStaticTyped(Class<?> clazz, String name, Class<?>[] parameterTypes, Object... args) {
		FunctionType type = FunctionType.inst("<init>", parameterTypes);
		try {
			return (T) getMethod(clazz, type).invokeExact(args);
		} catch (Throwable e) {
			throw sneakyThrow(e);
		} finally {
			type.recycle();
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T newInstanceTyped(Class<T> clazz, Class<?>[] parameterTypes, Object... args) {
		FunctionType type = FunctionType.inst("<init>", parameterTypes);
		try {
			return (T) getConstructor(clazz, type).invokeExact(args);
		} catch (Throwable e) {
			throw sneakyThrow(e);
		} finally {
			type.recycle();
		}
	}

	protected MethodHandle getMethod(Method method, FunctionType functionType) throws IllegalAccessException {
		CollectionOrderedMap<FunctionType, MethodHandle> map = methodPool.get(method.getDeclaringClass(), prov6);

		MethodHandle res = map.get(functionType);

		if (res != null) return res;

		for (var entry : map) {
			if (entry.key.match(functionType)) return entry.value;
		}

		res = asSpreader(method);

		map.put(from(method), res);

		return res;
	}

	protected MethodHandle getConstructor(Constructor<?> constructor, FunctionType functionType) throws IllegalAccessException {
		CollectionOrderedMap<FunctionType, MethodHandle> map = methodPool.get(constructor.getDeclaringClass(), prov6);

		MethodHandle res = map.get(functionType);
		if (res != null) return res;

		for (var entry : map) {
			if (entry.key.match(functionType)) return entry.value;
		}

		res = asSpreader(constructor);

		map.put(from(constructor), res);

		return res;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T invoke(Method method, Object object, Object... args) {
		FunctionType type = from(method);
		try {
			return (T) getMethod(method, type).invokeExact(object, args);
		} catch (Throwable e) {
			throw sneakyThrow(e);
		} finally {
			type.recycle();
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T invokeStatic(Method method, Object... args) {
		FunctionType type = from(method);
		try {
			return (T) getMethod(method, type).invokeExact(args);
		} catch (Throwable e) {
			throw sneakyThrow(e);
		} finally {
			type.recycle();
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T newInstance(Constructor<T> constructor, Object... args) {
		FunctionType type = from(constructor);
		try {
			return (T) getConstructor(constructor, type).invokeExact(args);
		} catch (Throwable e) {
			throw sneakyThrow(e);
		} finally {
			type.recycle();
		}
	}

	public static FunctionType inst(String name, MethodType methodType) {
		return FunctionType.inst(name, (Class<?>[]) ptypes.get(methodType));
	}

	public static FunctionType from(Method method) {
		return FunctionType.inst(method.getName(), (Class<?>[]) mtypes.get(method));
	}

	public static FunctionType from(Constructor<?> constructor) {
		return FunctionType.inst("<init>", (Class<?>[]) ctypes.get(constructor));
	}
}
