package endfield.desktop;

import sun.misc.Unsafe;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import static endfield.util.GetKt.sneakyThrow;

@SuppressWarnings("removal")
public final class LegacyUnsafer {
	static final Unsafe legacyUnsafe;

	static {
		try {
			Field field = Unsafe.class.getDeclaredField("theUnsafe");
			field.setAccessible(true);
			legacyUnsafe = (Unsafe) field.get(null);
		} catch (NoSuchFieldException | IllegalAccessException e) {
			throw sneakyThrow(e);
		}
	}

	private LegacyUnsafer() {}

	public static void setByte(Field field, Object object, byte value) {
		long offset = legacyUnsafe.objectFieldOffset(field);

		if ((field.getModifiers() & Modifier.VOLATILE) != 0)
			legacyUnsafe.putByteVolatile(object, offset, value);
		else
			legacyUnsafe.putByte(object, offset, value);
	}

	public static void setByteStatic(Field field, byte value) {
		long offset = legacyUnsafe.staticFieldOffset(field);
		Object base = legacyUnsafe.staticFieldBase(field);

		if ((field.getModifiers() & Modifier.VOLATILE) != 0)
			legacyUnsafe.putByteVolatile(base, offset, value);
		else
			legacyUnsafe.putByte(base, offset, value);
	}

	public static byte getByte(Field field, Object object) {
		long offset = legacyUnsafe.objectFieldOffset(field);

		return (field.getModifiers() & Modifier.VOLATILE) != 0 ?
				legacyUnsafe.getByteVolatile(object, offset) :
				legacyUnsafe.getByte(object, offset);
	}

	public static byte getByteStatic(Field field) {
		long offset = legacyUnsafe.staticFieldOffset(field);
		Object base = legacyUnsafe.staticFieldBase(field);

		return (field.getModifiers() & Modifier.VOLATILE) != 0 ?
				legacyUnsafe.getByteVolatile(base, offset) :
				legacyUnsafe.getByte(base, offset);
	}

	public static void setShort(Field field, Object object, short value) {
		long offset = legacyUnsafe.objectFieldOffset(field);

		if ((field.getModifiers() & Modifier.VOLATILE) != 0)
			legacyUnsafe.putShortVolatile(object, offset, value);
		else
			legacyUnsafe.putShort(object, offset, value);
	}

	public static void setShortStatic(Field field, short value) {
		long offset = legacyUnsafe.staticFieldOffset(field);
		Object base = legacyUnsafe.staticFieldBase(field);

		if ((field.getModifiers() & Modifier.VOLATILE) != 0)
			legacyUnsafe.putShortVolatile(base, offset, value);
		else
			legacyUnsafe.putShort(base, offset, value);
	}

	public static short getShort(Field field, Object object) {
		long offset = legacyUnsafe.objectFieldOffset(field);

		return (field.getModifiers() & Modifier.VOLATILE) != 0 ?
				legacyUnsafe.getShortVolatile(object, offset) :
				legacyUnsafe.getShort(object, offset);
	}

	public static short getShortStatic(Field field) {
		long offset = legacyUnsafe.staticFieldOffset(field);
		Object base = legacyUnsafe.staticFieldBase(field);

		return (field.getModifiers() & Modifier.VOLATILE) != 0 ?
				legacyUnsafe.getShortVolatile(base, offset) :
				legacyUnsafe.getShort(base, offset);
	}

	public static void setInt(Field field, Object object, int value) {
		long offset = legacyUnsafe.objectFieldOffset(field);

		if ((field.getModifiers() & Modifier.VOLATILE) != 0)
			legacyUnsafe.putIntVolatile(object, offset, value);
		else
			legacyUnsafe.putInt(object, offset, value);
	}

	public static void setIntStatic(Field field, int value) {
		long offset = legacyUnsafe.staticFieldOffset(field);
		Object base = legacyUnsafe.staticFieldBase(field);

		if ((field.getModifiers() & Modifier.VOLATILE) != 0)
			legacyUnsafe.putIntVolatile(base, offset, value);
		else
			legacyUnsafe.putInt(base, offset, value);
	}

	public static int getInt(Field field, Object object) {
		long offset = legacyUnsafe.objectFieldOffset(field);

		return (field.getModifiers() & Modifier.VOLATILE) != 0 ?
				legacyUnsafe.getIntVolatile(object, offset) :
				legacyUnsafe.getInt(object, offset);
	}

	public static int getIntStatic(Field field) {
		long offset = legacyUnsafe.staticFieldOffset(field);
		Object base = legacyUnsafe.staticFieldBase(field);

		return (field.getModifiers() & Modifier.VOLATILE) != 0 ?
				legacyUnsafe.getIntVolatile(base, offset) :
				legacyUnsafe.getInt(base, offset);
	}

	public static void setLong(Field field, Object object, long value) {
		long offset = legacyUnsafe.objectFieldOffset(field);

		if ((field.getModifiers() & Modifier.VOLATILE) != 0)
			legacyUnsafe.putLongVolatile(object, offset, value);
		else
			legacyUnsafe.putLong(object, offset, value);
	}

	public static void setLongStatic(Field field, long value) {
		long offset = legacyUnsafe.staticFieldOffset(field);
		Object base = legacyUnsafe.staticFieldBase(field);

		if ((field.getModifiers() & Modifier.VOLATILE) != 0)
			legacyUnsafe.putLongVolatile(base, offset, value);
		else
			legacyUnsafe.putLong(base, offset, value);
	}

	public static long getLong(Field field, Object object) {
		long offset = legacyUnsafe.objectFieldOffset(field);

		return (field.getModifiers() & Modifier.VOLATILE) != 0 ?
				legacyUnsafe.getLongVolatile(object, offset) :
				legacyUnsafe.getLong(object, offset);
	}

	public static long getLongStatic(Field field) {
		long offset = legacyUnsafe.staticFieldOffset(field);
		Object base = legacyUnsafe.staticFieldBase(field);

		return (field.getModifiers() & Modifier.VOLATILE) != 0 ?
				legacyUnsafe.getLongVolatile(base, offset) :
				legacyUnsafe.getLong(base, offset);
	}

	public static void setFloat(Field field, Object object, float value) {
		long offset = legacyUnsafe.objectFieldOffset(field);

		if ((field.getModifiers() & Modifier.VOLATILE) != 0)
			legacyUnsafe.putFloatVolatile(object, offset, value);
		else
			legacyUnsafe.putFloat(object, offset, value);
	}

	public static void setFloatStatic(Field field, float value) {
		long offset = legacyUnsafe.staticFieldOffset(field);
		Object base = legacyUnsafe.staticFieldBase(field);

		if ((field.getModifiers() & Modifier.VOLATILE) != 0) {
			legacyUnsafe.putFloatVolatile(base, offset, value);
		} else legacyUnsafe.putFloat(base, offset, value);
	}

	public static float getFloat(Field field, Object object) {
		long offset = legacyUnsafe.objectFieldOffset(field);

		return (field.getModifiers() & Modifier.VOLATILE) != 0 ?
				legacyUnsafe.getFloatVolatile(object, offset) :
				legacyUnsafe.getFloat(object, offset);
	}

	public static float getFloatStatic(Field field) {
		long offset = legacyUnsafe.staticFieldOffset(field);
		Object base = legacyUnsafe.staticFieldBase(field);

		return (field.getModifiers() & Modifier.VOLATILE) != 0 ?
				legacyUnsafe.getFloatVolatile(base, offset) :
				legacyUnsafe.getFloat(base, offset);
	}

	public static void setDouble(Field field, Object object, double value) {
		long offset = legacyUnsafe.objectFieldOffset(field);

		if ((field.getModifiers() & Modifier.VOLATILE) != 0)
			legacyUnsafe.putDoubleVolatile(object, offset, value);
		else
			legacyUnsafe.putDouble(object, offset, value);
	}

	public static void setDoubleStatic(Field field, double value) {
		long offset = legacyUnsafe.staticFieldOffset(field);
		Object base = legacyUnsafe.staticFieldBase(field);

		if ((field.getModifiers() & Modifier.VOLATILE) != 0)
			legacyUnsafe.putDoubleVolatile(base, offset, value);
		else
			legacyUnsafe.putDouble(base, offset, value);
	}

	public static double getDouble(Field field, Object object) {
		long offset = legacyUnsafe.objectFieldOffset(field);

		return (field.getModifiers() & Modifier.VOLATILE) != 0 ?
				legacyUnsafe.getDoubleVolatile(object, offset) :
				legacyUnsafe.getDouble(object, offset);
	}

	public static double getDoubleStatic(Field field) {
		long offset = legacyUnsafe.staticFieldOffset(field);
		Object base = legacyUnsafe.staticFieldBase(field);

		return (field.getModifiers() & Modifier.VOLATILE) != 0 ?
				legacyUnsafe.getDoubleVolatile(base, offset) :
				legacyUnsafe.getDouble(base, offset);
	}

	public static void setChar(Field field, Object object, char value) {
		long offset = legacyUnsafe.objectFieldOffset(field);

		if ((field.getModifiers() & Modifier.VOLATILE) != 0)
			legacyUnsafe.putCharVolatile(object, offset, value);
		else
			legacyUnsafe.putChar(object, offset, value);
	}

	public static void setCharStatic(Field field, char value) {
		long offset = legacyUnsafe.staticFieldOffset(field);
		Object base = legacyUnsafe.staticFieldBase(field);

		if ((field.getModifiers() & Modifier.VOLATILE) != 0)
			legacyUnsafe.putCharVolatile(base, offset, value);
		else
			legacyUnsafe.putChar(base, offset, value);
	}

	public static char getChar(Field field, Object object) {
		long offset = legacyUnsafe.objectFieldOffset(field);

		return (field.getModifiers() & Modifier.VOLATILE) != 0 ?
				legacyUnsafe.getCharVolatile(object, offset) :
				legacyUnsafe.getChar(object, offset);
	}

	public static char getCharStatic(Field field) {
		long offset = legacyUnsafe.staticFieldOffset(field);
		Object base = legacyUnsafe.staticFieldBase(field);

		return (field.getModifiers() & Modifier.VOLATILE) != 0 ?
				legacyUnsafe.getCharVolatile(base, offset) :
				legacyUnsafe.getChar(base, offset);
	}

	public static void setBoolean(Field field, Object object, boolean value) {
		long offset = legacyUnsafe.objectFieldOffset(field);

		if ((field.getModifiers() & Modifier.VOLATILE) != 0)
			legacyUnsafe.putBooleanVolatile(object, offset, value);
		else
			legacyUnsafe.putBoolean(object, offset, value);
	}

	public static void setBooleanStatic(Field field, boolean value) {
		long offset = legacyUnsafe.staticFieldOffset(field);
		Object base = legacyUnsafe.staticFieldBase(field);

		if ((field.getModifiers() & Modifier.VOLATILE) != 0)
			legacyUnsafe.putBooleanVolatile(base, offset, value);
		else
			legacyUnsafe.putBoolean(base, offset, value);
	}

	public static boolean getBoolean(Field field, Object object) {
		long offset = legacyUnsafe.objectFieldOffset(field);

		return (field.getModifiers() & Modifier.VOLATILE) != 0 ?
				legacyUnsafe.getBooleanVolatile(object, offset) :
				legacyUnsafe.getBoolean(object, offset);
	}

	public static boolean getBooleanStatic(Field field) {
		long offset = legacyUnsafe.staticFieldOffset(field);
		Object base = legacyUnsafe.staticFieldBase(field);

		return (field.getModifiers() & Modifier.VOLATILE) != 0 ?
				legacyUnsafe.getBooleanVolatile(base, offset) :
				legacyUnsafe.getBoolean(base, offset);
	}

	public static void setObject(Field field, Object object, Object value) {
		long offset = legacyUnsafe.objectFieldOffset(field);

		if ((field.getModifiers() & Modifier.VOLATILE) != 0)
			legacyUnsafe.putObjectVolatile(object, offset, value);
		else
			legacyUnsafe.putObject(object, offset, value);
	}

	public static void setObjectStatic(Field field, Object value) {
		long offset = legacyUnsafe.staticFieldOffset(field);
		Object base = legacyUnsafe.staticFieldBase(field);

		if ((field.getModifiers() & Modifier.VOLATILE) != 0)
			legacyUnsafe.putObjectVolatile(base, offset, value);
		else
			legacyUnsafe.putObject(base, offset, value);
	}

	public static Object getObject(Field field, Object object) {
		long offset = legacyUnsafe.objectFieldOffset(field);

		return (field.getModifiers() & Modifier.VOLATILE) != 0 ?
				legacyUnsafe.getObjectVolatile(object, offset) :
				legacyUnsafe.getObject(object, offset);
	}

	public static Object getObjectStatic(Field field) {
		long offset = legacyUnsafe.staticFieldOffset(field);
		Object base = legacyUnsafe.staticFieldBase(field);

		return (field.getModifiers() & Modifier.VOLATILE) != 0 ?
				legacyUnsafe.getObjectVolatile(base, offset) :
				legacyUnsafe.getObject(base, offset);
	}

	public static void set(Field field, Object object, Object value) {
		long offset = legacyUnsafe.objectFieldOffset(field);
		Class<?> clazz = field.getType();
		if ((field.getModifiers() & Modifier.VOLATILE) != 0)
			putVolatile(value, object, offset, clazz);
		else
			put(value, object, offset, clazz);
	}

	public static void setStatic(Field field, Object value) {
		Object base = legacyUnsafe.staticFieldBase(field);
		long offset = legacyUnsafe.staticFieldOffset(field);
		Class<?> clazz = field.getType();
		if ((field.getModifiers() & Modifier.VOLATILE) != 0)
			putVolatile(value, base, offset, clazz);
		else
			put(value, base, offset, clazz);
	}

	public static Object get(Field field, Object object) {
		long offset = legacyUnsafe.objectFieldOffset(field);
		Class<?> clazz = field.getType();

		return (field.getModifiers() & Modifier.VOLATILE) != 0 ?
				getVolatile(object, offset, clazz) :
				get(object, offset, clazz);
	}

	public static Object getStatic(Field field) {
		Object base = legacyUnsafe.staticFieldBase(field);
		long offset = legacyUnsafe.staticFieldOffset(field);
		Class<?> clazz = field.getType();

		return (field.getModifiers() & Modifier.VOLATILE) != 0 ?
				getVolatile(base, offset, clazz) :
				get(base, offset, clazz);
	}

	static void put(Object value, Object object, long offset, Class<?> type) {
		if (type.isPrimitive()) {
			if (type == int.class) legacyUnsafe.putInt(object, offset, ((Number) value).intValue());
			else if (type == float.class) legacyUnsafe.putFloat(object, offset, ((Number) value).floatValue());
			else if (type == boolean.class) legacyUnsafe.putBoolean(object, offset, (boolean) value);
			else if (type == byte.class) legacyUnsafe.putByte(object, offset, ((Number) value).byteValue());
			else if (type == double.class) legacyUnsafe.putDouble(object, offset, ((Number) value).doubleValue());
			else if (type == long.class) legacyUnsafe.putLong(object, offset, ((Number) value).longValue());
			else if (type == char.class) legacyUnsafe.putChar(object, offset, (char) value);
			else if (type == short.class) legacyUnsafe.putShort(object, offset, ((Number) value).shortValue());
			else throw new IllegalArgumentException("unknown type of field " + type);
		} else {
			legacyUnsafe.putObject(object, offset, value);
		}
	}

	static void putVolatile(Object value, Object object, long offset, Class<?> type) {
		if (type.isPrimitive()) {
			if (type == int.class) legacyUnsafe.putIntVolatile(object, offset, ((Number) value).intValue());
			else if (type == float.class) legacyUnsafe.putFloatVolatile(object, offset, ((Number) value).floatValue());
			else if (type == boolean.class) legacyUnsafe.putBooleanVolatile(object, offset, (boolean) value);
			else if (type == byte.class) legacyUnsafe.putByteVolatile(object, offset, ((Number) value).byteValue());
			else if (type == long.class) legacyUnsafe.putLongVolatile(object, offset, ((Number) value).longValue());
			else if (type == double.class) legacyUnsafe.putDoubleVolatile(object, offset, ((Number) value).doubleValue());
			else if (type == char.class) legacyUnsafe.putCharVolatile(object, offset, (char) value);
			else if (type == short.class) legacyUnsafe.putShortVolatile(object, offset, ((Number) value).shortValue());
			else throw new IllegalArgumentException("unknown type of field " + type);
		} else {
			legacyUnsafe.putObjectVolatile(object, offset, value);
		}
	}

	static Object get(Object object, long offset, Class<?> type) {
		if (type.isPrimitive()) {
			if (type == int.class) return legacyUnsafe.getInt(object, offset);
			else if (type == float.class) return legacyUnsafe.getFloat(object, offset);
			else if (type == boolean.class) return legacyUnsafe.getBoolean(object, offset);
			else if (type == byte.class) return legacyUnsafe.getByte(object, offset);
			else if (type == long.class) return legacyUnsafe.getDouble(object, offset);
			else if (type == double.class) return legacyUnsafe.getLong(object, offset);
			else if (type == char.class) return legacyUnsafe.getChar(object, offset);
			else if (type == short.class) return legacyUnsafe.getShort(object, offset);
			else throw new IllegalArgumentException("unknown type of field " + type);
		} else {
			return legacyUnsafe.getObject(object, offset);
		}
	}

	static Object getVolatile(Object object, long offset, Class<?> type) {
		if (type.isPrimitive()) {
			if (type == int.class) return legacyUnsafe.getIntVolatile(object, offset);
			else if (type == float.class) return legacyUnsafe.getFloatVolatile(object, offset);
			else if (type == boolean.class) return legacyUnsafe.getBooleanVolatile(object, offset);
			else if (type == byte.class) return legacyUnsafe.getByteVolatile(object, offset);
			else if (type == long.class) return legacyUnsafe.getLongVolatile(object, offset);
			else if (type == double.class) return legacyUnsafe.getDoubleVolatile(object, offset);
			else if (type == char.class) return legacyUnsafe.getCharVolatile(object, offset);
			else if (type == short.class) return legacyUnsafe.getShortVolatile(object, offset);
			else throw new IllegalArgumentException("unknown type of field " + type);
		} else {
			return legacyUnsafe.getObjectVolatile(object, offset);
		}
	}
}
