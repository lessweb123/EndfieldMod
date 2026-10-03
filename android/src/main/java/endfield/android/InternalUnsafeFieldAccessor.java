package endfield.android;

import endfield.android.util.Fields;
import endfield.util.AbstractFieldAccessor;
import endfield.util.FieldAccessor;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import static endfield.android.InternalUnsafer.internalUnsafe;
import static endfield.util.Reflects.getGetMessage;
import static endfield.util.Reflects.getSetMessage;

public class InternalUnsafeFieldAccessor extends AbstractFieldAccessor {
	protected final long offset;

	protected InternalUnsafeFieldAccessor(Field field) {
		super(field);
		offset = Fields.getOffset(field);
	}

	public static FieldAccessor getUnsafeFieldAccessor(Field field) {
		Class<?> type = field.getType();
		int modifiers = field.getModifiers();

		if ((modifiers & Modifier.STATIC) != 0) {
			if ((modifiers & Modifier.VOLATILE) != 0) {
				if (type.isPrimitive()) {
					if (type == boolean.class) return new InternalUnsafeQualifiedStaticBooleanFieldAccessor(field);
					else if (type == byte.class) return new InternalUnsafeQualifiedStaticByteFieldAccessor(field);
					else if (type == char.class) return new InternalUnsafeQualifiedStaticCharFieldAccessor(field);
					else if (type == short.class) return new InternalUnsafeQualifiedStaticShortFieldAccessor(field);
					else if (type == int.class) return new InternalUnsafeQualifiedStaticIntFieldAccessor(field);
					else if (type == long.class) return new InternalUnsafeQualifiedStaticLongFieldAccessor(field);
					else if (type == float.class) return new InternalUnsafeQualifiedStaticFloatFieldAccessor(field);
					else if (type == double.class) return new InternalUnsafeQualifiedStaticDoubleFieldAccessor(field);
					else throw new IllegalArgumentException("unknown type of field " + field);
				} else return new InternalUnsafeQualifiedStaticObjectFieldAccessor(field);
			} else {
				if (type.isPrimitive()) {
					if (type == boolean.class) return new InternalUnsafeStaticBooleanFieldAccessor(field);
					else if (type == byte.class) return new InternalUnsafeStaticByteFieldAccessor(field);
					else if (type == char.class) return new InternalUnsafeStaticCharFieldAccessor(field);
					else if (type == short.class) return new InternalUnsafeStaticShortFieldAccessor(field);
					else if (type == int.class) return new InternalUnsafeStaticIntFieldAccessor(field);
					else if (type == long.class) return new InternalUnsafeStaticLongFieldAccessor(field);
					else if (type == float.class) return new InternalUnsafeStaticFloatFieldAccessor(field);
					else if (type == double.class) return new InternalUnsafeStaticDoubleFieldAccessor(field);
					else throw new IllegalArgumentException("unknown type of field " + field);
				} else return new InternalUnsafeStaticObjectFieldAccessor(field);
			}
		} else {
			if ((modifiers & Modifier.VOLATILE) != 0) {
				if (type.isPrimitive()) {
					if (type == boolean.class) return new InternalUnsafeQualifiedBooleanFieldAccessor(field);
					else if (type == byte.class) return new InternalUnsafeQualifiedByteFieldAccessor(field);
					else if (type == char.class) return new InternalUnsafeQualifiedCharFieldAccessor(field);
					else if (type == short.class) return new InternalUnsafeQualifiedShortFieldAccessor(field);
					else if (type == int.class) return new InternalUnsafeQualifiedIntFieldAccessor(field);
					else if (type == long.class) return new InternalUnsafeQualifiedLongFieldAccessor(field);
					else if (type == float.class) return new InternalUnsafeQualifiedFloatFieldAccessor(field);
					else if (type == double.class) return new InternalUnsafeQualifiedDoubleFieldAccessor(field);
					else throw new IllegalArgumentException("unknown type of field " + field);
				} else return new InternalUnsafeQualifiedObjectFieldAccessor(field);
			} else {
				if (type.isPrimitive()) {
					if (type == boolean.class) return new InternalUnsafeBooleanFieldAccessor(field);
					else if (type == byte.class) return new InternalUnsafeByteFieldAccessor(field);
					else if (type == char.class) return new InternalUnsafeCharFieldAccessor(field);
					else if (type == short.class) return new InternalUnsafeShortFieldAccessor(field);
					else if (type == int.class) return new InternalUnsafeIntFieldAccessor(field);
					else if (type == long.class) return new InternalUnsafeLongFieldAccessor(field);
					else if (type == float.class) return new InternalUnsafeFloatFieldAccessor(field);
					else if (type == double.class) return new InternalUnsafeDoubleFieldAccessor(field);
					else throw new IllegalArgumentException("unknown type of field " + field);
				} else return new InternalUnsafeObjectFieldAccessor(field);
			}
		}
	}

	public void ensureObject(Object object) {
		if (!field.getDeclaringClass().isInstance(object))
			throw new IllegalArgumentException(getSetMessage(field, object));
	}

	public void ensureValue(Object value) {
		if (value != null && !field.getType().isInstance(value))
			throw new IllegalArgumentException(getSetMessage(field, value));
	}

	@Override
	public <T> T get(Object object) {
		throw new IllegalArgumentException(getGetMessage(field, Object.class.getName()));
	}

	@Override
	public void set(Object object, Object value) {
		throw new IllegalArgumentException(getSetMessage(field, value));
	}

	@Override
	public <T> T getObject(Object object) {
		throw new IllegalArgumentException(getGetMessage(field, Object.class.getName()));
	}

	@Override
	public void setObject(Object object, Object value) {
		throw new IllegalArgumentException(getSetMessage(field, value));
	}

	@Override
	public boolean getBoolean(Object object) {
		throw new IllegalArgumentException(getGetMessage(field, "boolean"));
	}

	@Override
	public void setBoolean(Object object, boolean value) {
		throw new IllegalArgumentException(getSetMessage(field, "boolean", String.valueOf(value)));
	}

	@Override
	public byte getByte(Object object) {
		throw new IllegalArgumentException(getGetMessage(field, "boolean"));
	}

	@Override
	public void setByte(Object object, byte value) {
		throw new IllegalArgumentException(getSetMessage(field, "byte", String.valueOf(value)));
	}

	@Override
	public char getChar(Object object) {
		throw new IllegalArgumentException(getGetMessage(field, "char"));
	}

	@Override
	public void setChar(Object object, char value) {
		throw new IllegalArgumentException(getSetMessage(field, "char", String.valueOf(value)));
	}

	@Override
	public short getShort(Object object) {
		throw new IllegalArgumentException(getGetMessage(field, "short"));
	}

	@Override
	public void setShort(Object object, short value) {
		throw new IllegalArgumentException(getSetMessage(field, "short", String.valueOf(value)));
	}

	@Override
	public int getInt(Object object) {
		throw new IllegalArgumentException(getGetMessage(field, "int"));
	}

	@Override
	public void setInt(Object object, int value) {
		throw new IllegalArgumentException(getSetMessage(field, "int", String.valueOf(value)));
	}

	@Override
	public long getLong(Object object) {
		throw new IllegalArgumentException(getGetMessage(field, "long"));
	}

	@Override
	public void setLong(Object object, long value) {
		throw new IllegalArgumentException(getSetMessage(field, "long", String.valueOf(value)));
	}

	@Override
	public float getFloat(Object object) {
		throw new IllegalArgumentException(getGetMessage(field, "float"));
	}

	@Override
	public void setFloat(Object object, float value) {
		throw new IllegalArgumentException(getSetMessage(field, "float", String.valueOf(value)));
	}

	@Override
	public double getDouble(Object object) {
		throw new IllegalArgumentException(getGetMessage(field, "double"));
	}

	@Override
	public void setDouble(Object object, double value) {
		throw new IllegalArgumentException(getSetMessage(field, "double", String.valueOf(value)));
	}
}

class InternalUnsafeObjectFieldAccessor extends InternalUnsafeFieldAccessor {
	public InternalUnsafeObjectFieldAccessor(Field f) {
		super(f);
	}

	@Override
	public <T> T get(Object object) {
		return getObject(object);
	}

	@Override
	public void set(Object object, Object value) {
		setObject(object, value);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T getObject(Object object) {
		ensureObject(object);
		return (T) internalUnsafe.getReference(object, offset);
	}

	@Override
	public void setObject(Object object, Object value) {
		ensureObject(object);
		ensureValue(value);
		internalUnsafe.putReference(object, offset, value);
	}
}

class InternalUnsafeBooleanFieldAccessor extends InternalUnsafeFieldAccessor {
	public InternalUnsafeBooleanFieldAccessor(Field f) {
		super(f);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T get(Object object) {
		return (T) Boolean.valueOf(getBoolean(object));
	}

	@Override
	public void set(Object object, Object value) {
		setBoolean(object, (boolean) value);
	}

	@Override
	public boolean getBoolean(Object object) {
		ensureObject(object);
		return internalUnsafe.getBoolean(object, offset);
	}

	@Override
	public void setBoolean(Object object, boolean value) {
		ensureObject(object);
		internalUnsafe.putBoolean(object, offset, value);
	}
}

class InternalUnsafeByteFieldAccessor extends InternalUnsafeFieldAccessor {
	public InternalUnsafeByteFieldAccessor(Field f) {
		super(f);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T get(Object object) {
		return (T) Byte.valueOf(getByte(object));
	}

	@Override
	public void set(Object object, Object value) {
		setByte(object, ((Number) value).byteValue());
	}

	@Override
	public byte getByte(Object object) {
		ensureObject(object);
		return internalUnsafe.getByte(object, offset);
	}

	@Override
	public void setByte(Object object, byte value) {
		ensureObject(object);
		internalUnsafe.putByte(object, offset, value);
	}

	@Override
	public short getShort(Object object) {
		return getByte(object);
	}

	@Override
	public int getInt(Object object) {
		return getByte(object);
	}

	@Override
	public long getLong(Object object) {
		return getByte(object);
	}

	@Override
	public float getFloat(Object object) {
		return getByte(object);
	}

	@Override
	public double getDouble(Object object) {
		return getByte(object);
	}
}

class InternalUnsafeCharFieldAccessor extends InternalUnsafeFieldAccessor {
	public InternalUnsafeCharFieldAccessor(Field f) {
		super(f);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T get(Object object) {
		return (T) Character.valueOf(getChar(object));
	}

	@Override
	public void set(Object object, Object value) {
		setChar(object, (char) value);
	}

	@Override
	public char getChar(Object object) {
		ensureObject(object);
		return internalUnsafe.getChar(object, offset);
	}

	@Override
	public void setChar(Object object, char value) {
		ensureObject(object);
		internalUnsafe.putChar(object, offset, value);
	}

	@Override
	public int getInt(Object object) {
		return getChar(object);
	}

	@Override
	public long getLong(Object object) {
		return getChar(object);
	}

	@Override
	public float getFloat(Object object) {
		return getChar(object);
	}

	@Override
	public double getDouble(Object object) {
		return getChar(object);
	}
}

class InternalUnsafeShortFieldAccessor extends InternalUnsafeFieldAccessor {
	public InternalUnsafeShortFieldAccessor(Field f) {
		super(f);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T get(Object object) {
		return (T) Short.valueOf(getShort(object));
	}

	@Override
	public void set(Object object, Object value) {
		setShort(object, ((Number) value).shortValue());
	}

	@Override
	public void setByte(Object object, byte value) {
		setShort(object, value);
	}

	@Override
	public short getShort(Object object) {
		ensureObject(object);
		return internalUnsafe.getShort(object, offset);
	}

	@Override
	public void setShort(Object object, short value) {
		ensureObject(object);
		internalUnsafe.putShort(object, offset, value);
	}

	@Override
	public int getInt(Object object) {
		return getShort(object);
	}

	@Override
	public long getLong(Object object) {
		return getShort(object);
	}

	@Override
	public float getFloat(Object object) {
		return getShort(object);
	}

	@Override
	public double getDouble(Object object) {
		return getShort(object);
	}
}

class InternalUnsafeIntFieldAccessor extends InternalUnsafeFieldAccessor {
	public InternalUnsafeIntFieldAccessor(Field f) {
		super(f);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T get(Object object) {
		return (T) Integer.valueOf(getInt(object));
	}

	@Override
	public void set(Object object, Object value) {
		setInt(object, ((Number) value).intValue());
	}

	@Override
	public void setByte(Object object, byte value) {
		setInt(object, value);
	}

	@Override
	public void setChar(Object object, char value) {
		setInt(object, value);
	}

	@Override
	public void setShort(Object object, short value) {
		setInt(object, value);
	}

	@Override
	public int getInt(Object object) {
		ensureObject(object);
		return internalUnsafe.getInt(object, offset);
	}

	@Override
	public void setInt(Object object, int value) {
		ensureObject(object);
		internalUnsafe.putInt(object, offset, value);
	}

	@Override
	public long getLong(Object object) {
		return getInt(object);
	}

	@Override
	public float getFloat(Object object) {
		return getInt(object);
	}

	@Override
	public double getDouble(Object object) {
		return getInt(object);
	}
}

class InternalUnsafeLongFieldAccessor extends InternalUnsafeFieldAccessor {
	public InternalUnsafeLongFieldAccessor(Field f) {
		super(f);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T get(Object object) {
		return (T) Long.valueOf(getLong(object));
	}

	@Override
	public void set(Object object, Object value) {
		setLong(object, ((Number) value).longValue());
	}

	@Override
	public void setByte(Object object, byte value) {
		setLong(object, value);
	}

	@Override
	public void setChar(Object object, char value) {
		setLong(object, value);
	}

	@Override
	public void setShort(Object object, short value) {
		setLong(object, value);
	}

	@Override
	public void setInt(Object object, int value) {
		setLong(object, value);
	}

	@Override
	public long getLong(Object object) {
		ensureObject(object);
		return internalUnsafe.getLong(object, offset);
	}

	@Override
	public void setLong(Object object, long value) {
		ensureObject(object);
		internalUnsafe.putLong(object, offset, value);
	}

	@Override
	public float getFloat(Object object) {
		return getLong(object);
	}

	@Override
	public double getDouble(Object object) {
		return getLong(object);
	}
}

class InternalUnsafeFloatFieldAccessor extends InternalUnsafeFieldAccessor {
	public InternalUnsafeFloatFieldAccessor(Field f) {
		super(f);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T get(Object object) {
		return (T) Float.valueOf(getFloat(object));
	}

	@Override
	public void set(Object object, Object value) {
		setFloat(object, ((Number) value).floatValue());
	}

	@Override
	public void setByte(Object object, byte value) {
		setFloat(object, value);
	}

	@Override
	public void setChar(Object object, char value) {
		setFloat(object, value);
	}

	@Override
	public void setShort(Object object, short value) {
		setFloat(object, value);
	}

	@Override
	public void setInt(Object object, int value) {
		setFloat(object, value);
	}

	@Override
	public void setLong(Object object, long value) {
		setFloat(object, value);
	}

	@Override
	public float getFloat(Object object) {
		ensureObject(object);
		return internalUnsafe.getFloat(object, offset);
	}

	@Override
	public void setFloat(Object object, float value) {
		ensureObject(object);
		internalUnsafe.putFloat(object, offset, value);
	}

	@Override
	public double getDouble(Object object) {
		return getFloat(object);
	}
}

class InternalUnsafeDoubleFieldAccessor extends InternalUnsafeFieldAccessor {
	public InternalUnsafeDoubleFieldAccessor(Field f) {
		super(f);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T get(Object object) {
		return (T) Double.valueOf(getDouble(object));
	}

	@Override
	public void set(Object object, Object value) {
		setDouble(object, ((Number) value).doubleValue());
	}

	@Override
	public void setByte(Object object, byte value) {
		setDouble(object, value);
	}

	@Override
	public void setChar(Object object, char value) {
		setDouble(object, value);
	}

	@Override
	public void setShort(Object object, short value) {
		setDouble(object, value);
	}

	@Override
	public void setInt(Object object, int value) {
		setDouble(object, value);
	}

	@Override
	public void setLong(Object object, long value) {
		setDouble(object, value);
	}

	@Override
	public void setFloat(Object object, float value) {
		setDouble(object, value);
	}

	@Override
	public double getDouble(Object object) {
		ensureObject(object);
		return internalUnsafe.getDouble(object, offset);
	}

	@Override
	public void setDouble(Object object, double value) {
		ensureObject(object);
		internalUnsafe.putDouble(object, offset, value);
	}
}

class InternalUnsafeQualifiedObjectFieldAccessor extends InternalUnsafeObjectFieldAccessor {
	public InternalUnsafeQualifiedObjectFieldAccessor(Field f) {
		super(f);
	}

	@Override
	public <T> T get(Object object) {
		return getObject(object);
	}

	@Override
	public void set(Object object, Object value) {
		setObject(object, value);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T getObject(Object object) {
		ensureObject(object);
		return (T) internalUnsafe.getReferenceVolatile(object, offset);
	}

	@Override
	public void setObject(Object object, Object value) {
		ensureObject(object);
		ensureValue(value);
		internalUnsafe.putReferenceVolatile(object, offset, value);
	}
}

class InternalUnsafeQualifiedBooleanFieldAccessor extends InternalUnsafeBooleanFieldAccessor {
	public InternalUnsafeQualifiedBooleanFieldAccessor(Field f) {
		super(f);
	}

	@Override
	public boolean getBoolean(Object object) {
		ensureObject(object);
		return internalUnsafe.getBooleanVolatile(object, offset);
	}

	@Override
	public void setBoolean(Object object, boolean value) {
		ensureObject(object);
		internalUnsafe.putBooleanVolatile(object, offset, value);
	}
}

class InternalUnsafeQualifiedByteFieldAccessor extends InternalUnsafeByteFieldAccessor {
	public InternalUnsafeQualifiedByteFieldAccessor(Field f) {
		super(f);
	}

	@Override
	public byte getByte(Object object) {
		ensureObject(object);
		return internalUnsafe.getByteVolatile(object, offset);
	}

	@Override
	public void setByte(Object object, byte value) {
		ensureObject(object);
		internalUnsafe.putByteVolatile(object, offset, value);
	}
}

class InternalUnsafeQualifiedCharFieldAccessor extends InternalUnsafeCharFieldAccessor {
	public InternalUnsafeQualifiedCharFieldAccessor(Field f) {
		super(f);
	}

	@Override
	public char getChar(Object object) {
		ensureObject(object);
		return internalUnsafe.getCharVolatile(object, offset);
	}

	@Override
	public void setChar(Object object, char value) {
		ensureObject(object);
		internalUnsafe.putCharVolatile(object, offset, value);
	}
}

class InternalUnsafeQualifiedShortFieldAccessor extends InternalUnsafeShortFieldAccessor {
	public InternalUnsafeQualifiedShortFieldAccessor(Field f) {
		super(f);
	}

	@Override
	public short getShort(Object object) {
		ensureObject(object);
		return internalUnsafe.getShortVolatile(object, offset);
	}

	@Override
	public void setShort(Object object, short value) {
		ensureObject(object);
		internalUnsafe.putShortVolatile(object, offset, value);
	}
}

class InternalUnsafeQualifiedIntFieldAccessor extends InternalUnsafeIntFieldAccessor {
	public InternalUnsafeQualifiedIntFieldAccessor(Field f) {
		super(f);
	}

	@Override
	public int getInt(Object object) {
		ensureObject(object);
		return internalUnsafe.getIntVolatile(object, offset);
	}

	@Override
	public void setInt(Object object, int value) {
		ensureObject(object);
		internalUnsafe.putIntVolatile(object, offset, value);
	}
}

class InternalUnsafeQualifiedLongFieldAccessor extends InternalUnsafeLongFieldAccessor {
	public InternalUnsafeQualifiedLongFieldAccessor(Field f) {
		super(f);
	}

	@Override
	public long getLong(Object object) {
		ensureObject(object);
		return internalUnsafe.getLongVolatile(object, offset);
	}

	@Override
	public void setLong(Object object, long value) {
		ensureObject(object);
		internalUnsafe.putLongVolatile(object, offset, value);
	}
}

class InternalUnsafeQualifiedFloatFieldAccessor extends InternalUnsafeFloatFieldAccessor {
	public InternalUnsafeQualifiedFloatFieldAccessor(Field f) {
		super(f);
	}

	@Override
	public float getFloat(Object object) {
		ensureObject(object);
		return internalUnsafe.getFloatVolatile(object, offset);
	}

	@Override
	public void setFloat(Object object, float value) {
		ensureObject(object);
		internalUnsafe.putFloatVolatile(object, offset, value);
	}
}

class InternalUnsafeQualifiedDoubleFieldAccessor extends InternalUnsafeDoubleFieldAccessor {
	public InternalUnsafeQualifiedDoubleFieldAccessor(Field f) {
		super(f);
	}

	@Override
	public double getDouble(Object object) {
		ensureObject(object);
		return internalUnsafe.getDoubleVolatile(object, offset);
	}

	@Override
	public void setDouble(Object object, double value) {
		ensureObject(object);
		internalUnsafe.putDoubleVolatile(object, offset, value);
	}
}

abstract class InternalUnsafeStaticFieldAccessor extends InternalUnsafeFieldAccessor {
	protected final Object base;

	protected InternalUnsafeStaticFieldAccessor(Field f) {
		super(f);

		if ((f.getModifiers() & Modifier.STATIC) != 0) base = f.getDeclaringClass();
		else throw new IllegalArgumentException("This field is not a static field: " + f);
	}
}

class InternalUnsafeStaticObjectFieldAccessor extends InternalUnsafeStaticFieldAccessor {
	public InternalUnsafeStaticObjectFieldAccessor(Field f) {
		super(f);
	}

	@Override
	public <T> T get(Object object) {
		return getObject(object);
	}

	@Override
	public void set(Object object, Object value) {
		setObject(object, value);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T getObject(Object object) {
		return (T) internalUnsafe.getReference(base, offset);
	}

	@Override
	public void setObject(Object object, Object value) {
		ensureValue(value);
		internalUnsafe.putReference(base, offset, value);
	}
}

class InternalUnsafeStaticBooleanFieldAccessor extends InternalUnsafeStaticFieldAccessor {
	public InternalUnsafeStaticBooleanFieldAccessor(Field f) {
		super(f);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T get(Object object) {
		return (T) Boolean.valueOf(getBoolean(object));
	}

	@Override
	public void set(Object object, Object value) {
		setBoolean(object, (boolean) value);
	}

	@Override
	public boolean getBoolean(Object object) {
		return internalUnsafe.getBoolean(base, offset);
	}

	@Override
	public void setBoolean(Object object, boolean value) {
		internalUnsafe.putBoolean(base, offset, value);
	}
}

class InternalUnsafeStaticByteFieldAccessor extends InternalUnsafeStaticFieldAccessor {
	public InternalUnsafeStaticByteFieldAccessor(Field f) {
		super(f);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T get(Object object) {
		return (T) Byte.valueOf(getByte(object));
	}

	@Override
	public void set(Object object, Object value) {
		setByte(object, ((Number) value).byteValue());
	}

	@Override
	public byte getByte(Object object) {
		return internalUnsafe.getByte(base, offset);
	}

	@Override
	public void setByte(Object object, byte value) {
		internalUnsafe.putByte(base, offset, value);
	}

	@Override
	public short getShort(Object object) {
		return getByte(object);
	}

	@Override
	public int getInt(Object object) {
		return getByte(object);
	}

	@Override
	public long getLong(Object object) {
		return getByte(object);
	}

	@Override
	public float getFloat(Object object) {
		return getByte(object);
	}

	@Override
	public double getDouble(Object object) {
		return getByte(object);
	}
}

class InternalUnsafeStaticCharFieldAccessor extends InternalUnsafeStaticFieldAccessor {
	public InternalUnsafeStaticCharFieldAccessor(Field f) {
		super(f);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T get(Object object) {
		return (T) Character.valueOf(getChar(object));
	}

	@Override
	public void set(Object object, Object value) {
		setChar(object, (char) value);
	}

	@Override
	public char getChar(Object object) {
		return internalUnsafe.getChar(base, offset);
	}

	@Override
	public void setChar(Object object, char value) {
		internalUnsafe.putChar(base, offset, value);
	}

	@Override
	public int getInt(Object object) {
		return getChar(object);
	}

	@Override
	public long getLong(Object object) {
		return getChar(object);
	}

	@Override
	public float getFloat(Object object) {
		return getChar(object);
	}

	@Override
	public double getDouble(Object object) {
		return getChar(object);
	}
}

class InternalUnsafeStaticShortFieldAccessor extends InternalUnsafeStaticFieldAccessor {
	public InternalUnsafeStaticShortFieldAccessor(Field f) {
		super(f);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T get(Object object) {
		return (T) Short.valueOf(getShort(object));
	}

	@Override
	public void set(Object object, Object value) {
		setShort(object, ((Number) value).shortValue());
	}

	@Override
	public void setByte(Object object, byte value) {
		setShort(object, value);
	}

	@Override
	public short getShort(Object object) {
		return internalUnsafe.getShort(base, offset);
	}

	@Override
	public void setShort(Object object, short value) {
		internalUnsafe.putShort(base, offset, value);
	}

	@Override
	public int getInt(Object object) {
		return getShort(object);
	}

	@Override
	public long getLong(Object object) {
		return getShort(object);
	}

	@Override
	public float getFloat(Object object) {
		return getShort(object);
	}

	@Override
	public double getDouble(Object object) {
		return getShort(object);
	}
}

class InternalUnsafeStaticIntFieldAccessor extends InternalUnsafeStaticFieldAccessor {
	public InternalUnsafeStaticIntFieldAccessor(Field f) {
		super(f);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T get(Object object) {
		return (T) Integer.valueOf(getInt(object));
	}

	@Override
	public void set(Object object, Object value) {
		setInt(object, ((Number) value).intValue());
	}

	@Override
	public void setByte(Object object, byte value) {
		setInt(object, value);
	}

	@Override
	public void setChar(Object object, char value) {
		setInt(object, value);
	}

	@Override
	public void setShort(Object object, short value) {
		setInt(object, value);
	}

	@Override
	public int getInt(Object object) {
		return internalUnsafe.getInt(base, offset);
	}

	@Override
	public void setInt(Object object, int value) {
		internalUnsafe.putInt(base, offset, value);
	}

	@Override
	public long getLong(Object object) {
		return getInt(object);
	}

	@Override
	public float getFloat(Object object) {
		return getInt(object);
	}

	@Override
	public double getDouble(Object object) {
		return getInt(object);
	}
}

class InternalUnsafeStaticLongFieldAccessor extends InternalUnsafeStaticFieldAccessor {
	public InternalUnsafeStaticLongFieldAccessor(Field f) {
		super(f);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T get(Object object) {
		return (T) Long.valueOf(getLong(object));
	}

	@Override
	public void set(Object object, Object value) {
		setLong(object, ((Number) value).longValue());
	}

	@Override
	public void setByte(Object object, byte value) {
		setLong(object, value);
	}

	@Override
	public void setChar(Object object, char value) {
		setLong(object, value);
	}

	@Override
	public void setShort(Object object, short value) {
		setLong(object, value);
	}

	@Override
	public void setInt(Object object, int value) {
		setLong(object, value);
	}

	@Override
	public long getLong(Object object) {
		return internalUnsafe.getLong(base, offset);
	}

	@Override
	public void setLong(Object object, long value) {
		internalUnsafe.putLong(base, offset, value);
	}

	@Override
	public float getFloat(Object object) {
		return getLong(object);
	}

	@Override
	public double getDouble(Object object) {
		return getLong(object);
	}
}

class InternalUnsafeStaticFloatFieldAccessor extends InternalUnsafeStaticFieldAccessor {
	public InternalUnsafeStaticFloatFieldAccessor(Field f) {
		super(f);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T get(Object object) {
		return (T) Float.valueOf(getFloat(object));
	}

	@Override
	public void set(Object object, Object value) {
		setFloat(object, ((Number) value).floatValue());
	}

	@Override
	public void setByte(Object object, byte value) {
		setFloat(object, value);
	}

	@Override
	public void setChar(Object object, char value) {
		setFloat(object, value);
	}

	@Override
	public void setShort(Object object, short value) {
		setFloat(object, value);
	}

	@Override
	public void setInt(Object object, int value) {
		setFloat(object, value);
	}

	@Override
	public void setLong(Object object, long value) {
		setFloat(object, value);
	}

	@Override
	public float getFloat(Object object) {
		return internalUnsafe.getFloat(base, offset);
	}

	@Override
	public void setFloat(Object object, float value) {
		internalUnsafe.putFloat(base, offset, value);
	}

	@Override
	public double getDouble(Object object) {
		return getFloat(object);
	}
}

class InternalUnsafeStaticDoubleFieldAccessor extends InternalUnsafeStaticFieldAccessor {
	public InternalUnsafeStaticDoubleFieldAccessor(Field f) {
		super(f);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T get(Object object) {
		return (T) Double.valueOf(getDouble(object));
	}

	@Override
	public void set(Object object, Object value) {
		setDouble(object, ((Number) value).doubleValue());
	}

	@Override
	public void setByte(Object object, byte value) {
		setDouble(object, value);
	}

	@Override
	public void setChar(Object object, char value) {
		setDouble(object, value);
	}

	@Override
	public void setShort(Object object, short value) {
		setDouble(object, value);
	}

	@Override
	public void setInt(Object object, int value) {
		setDouble(object, value);
	}

	@Override
	public void setLong(Object object, long value) {
		setDouble(object, value);
	}

	@Override
	public void setFloat(Object object, float value) {
		setDouble(object, value);
	}

	@Override
	public double getDouble(Object object) {
		return internalUnsafe.getDouble(base, offset);
	}

	@Override
	public void setDouble(Object object, double value) {
		internalUnsafe.putDouble(base, offset, value);
	}
}

class InternalUnsafeQualifiedStaticObjectFieldAccessor extends InternalUnsafeStaticObjectFieldAccessor {
	public InternalUnsafeQualifiedStaticObjectFieldAccessor(Field f) {
		super(f);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T getObject(Object object) {
		return (T) internalUnsafe.getReferenceVolatile(base, offset);
	}

	@Override
	public void setObject(Object object, Object value) {
		ensureValue(value);
		internalUnsafe.putReferenceVolatile(base, offset, value);
	}
}

class InternalUnsafeQualifiedStaticBooleanFieldAccessor extends InternalUnsafeStaticBooleanFieldAccessor {
	public InternalUnsafeQualifiedStaticBooleanFieldAccessor(Field f) {
		super(f);
	}

	@Override
	public boolean getBoolean(Object object) {
		return internalUnsafe.getBooleanVolatile(base, offset);
	}

	@Override
	public void setBoolean(Object object, boolean value) {
		internalUnsafe.putBooleanVolatile(base, offset, value);
	}
}

class InternalUnsafeQualifiedStaticByteFieldAccessor extends InternalUnsafeStaticByteFieldAccessor {
	public InternalUnsafeQualifiedStaticByteFieldAccessor(Field f) {
		super(f);
	}

	@Override
	public byte getByte(Object object) {
		return internalUnsafe.getByteVolatile(base, offset);
	}

	@Override
	public void setByte(Object object, byte value) {
		internalUnsafe.putByteVolatile(base, offset, value);
	}
}

class InternalUnsafeQualifiedStaticCharFieldAccessor extends InternalUnsafeStaticCharFieldAccessor {
	public InternalUnsafeQualifiedStaticCharFieldAccessor(Field f) {
		super(f);
	}

	@Override
	public short getShort(Object object) {
		return internalUnsafe.getShortVolatile(base, offset);
	}

	@Override
	public void setShort(Object object, short value) {
		internalUnsafe.putShortVolatile(base, offset, value);
	}
}

class InternalUnsafeQualifiedStaticShortFieldAccessor extends InternalUnsafeStaticShortFieldAccessor {
	public InternalUnsafeQualifiedStaticShortFieldAccessor(Field f) {
		super(f);
	}

	@Override
	public short getShort(Object object) {
		return internalUnsafe.getShortVolatile(base, offset);
	}

	@Override
	public void setShort(Object object, short value) {
		internalUnsafe.putShortVolatile(base, offset, value);
	}
}

class InternalUnsafeQualifiedStaticIntFieldAccessor extends InternalUnsafeStaticShortFieldAccessor {
	public InternalUnsafeQualifiedStaticIntFieldAccessor(Field f) {
		super(f);
	}

	@Override
	public int getInt(Object object) {
		return internalUnsafe.getIntVolatile(base, offset);
	}

	@Override
	public void setInt(Object object, int value) {
		internalUnsafe.putIntVolatile(base, offset, value);
	}
}

class InternalUnsafeQualifiedStaticLongFieldAccessor extends InternalUnsafeStaticLongFieldAccessor {
	public InternalUnsafeQualifiedStaticLongFieldAccessor(Field f) {
		super(f);
	}

	@Override
	public long getLong(Object object) {
		return internalUnsafe.getLongVolatile(base, offset);
	}

	@Override
	public void setLong(Object object, long value) {
		internalUnsafe.putLongVolatile(base, offset, value);
	}
}

class InternalUnsafeQualifiedStaticFloatFieldAccessor extends InternalUnsafeStaticFloatFieldAccessor {
	public InternalUnsafeQualifiedStaticFloatFieldAccessor(Field f) {
		super(f);
	}

	@Override
	public float getFloat(Object object) {
		return internalUnsafe.getFloatVolatile(base, offset);
	}

	@Override
	public void setFloat(Object object, float value) {
		internalUnsafe.putFloatVolatile(base, offset, value);
	}
}

class InternalUnsafeQualifiedStaticDoubleFieldAccessor extends InternalUnsafeStaticDoubleFieldAccessor {
	public InternalUnsafeQualifiedStaticDoubleFieldAccessor(Field f) {
		super(f);
	}

	@Override
	public double getDouble(Object object) {
		return internalUnsafe.getDoubleVolatile(base, offset);
	}

	@Override
	public void setDouble(Object object, double value) {
		internalUnsafe.putDoubleVolatile(base, offset, value);
	}
}