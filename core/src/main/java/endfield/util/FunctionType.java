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
	Class<?>[] paramType;
	int hash;

	FunctionType() {
		name = "";
		paramType = Constant.EMPTY_CLASS;
	}

	FunctionType(String key, Class<?>[] types) {
		name = key;
		paramType = types;
		hash = 31 * key.hashCode() + Arrays.hashCode(types);
	}

	public static FunctionType inst(String name, Class<?>... paramType) {
		FunctionType res = recyclePool.obtain();
		res.name = name;
		res.paramType = paramType;
		res.hash = 31 * name.hashCode() + Arrays.hashCode(paramType);
		return res;
	}

	public static FunctionType inst(String name, Object... param) {
		return inst(name, Reflections.unwrapped(Reflections.toTypes(param)));
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
		return name.equals(key) && Reflections.match(paramType, args);
	}

	public boolean match(String key, Class<?>... types) {
		return name.equals(key) && Reflections.match(paramType, types);
	}

	public boolean match(FunctionType type) {
		return name.equals(type.name) && Reflections.match(paramType, type.paramType);
	}

	public String name() {
		return name;
	}

	public Class<?>[] paramType() {
		return paramType;
	}

	public void recycle() {
		recyclePool.free(this);
	}

	@Override
	public void reset() {
		name = "";
		paramType = Constant.EMPTY_CLASS;
		hash = 0;
	}

	@Override
	public boolean equals(Object obj) {
		return this == obj || obj instanceof FunctionType that && name.equals(that.name) && Arrays.equals(paramType, that.paramType);
	}

	@Override
	public int hashCode() {
		return hash;
	}

	@Override
	public String toString() {
		StringBuilder buf = new StringBuilder();

		int max = paramType.length;

		buf.append(name).append('(');
		int i = 0;
		while (true) {
			buf.append(paramType[i++].getName());
			if (i == max) return buf.append(')').toString();
			buf.append(',');
		}
	}
}
