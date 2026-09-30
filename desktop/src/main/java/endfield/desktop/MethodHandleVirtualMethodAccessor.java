package endfield.desktop;

import endfield.util.AbstractMethodAccessor;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodType;
import java.lang.reflect.Method;

import static endfield.desktop.DesktopImpl.lookup;
import static endfield.util.GetKt.sneakyThrow;

public final class MethodHandleVirtualMethodAccessor extends AbstractMethodAccessor {
	final MethodHandle spreadHandle;

	public MethodHandleVirtualMethodAccessor(Method method) {
		super(method);

		try {
			MethodHandle target = lookup.unreflect(method).asFixedArity();

			int paramCount = target.type().parameterCount();

			if (paramCount < 1)
				throw new IllegalArgumentException("Instance method must have e receiver");
			MethodHandle spread = target.asSpreader(Object[].class, paramCount -1);
			MethodType newType = spread.type()
					.changeParameterType(0, Object.class)
					.changeReturnType(Object.class);
			spreadHandle = spread.asType(newType);
		} catch (IllegalAccessException e) {
			throw sneakyThrow(e);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T invoke(Object object, Object... args) {
		try {
			return (T) spreadHandle.invokeExact(object, args);
		} catch (Throwable e) {
			throw sneakyThrow(e);
		}
	}
}
