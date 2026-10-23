package endfield.util;

import java.lang.reflect.Method;

public abstract class AbstractMethodAccessor implements MethodAccessor {
	protected final Method method;

	protected AbstractMethodAccessor(Method m) {
		method = m;
	}

	@Override
	public Method getMethod() {
		return method;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + ": " + method.toString();
	}
}
