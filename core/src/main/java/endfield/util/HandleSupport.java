package endfield.util;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.invoke.MethodType;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import static endfield.Vars2.invokers;

public final class HandleSupport {
	private HandleSupport() {}

	public static Lookup lookup(Class<?> clazz) {
		return invokers.lookup(clazz);
	}

	public static MethodHandle findStatic(Class<?> refc, String name, MethodType type) {
		return invokers.findStatic(refc, name, type);
	}

	public static MethodHandle findVirtual(Class<?> refc, String name, MethodType type) {
		return invokers.findVirtual(refc, name, type);
	}

	public static MethodHandle findConstructor(Class<?> refc, MethodType type) {
		return invokers.findConstructor(refc, type);
	}

	public static MethodHandle findSpecial(Class<?> refc, String name, MethodType type) {
		return invokers.findSpecial(refc, name, type);
	}

	public static MethodHandle findGetter(Class<?> refc, String name, Class<?> type) {
		return invokers.findGetter(refc, name, type);
	}

	public static MethodHandle findSetter(Class<?> refc, String name, Class<?> type) {
		return invokers.findSetter(refc, name, type);
	}

	public static MethodHandle findStaticGetter(Class<?> refc, String name, Class<?> type) {
		return invokers.findStaticGetter(refc, name, type);
	}

	public static MethodHandle findStaticSetter(Class<?> refc, String name, Class<?> type) {
		return invokers.findStaticSetter(refc, name, type);
	}

	public static MethodHandle unreflect(Method method) {
		return invokers.unreflect(method);
	}

	public static MethodHandle unreflectSpecial(Method method) {
		return invokers.unreflectSpecial(method);
	}

	public static MethodHandle unreflectConstructor(Constructor<?> constructor) {
		return invokers.unreflectConstructor(constructor);
	}

	public static MethodHandle unreflectGetter(Field field) {
		return invokers.unreflectGetter(field);
	}

	public static MethodHandle unreflectSetter(Field field) {
		return invokers.unreflectSetter(field);
	}
}
