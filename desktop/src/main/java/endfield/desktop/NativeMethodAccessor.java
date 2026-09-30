package endfield.desktop;

import endfield.util.AbstractMethodAccessor;
import endfield.util.Reflects;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodType;
import java.lang.reflect.Method;
import java.util.Objects;

import static endfield.desktop.DesktopImpl.lookup;
import static endfield.util.GetKt.sneakyThrow;

public class NativeMethodAccessor extends AbstractMethodAccessor {
	static final MethodHandle invoke;

	static {
		try {
			Class<?> dec = Objects.requireNonNullElseGet(
					Reflects.findClass("jdk.internal.reflect.DirectMethodHandleAccessor$NativeAccessor"), () ->
					Reflects.findClass("jdk.internal.reflect.NativeMethodAccessorImpl")
			);
			invoke = lookup.findStatic(dec, "invoke0", MethodType.methodType(Object.class, Method.class, Object.class, Object[].class));
		} catch (NoSuchMethodException | IllegalAccessException e) {
			throw sneakyThrow(e);
		}
	}

	public NativeMethodAccessor(Method method) {
		super(method);
	}

	@SuppressWarnings("unchecked")
	public static <T> T invoke(Method method, Object object, Object[] args) throws Throwable {
		return (T) invoke.invokeExact(method, object, args);
	}

	@Override
	public <T> T invoke(Object object, Object... args) {
		try {
			return invoke(method, object, args);
		} catch (Throwable e) {
			throw sneakyThrow(e);
		}
	}
}
