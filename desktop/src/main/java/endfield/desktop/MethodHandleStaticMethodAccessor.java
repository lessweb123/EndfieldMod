package endfield.desktop;

import endfield.util.AbstractMethodAccessor;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodType;
import java.lang.reflect.Method;

import static endfield.desktop.DesktopImpl.lookup;
import static endfield.util.GetKt.sneakyThrow;

public final class MethodHandleStaticMethodAccessor extends AbstractMethodAccessor {
	final MethodHandle spreadHandle;

	public MethodHandleStaticMethodAccessor(Method method) {
		super(method);

		try {
			MethodHandle target = lookup.unreflect(method).asFixedArity();

			int paramCount = target.type().parameterCount();

			spreadHandle = target.asSpreader(Object[].class, paramCount)
					.asType(MethodType.methodType(Object.class, Object[].class));
		} catch (IllegalAccessException e) {
			throw sneakyThrow(e);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T invoke(Object object, Object... args) {
		try {
			return (T) spreadHandle.invokeExact(args);
		} catch (Throwable e) {
			throw sneakyThrow(e);
		}
	}
}
