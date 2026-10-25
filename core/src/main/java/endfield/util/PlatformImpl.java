package endfield.util;

import endfield.util.aspector.accesses.PackageAccessHandler;
import endfield.util.aspector.classes.ClassAccessor;
import mindustry.Vars;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

/**
 * Handling APIs for different implementations of Desktop and Android.
 *
 * @since 1.0.9
 */
public interface PlatformImpl {
	<T> T clone(T object);

	default FieldAccessor fieldAccessor(Field field) {
		return new ReflectionFieldAccessor(field);
	}

	default MethodAccessor methodAccessor(Method method) {
		return new ReflectionMethodAccessor(method);
	}

	default <T> ConstructorAccessor<T> constructorAccessor(Constructor<T> constructor) {
		return new ReflectionConstructorAccessor<>(constructor);
	}

	default MethodInvoker methodInvoker(Method method) {
		return (method.getModifiers() & Modifier.STATIC) != 0 ?
				new ReflectionStaticMethodInvoker(method) :
				new ReflectionVirtualMethodInvoker(method);
	}

	default MethodInvoker methodInvoker(Constructor<?> constructor) {
		return new ReflectionConstructorInvoker(constructor);
	}

	default PackageAccessHandler packageAccessHandler(ClassAccessor accessor) {
		throw new UnsupportedOperationException();
	}

	void put(long srcAddress, long destAddress, long bytes);

	void put(Object src, int srcOffset, Object dst, int dstOffset, long bytes);

	default Class<?> callerClass() {
		Thread thread = Thread.currentThread();
		StackTraceElement[] trace = thread.getStackTrace();
		try {
			return Class.forName(trace[3].getClassName(), false, Vars.mods.mainLoader());
		} catch (ClassNotFoundException e) {
			return null;
		}
	}
}
