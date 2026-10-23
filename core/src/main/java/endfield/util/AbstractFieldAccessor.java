package endfield.util;

import java.lang.reflect.Field;

public abstract class AbstractFieldAccessor implements FieldAccessor {
	protected final Field field;

	protected AbstractFieldAccessor(Field f) {
		field = f;
	}

	@Override
	public Field getField() {
		return field;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + ": " + field.toString();
	}
}
