package endfield.util;

import java.lang.reflect.Field;

import static endfield.util.GetKt.sneakyThrow;

public class ReflectionFieldAccessor extends AbstractFieldAccessor {
	public ReflectionFieldAccessor(Field field) {
		super(field);

		if (!Reflects.setAccessible(field)) throw new IllegalArgumentException("Unable to access field: " + field);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T get(Object object) {
		try {
			return (T) field.get(object);
		} catch (IllegalAccessException e) {
			throw sneakyThrow(e);
		}
	}

	@Override
	public void set(Object object, Object value) {
		try {
			field.set(object, value);
		} catch (IllegalAccessException e) {
			throw sneakyThrow(e);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T getObject(Object object) {
		try {
			return (T) field.get(object);
		} catch (IllegalAccessException e) {
			throw sneakyThrow(e);
		}
	}

	@Override
	public void setObject(Object object, Object value) {
		try {
			field.set(object, value);
		} catch (IllegalAccessException e) {
			throw sneakyThrow(e);
		}
	}

	@Override
	public boolean getBoolean(Object object) {
		try {
			return field.getBoolean(object);
		} catch (IllegalAccessException e) {
			throw sneakyThrow(e);
		}
	}

	@Override
	public void setBoolean(Object object, boolean value) {
		try {
			field.setBoolean(object, value);
		} catch (IllegalAccessException e) {
			throw sneakyThrow(e);
		}
	}

	@Override
	public byte getByte(Object object) {
		try {
			return field.getByte(object);
		} catch (IllegalAccessException e) {
			throw sneakyThrow(e);
		}
	}

	@Override
	public void setByte(Object object, byte value) {
		try {
			field.setByte(object, value);
		} catch (IllegalAccessException e) {
			throw sneakyThrow(e);
		}
	}

	@Override
	public char getChar(Object object) {
		try {
			return field.getChar(object);
		} catch (IllegalAccessException e) {
			throw sneakyThrow(e);
		}
	}

	@Override
	public void setChar(Object object, char value) {
		try {
			field.setChar(object, value);
		} catch (IllegalAccessException e) {
			throw sneakyThrow(e);
		}
	}

	@Override
	public short getShort(Object object) {
		try {
			return field.getShort(object);
		} catch (IllegalAccessException e) {
			throw sneakyThrow(e);
		}
	}

	@Override
	public void setShort(Object object, short value) {
		try {
			field.setShort(object, value);
		} catch (IllegalAccessException e) {
			throw sneakyThrow(e);
		}
	}

	@Override
	public int getInt(Object object) {
		try {
			return field.getInt(object);
		} catch (IllegalAccessException e) {
			throw sneakyThrow(e);
		}
	}

	@Override
	public void setInt(Object object, int value) {
		try {
			field.setInt(object, value);
		} catch (IllegalAccessException e) {
			throw sneakyThrow(e);
		}
	}

	@Override
	public long getLong(Object object) {
		try {
			return field.getLong(object);
		} catch (IllegalAccessException e) {
			throw sneakyThrow(e);
		}
	}

	@Override
	public void setLong(Object object, long value) {
		try {
			field.setLong(object, value);
		} catch (IllegalAccessException e) {
			throw sneakyThrow(e);
		}
	}

	@Override
	public float getFloat(Object object) {
		try {
			return field.getFloat(object);
		} catch (IllegalAccessException e) {
			throw sneakyThrow(e);
		}
	}

	@Override
	public void setFloat(Object object, float value) {
		try {
			field.setFloat(object, value);
		} catch (IllegalAccessException e) {
			throw sneakyThrow(e);
		}
	}

	@Override
	public double getDouble(Object object) {
		try {
			return field.getDouble(object);
		} catch (IllegalAccessException e) {
			throw sneakyThrow(e);
		}
	}

	@Override
	public void setDouble(Object object, double value) {
		try {
			field.setDouble(object, value);
		} catch (IllegalAccessException e) {
			throw sneakyThrow(e);
		}
	}
}
