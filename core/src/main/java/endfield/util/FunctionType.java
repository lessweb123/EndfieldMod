package endfield.util;

import arc.util.pooling.Pool;
import arc.util.pooling.Pool.Poolable;
import arc.util.pooling.Pools;

import java.lang.invoke.MethodType;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Arrays;

/**
 * Function type encapsulates objects, recording the parameter types of functions for comparison and
 * search.
 *
 * @author EBwilson
 * @author LessWeb
 */
public class FunctionType implements Poolable {
	static final Pool<FunctionType> recyclePool = Pools.get(FunctionType.class, FunctionType::new);

	String name;
	Class<?>[] ptypes;
	int hash;

	FunctionType() {
		name = "";
		ptypes = Constant.EMPTY_CLASS;
	}

	FunctionType(String key, Class<?>[] types) {
		name = key;
		ptypes = types;

		int h = 31 * key.hashCode();
		for (Class<?> type : types)
			h = 31 * h + type.hashCode();

		hash = h;
	}

	public static FunctionType inst(String name, Class<?>... types) {
		FunctionType res = recyclePool.obtain();
		res.name = name;
		res.ptypes = types;

		int h = 31 * name.hashCode();
		for (Class<?> type : types)
			h = 31 * h + type.hashCode();

		res.hash = h;

		return res;
	}

	public static FunctionType inst(String name, Object... param) {
		return inst(name, Reflections.toTypesUnwrapped(param));
	}

	public static FunctionType from(String name, MethodType type) {
		return inst(name, type.parameterArray());
	}

	public static FunctionType from(Method method) {
		return inst(method.getName(), method.getParameterTypes());
	}

	public static FunctionType from(Constructor<?> method) {
		return inst("<init>", method.getParameterTypes());
	}

	public static FunctionType generic(String name, int argCount) {
		Class<?>[] types = new Class[argCount];
		Arrays.fill(types, void.class);
		return inst(name, types);
	}

	public static void recycle(FunctionType type) {
		recyclePool.free(type);
	}

	public boolean match(String key, Object... args) {
		return name.equals(key) && Reflections.match(ptypes, args);
	}

	public boolean match(String key, Class<?>... types) {
		return name.equals(key) && Reflections.match(ptypes, types);
	}

	public boolean match(FunctionType type) {
		return name.equals(type.name) && Reflections.match(ptypes, type.ptypes);
	}

	public String name() {
		return name;
	}

	/** Parameter class array, do not modify it. */
	public Class<?>[] parameterTypes() {
		return ptypes;
	}

	/** Recycle the object when no longer referenced. */
	public void recycle() {
		recyclePool.free(this);
	}

	@Override
	public void reset() {
		name = "";
		ptypes = Constant.EMPTY_CLASS;
		hash = 0;
	}

	@Override
	public boolean equals(Object obj) {
		return this == obj || obj instanceof FunctionType that && name.equals(that.name) && Arrays.equals(ptypes, that.ptypes);
	}

	@Override
	public int hashCode() {
		return hash;
	}

	@Override
	public String toString() {
		StringBuilder buf = new StringBuilder();

		int max = ptypes.length;

		buf.append(name).append('(');
		int i = 0;
		while (true) {
			buf.append(ptypes[i++].getName());
			if (i == max) return buf.append(')').toString();
			buf.append(',');
		}
	}
}
