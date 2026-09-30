package endfield.desktop;

import endfield.util.AbstractConstructorAccessor;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodType;
import java.lang.reflect.Constructor;

import static endfield.desktop.DesktopImpl.lookup;
import static endfield.util.GetKt.sneakyThrow;

public final class MethodHandleConstructorAccessor<T> extends AbstractConstructorAccessor<T> {
	final MethodHandle spreadHandle;

	public MethodHandleConstructorAccessor(Constructor<T> cons) {
		super(cons);

		try {
			MethodHandle target = lookup.unreflectConstructor(cons).asFixedArity();

			int paramCount = target.type().parameterCount();
			MethodHandle spread = target.asSpreader(Object[].class, paramCount);
			spreadHandle = spread.asType(MethodType.methodType(Object.class, Object[].class));
		} catch (IllegalAccessException e) {
			throw sneakyThrow(e);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public T newInstance(Object... args) {
		try {
			return (T) spreadHandle.invokeExact(args);
		} catch (Throwable e) {
			throw sneakyThrow(e);
		}
	}
}
