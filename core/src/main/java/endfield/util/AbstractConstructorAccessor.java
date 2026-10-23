package endfield.util;

import java.lang.reflect.Constructor;

public abstract class AbstractConstructorAccessor<T> implements ConstructorAccessor<T> {
	protected final Constructor<T> constructor;

	protected AbstractConstructorAccessor(Constructor<T> cons) {
		constructor = cons;
	}

	@Override
	public Constructor<T> getConstructor() {
		return constructor;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + ": " + constructor.toString();
	}
}
