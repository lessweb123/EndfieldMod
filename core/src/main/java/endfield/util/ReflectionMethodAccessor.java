package endfield.util;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import static endfield.util.GetKt.sneakyThrow;

public class ReflectionMethodAccessor extends AbstractMethodAccessor {
	public ReflectionMethodAccessor(Method method) {
		super(method);

		if (!Reflects.setAccessible(method)) throw new IllegalArgumentException("Unable to access method: " + method);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T invoke(Object object, Object... args) {
		try {
			return (T) method.invoke(object, args);
		} catch (IllegalAccessException | InvocationTargetException e) {
			throw sneakyThrow(e);
		}
	}
}
