package endfield.android;

import endfield.android.util.Fields;
import endfield.util.AbstractFieldAccessor;
import endfield.util.FieldAccessor;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import static endfield.android.Unsafer2.internalUnsafe;
import static endfield.util.Reflects.getGetMessage;
import static endfield.util.Reflects.getSetMessage;

public class UnsafeFieldAccessor2 extends AbstractFieldAccessor {
	protected final long offset;

	protected UnsafeFieldAccessor2(Field field) {
		super(field);
		offset = Fields.getOffset(field);
	}

	public static FieldAccessor getUnsafeFieldAccessor(Field field) {
		Class<?> type = field.getType();
		int modifiers = field.getModifiers();

		if ((modifiers & Modifier.STATIC) != 0) {
			if ((modifiers & Modifier.VOLATILE) != 0) {
				if (type.isPrimitive()) {
					if (type == boolean.class) return new UnsafeQualifiedStaticBooleanFieldAccessor2(field);
					else if (type == byte.class) return new UnsafeQualifiedStaticByteFieldAccessor2(field);
					else if (type == char.class) return new UnsafeQualifiedStaticCharFieldAccessor2(field);
					else if (type == short.class) return new UnsafeQualifiedStaticShortFieldAccessor2(field);
					else if (type == int.class) return new UnsafeQualifiedStaticIntFieldAccessor2(field);
					else if (type == long.class) return new UnsafeQualifiedStaticLongFieldAccessor2(field);
					else if (type == float.class) return new UnsafeQualifiedStaticFloatFieldAccessor2(field);
					else if (type == double.class) return new UnsafeQualifiedStaticDoubleFieldAccessor2(field);
					else throw new IllegalArgumentException("unknown type of field " + field);
				} else return new UnsafeQualifiedStaticObjectFieldAccessor2(field);
			} else {
				if (type.isPrimitive()) {
					if (type == boolean.class) return new UnsafeStaticBooleanFieldAccessor2(field);
					else if (type == byte.class) return new UnsafeStaticByteFieldAccessor2(field);
					else if (type == char.class) return new UnsafeStaticCharFieldAccessor2(field);
					else if (type == short.class) return new UnsafeStaticShortFieldAccessor2(field);
					else if (type == int.class) return new UnsafeStaticIntFieldAccessor2(field);
					else if (type == long.class) return new UnsafeStaticLongFieldAccessor2(field);
					else if (type == float.class) return new UnsafeStaticFloatFieldAccessor2(field);
					else if (type == double.class) return new UnsafeStaticDoubleFieldAccessor2(field);
					else throw new IllegalArgumentException("unknown type of field " + field);
				} else return new UnsafeStaticObjectFieldAccessor2(field);
			}
		} else {
			if ((modifiers & Modifier.VOLATILE) != 0) {
				if (type.isPrimitive()) {
					if (type == boolean.class) return new UnsafeQualifiedBooleanFieldAccessor2(field);
					else if (type == byte.class) return new UnsafeQualifiedByteFieldAccessor2(field);
					else if (type == char.class) return new UnsafeQualifiedCharFieldAccessor2(field);
					else if (type == short.class) return new UnsafeQualifiedShortFieldAccessor2(field);
					else if (type == int.class) return new UnsafeQualifiedIntFieldAccessor2(field);
					else if (type == long.class) return new UnsafeQualifiedLongFieldAccessor2(field);
					else if (type == float.class) return new UnsafeQualifiedFloatFieldAccessor2(field);
					else if (type == double.class) return new UnsafeQualifiedDoubleFieldAccessor2(field);
					else throw new IllegalArgumentException("unknown type of field " + field);
				} else return new UnsafeQualifiedObjectFieldAccessor2(field);
			} else {
				if (type.isPrimitive()) {
					if (type == boolean.class) return new UnsafeBooleanFieldAccessor2(field);
					else if (type == byte.class) return new UnsafeByteFieldAccessor2(field);
					else if (type == char.class) return new UnsafeCharFieldAccessor2(field);
					else if (type == short.class) return new UnsafeShortFieldAccessor2(field);
					else if (type == int.class) return new UnsafeIntFieldAccessor2(field);
					else if (type == long.class) return new UnsafeLongFieldAccessor2(field);
					else if (type == float.class) return new UnsafeFloatFieldAccessor2(field);
					else if (type == double.class) return new UnsafeDoubleFieldAccessor2(field);
					else throw new IllegalArgumentException("unknown type of field " + field);
				} else return new UnsafeObjectFieldAccessor2(field);
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

class UnsafeObjectFieldAccessor2 extends UnsafeFieldAccessor2 {
	public UnsafeObjectFieldAccessor2(Field f) {
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

class UnsafeBooleanFieldAccessor2 extends UnsafeFieldAccessor2 {
	public UnsafeBooleanFieldAccessor2(Field f) {
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

class UnsafeByteFieldAccessor2 extends UnsafeFieldAccessor2 {
	public UnsafeByteFieldAccessor2(Field f) {
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

class UnsafeCharFieldAccessor2 extends UnsafeFieldAccessor2 {
	public UnsafeCharFieldAccessor2(Field f) {
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

class UnsafeShortFieldAccessor2 extends UnsafeFieldAccessor2 {
	public UnsafeShortFieldAccessor2(Field f) {
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

class UnsafeIntFieldAccessor2 extends UnsafeFieldAccessor2 {
	public UnsafeIntFieldAccessor2(Field f) {
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

class UnsafeLongFieldAccessor2 extends UnsafeFieldAccessor2 {
	public UnsafeLongFieldAccessor2(Field f) {
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

class UnsafeFloatFieldAccessor2 extends UnsafeFieldAccessor2 {
	public UnsafeFloatFieldAccessor2(Field f) {
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

class UnsafeDoubleFieldAccessor2 extends UnsafeFieldAccessor2 {
	public UnsafeDoubleFieldAccessor2(Field f) {
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

class UnsafeQualifiedObjectFieldAccessor2 extends UnsafeObjectFieldAccessor2 {
	public UnsafeQualifiedObjectFieldAccessor2(Field f) {
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

class UnsafeQualifiedBooleanFieldAccessor2 extends UnsafeBooleanFieldAccessor2 {
	public UnsafeQualifiedBooleanFieldAccessor2(Field f) {
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

class UnsafeQualifiedByteFieldAccessor2 extends UnsafeByteFieldAccessor2 {
	public UnsafeQualifiedByteFieldAccessor2(Field f) {
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

class UnsafeQualifiedCharFieldAccessor2 extends UnsafeCharFieldAccessor2 {
	public UnsafeQualifiedCharFieldAccessor2(Field f) {
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

class UnsafeQualifiedShortFieldAccessor2 extends UnsafeShortFieldAccessor2 {
	public UnsafeQualifiedShortFieldAccessor2(Field f) {
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

class UnsafeQualifiedIntFieldAccessor2 extends UnsafeIntFieldAccessor2 {
	public UnsafeQualifiedIntFieldAccessor2(Field f) {
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

class UnsafeQualifiedLongFieldAccessor2 extends UnsafeLongFieldAccessor2 {
	public UnsafeQualifiedLongFieldAccessor2(Field f) {
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

class UnsafeQualifiedFloatFieldAccessor2 extends UnsafeFloatFieldAccessor2 {
	public UnsafeQualifiedFloatFieldAccessor2(Field f) {
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

class UnsafeQualifiedDoubleFieldAccessor2 extends UnsafeDoubleFieldAccessor2 {
	public UnsafeQualifiedDoubleFieldAccessor2(Field f) {
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

abstract class UnsafeStaticFieldAccessor2 extends UnsafeFieldAccessor2 {
	protected final Object base;

	protected UnsafeStaticFieldAccessor2(Field f) {
		super(f);

		if ((f.getModifiers() & Modifier.STATIC) != 0) base = f.getDeclaringClass();
		else throw new IllegalArgumentException("This field is not a static field: " + f);
	}
}

class UnsafeStaticObjectFieldAccessor2 extends UnsafeStaticFieldAccessor2 {
	public UnsafeStaticObjectFieldAccessor2(Field f) {
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

class UnsafeStaticBooleanFieldAccessor2 extends UnsafeStaticFieldAccessor2 {
	public UnsafeStaticBooleanFieldAccessor2(Field f) {
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

class UnsafeStaticByteFieldAccessor2 extends UnsafeStaticFieldAccessor2 {
	public UnsafeStaticByteFieldAccessor2(Field f) {
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

class UnsafeStaticCharFieldAccessor2 extends UnsafeStaticFieldAccessor2 {
	public UnsafeStaticCharFieldAccessor2(Field f) {
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

class UnsafeStaticShortFieldAccessor2 extends UnsafeStaticFieldAccessor2 {
	public UnsafeStaticShortFieldAccessor2(Field f) {
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

class UnsafeStaticIntFieldAccessor2 extends UnsafeStaticFieldAccessor2 {
	public UnsafeStaticIntFieldAccessor2(Field f) {
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

class UnsafeStaticLongFieldAccessor2 extends UnsafeStaticFieldAccessor2 {
	public UnsafeStaticLongFieldAccessor2(Field f) {
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

class UnsafeStaticFloatFieldAccessor2 extends UnsafeStaticFieldAccessor2 {
	public UnsafeStaticFloatFieldAccessor2(Field f) {
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

class UnsafeStaticDoubleFieldAccessor2 extends UnsafeStaticFieldAccessor2 {
	public UnsafeStaticDoubleFieldAccessor2(Field f) {
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

class UnsafeQualifiedStaticObjectFieldAccessor2 extends UnsafeStaticObjectFieldAccessor2 {
	public UnsafeQualifiedStaticObjectFieldAccessor2(Field f) {
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

class UnsafeQualifiedStaticBooleanFieldAccessor2 extends UnsafeStaticBooleanFieldAccessor2 {
	public UnsafeQualifiedStaticBooleanFieldAccessor2(Field f) {
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

class UnsafeQualifiedStaticByteFieldAccessor2 extends UnsafeStaticByteFieldAccessor2 {
	public UnsafeQualifiedStaticByteFieldAccessor2(Field f) {
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

class UnsafeQualifiedStaticCharFieldAccessor2 extends UnsafeStaticCharFieldAccessor2 {
	public UnsafeQualifiedStaticCharFieldAccessor2(Field f) {
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

class UnsafeQualifiedStaticShortFieldAccessor2 extends UnsafeStaticShortFieldAccessor2 {
	public UnsafeQualifiedStaticShortFieldAccessor2(Field f) {
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

class UnsafeQualifiedStaticIntFieldAccessor2 extends UnsafeStaticShortFieldAccessor2 {
	public UnsafeQualifiedStaticIntFieldAccessor2(Field f) {
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

class UnsafeQualifiedStaticLongFieldAccessor2 extends UnsafeStaticLongFieldAccessor2 {
	public UnsafeQualifiedStaticLongFieldAccessor2(Field f) {
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

class UnsafeQualifiedStaticFloatFieldAccessor2 extends UnsafeStaticFloatFieldAccessor2 {
	public UnsafeQualifiedStaticFloatFieldAccessor2(Field f) {
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

class UnsafeQualifiedStaticDoubleFieldAccessor2 extends UnsafeStaticDoubleFieldAccessor2 {
	public UnsafeQualifiedStaticDoubleFieldAccessor2(Field f) {
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