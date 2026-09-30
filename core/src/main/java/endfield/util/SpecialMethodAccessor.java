package endfield.util;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodType;
import java.lang.reflect.Method;

import static endfield.util.GetKt.sneakyThrow;

/**
 * @since 1.0.10
 */
public class SpecialMethodAccessor extends AbstractMethodAccessor {
	final MethodHandle spreadHandle;

	public SpecialMethodAccessor(Method method) {
		super(method);

		MethodHandle target = ReflectsKt.findSpecial(method).asFixedArity();

		int paramCount = target.type().parameterCount();

		MethodHandle spread = target.asSpreader(Object[].class, paramCount -1);
		MethodType newType = spread.type()
				.changeParameterType(0, Object.class)
				.changeReturnType(Object.class);
		spreadHandle = spread.asType(newType);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T invoke(Object obj, Object... args) {
		try {
			return (T) spreadHandle.invokeExact(obj, args);
		} catch (Throwable e) {
			throw sneakyThrow(e);
		}
	}
}
