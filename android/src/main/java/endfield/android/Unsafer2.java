package endfield.android;

import android.annotation.TargetApi;
import android.os.Build.VERSION_CODES;
import endfield.android.util.Fields;
import jdk.internal.misc.Unsafe;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import static endfield.util.GetKt.sneakyThrow;

// Sdk_version>=33
@TargetApi(VERSION_CODES.TIRAMISU)
public final class Unsafer2 {
	static final Unsafe internalUnsafe;

	static {
		try {
			Field field = null;

			try {
				// Sdk_version>=36.1
				field = sun.misc.Unsafe.class.getDeclaredField("theInternalUnsafe");
			} catch (NoSuchFieldException ignored) {
			}

			if (field == null) field = Unsafe.class.getDeclaredField("theUnsafe");

			field.setAccessible(true);
			internalUnsafe = (Unsafe) field.get(null);
		} catch (NoSuchFieldException | IllegalAccessException e) {
			throw sneakyThrow(e);
		}
	}

	public static void setByte(Field field, Object object, byte value) {
		int offset = Fields.getOffset(field);

		if ((field.getModifiers() & Modifier.VOLATILE) != 0)
			internalUnsafe.putByteVolatile(object, offset, value);
		else
			internalUnsafe.putByte(object, offset, value);
	}

	public static void setByteStatic(Field field, byte value) {
		int offset = Fields.getOffset(field);

		if ((field.getModifiers() & Modifier.VOLATILE) != 0)
			internalUnsafe.putByteVolatile(field.getDeclaringClass(), offset, value);
		else
			internalUnsafe.putByte(field.getDeclaringClass(), offset, value);
	}

	public static byte getByte(Field field, Object object) {
		int offset = Fields.getOffset(field);

		return (field.getModifiers() & Modifier.VOLATILE) != 0 ?
				internalUnsafe.getByteVolatile(object, offset) :
				internalUnsafe.getByte(object, offset);
	}

	public static byte getByteStatic(Field field) {
		int offset = Fields.getOffset(field);

		return (field.getModifiers() & Modifier.VOLATILE) != 0 ?
				internalUnsafe.getByteVolatile(field.getDeclaringClass(), offset) :
				internalUnsafe.getByte(field.getDeclaringClass(), offset);
	}

	public static void setShort(Field field, Object object, short value) {
		int offset = Fields.getOffset(field);

		if ((field.getModifiers() & Modifier.VOLATILE) != 0)
			internalUnsafe.putShortVolatile(object, offset, value);
		else
			internalUnsafe.putShort(object, offset, value);
	}

	public static void setShortStatic(Field field, short value) {
		int offset = Fields.getOffset(field);

		if ((field.getModifiers() & Modifier.VOLATILE) != 0)
			internalUnsafe.putShortVolatile(field.getDeclaringClass(), offset, value);
		else
			internalUnsafe.putShort(field.getDeclaringClass(), offset, value);
	}

	public static short getShort(Field field, Object object) {
		int offset = Fields.getOffset(field);

		return (field.getModifiers() & Modifier.VOLATILE) != 0 ?
				internalUnsafe.getShortVolatile(object, offset) :
				internalUnsafe.getShort(object, offset);
	}

	public static short getShortStatic(Field field) {
		int offset = Fields.getOffset(field);

		return (field.getModifiers() & Modifier.VOLATILE) != 0 ?
				internalUnsafe.getShortVolatile(field.getDeclaringClass(), offset) :
				internalUnsafe.getShort(field.getDeclaringClass(), offset);
	}

	public static void setInt(Field field, Object object, int value) {
		int offset = Fields.getOffset(field);

		if ((field.getModifiers() & Modifier.VOLATILE) != 0)
			internalUnsafe.putIntVolatile(object, offset, value);
		else
			internalUnsafe.putInt(object, offset, value);
	}

	public static void setIntStatic(Field field, int value) {
		int offset = Fields.getOffset(field);

		if ((field.getModifiers() & Modifier.VOLATILE) != 0)
			internalUnsafe.putIntVolatile(field.getDeclaringClass(), offset, value);
		else
			internalUnsafe.putInt(field.getDeclaringClass(), offset, value);
	}

	public static int getInt(Field field, Object object) {
		int offset = Fields.getOffset(field);

		return (field.getModifiers() & Modifier.VOLATILE) != 0 ?
				internalUnsafe.getIntVolatile(object, offset) :
				internalUnsafe.getInt(object, offset);
	}

	public static int getIntStatic(Field field) {
		int offset = Fields.getOffset(field);

		return (field.getModifiers() & Modifier.VOLATILE) != 0 ?
				internalUnsafe.getIntVolatile(field.getDeclaringClass(), offset) :
				internalUnsafe.getInt(field.getDeclaringClass(), offset);
	}

	public static void setLong(Field field, Object object, long value) {
		int offset = Fields.getOffset(field);

		if ((field.getModifiers() & Modifier.VOLATILE) != 0)
			internalUnsafe.putLongVolatile(object, offset, value);
		else
			internalUnsafe.putLong(object, offset, value);
	}

	public static void setLongStatic(Field field, long value) {
		int offset = Fields.getOffset(field);

		if ((field.getModifiers() & Modifier.VOLATILE) != 0)
			internalUnsafe.putLongVolatile(field.getDeclaringClass(), offset, value);
		else
			internalUnsafe.putLong(field.getDeclaringClass(), offset, value);
	}

	public static long getLong(Field field, Object object) {
		int offset = Fields.getOffset(field);

		return (field.getModifiers() & Modifier.VOLATILE) != 0 ?
				internalUnsafe.getLongVolatile(object, offset) :
				internalUnsafe.getLong(object, offset);
	}

	public static long getLongStatic(Field field) {
		int offset = Fields.getOffset(field);

		return (field.getModifiers() & Modifier.VOLATILE) != 0 ?
				internalUnsafe.getLongVolatile(field.getDeclaringClass(), offset) :
				internalUnsafe.getLong(field.getDeclaringClass(), offset);
	}

	public static void setFloat(Field field, Object object, float value) {
		int offset = Fields.getOffset(field);

		if ((field.getModifiers() & Modifier.VOLATILE) != 0)
			internalUnsafe.putFloatVolatile(object, offset, value);
		else
			internalUnsafe.putFloat(object, offset, value);
	}

	public static void setFloatStatic(Field field, float value) {
		int offset = Fields.getOffset(field);

		if ((field.getModifiers() & Modifier.VOLATILE) != 0) {
			internalUnsafe.putFloatVolatile(field.getDeclaringClass(), offset, value);
		} else
			internalUnsafe.putFloat(field.getDeclaringClass(), offset, value);
	}

	public static float getFloat(Field field, Object object) {
		int offset = Fields.getOffset(field);

		return (field.getModifiers() & Modifier.VOLATILE) != 0 ?
				internalUnsafe.getFloatVolatile(object, offset) :
				internalUnsafe.getFloat(object, offset);
	}

	public static float getFloatStatic(Field field) {
		int offset = Fields.getOffset(field);

		return (field.getModifiers() & Modifier.VOLATILE) != 0 ?
				internalUnsafe.getFloatVolatile(field.getDeclaringClass(), offset) :
				internalUnsafe.getFloat(field.getDeclaringClass(), offset);
	}

	public static void setDouble(Field field, Object object, double value) {
		int offset = Fields.getOffset(field);

		if ((field.getModifiers() & Modifier.VOLATILE) != 0)
			internalUnsafe.putDoubleVolatile(object, offset, value);
		else
			internalUnsafe.putDouble(object, offset, value);
	}

	public static void setDoubleStatic(Field field, double value) {
		int offset = Fields.getOffset(field);

		if ((field.getModifiers() & Modifier.VOLATILE) != 0)
			internalUnsafe.putDoubleVolatile(field.getDeclaringClass(), offset, value);
		else
			internalUnsafe.putDouble(field.getDeclaringClass(), offset, value);
	}

	public static double getDouble(Field field, Object object) {
		int offset = Fields.getOffset(field);

		return (field.getModifiers() & Modifier.VOLATILE) != 0 ?
				internalUnsafe.getDoubleVolatile(object, offset) :
				internalUnsafe.getDouble(object, offset);
	}

	public static double getDoubleStatic(Field field) {
		int offset = Fields.getOffset(field);

		return (field.getModifiers() & Modifier.VOLATILE) != 0 ?
				internalUnsafe.getDoubleVolatile(field.getDeclaringClass(), offset) :
				internalUnsafe.getDouble(field.getDeclaringClass(), offset);
	}

	public static void setChar(Field field, Object object, char value) {
		int offset = Fields.getOffset(field);

		if ((field.getModifiers() & Modifier.VOLATILE) != 0)
			internalUnsafe.putCharVolatile(object, offset, value);
		else
			internalUnsafe.putChar(object, offset, value);
	}

	public static void setCharStatic(Field field, char value) {
		int offset = Fields.getOffset(field);

		if ((field.getModifiers() & Modifier.VOLATILE) != 0)
			internalUnsafe.putCharVolatile(field.getDeclaringClass(), offset, value);
		else
			internalUnsafe.putChar(field.getDeclaringClass(), offset, value);
	}

	public static char getChar(Field field, Object object) {
		int offset = Fields.getOffset(field);

		return (field.getModifiers() & Modifier.VOLATILE) != 0 ?
				internalUnsafe.getCharVolatile(object, offset) :
				internalUnsafe.getChar(object, offset);
	}

	public static char getCharStatic(Field field) {
		int offset = Fields.getOffset(field);

		return (field.getModifiers() & Modifier.VOLATILE) != 0 ?
				internalUnsafe.getCharVolatile(field.getDeclaringClass(), offset) :
				internalUnsafe.getChar(field.getDeclaringClass(), offset);
	}

	public static void setBoolean(Field field, Object object, boolean value) {
		int offset = Fields.getOffset(field);

		if ((field.getModifiers() & Modifier.VOLATILE) != 0)
			internalUnsafe.putBooleanVolatile(object, offset, value);
		else
			internalUnsafe.putBoolean(object, offset, value);
	}

	public static void setBooleanStatic(Field field, boolean value) {
		int offset = Fields.getOffset(field);

		if ((field.getModifiers() & Modifier.VOLATILE) != 0)
			internalUnsafe.putBooleanVolatile(field.getDeclaringClass(), offset, value);
		else
			internalUnsafe.putBoolean(field.getDeclaringClass(), offset, value);
	}

	public static boolean getBoolean(Field field, Object object) {
		int offset = Fields.getOffset(field);

		return (field.getModifiers() & Modifier.VOLATILE) != 0 ?
				internalUnsafe.getBooleanVolatile(object, offset) :
				internalUnsafe.getBoolean(object, offset);
	}

	public static boolean getBooleanStatic(Field field) {
		int offset = Fields.getOffset(field);

		return (field.getModifiers() & Modifier.VOLATILE) != 0 ?
				internalUnsafe.getBooleanVolatile(field.getDeclaringClass(), offset) :
				internalUnsafe.getBoolean(field.getDeclaringClass(), offset);
	}

	public static void setObject(Field field, Object object, Object value) {
		int offset = Fields.getOffset(field);

		if ((field.getModifiers() & Modifier.VOLATILE) != 0)
			internalUnsafe.putReferenceVolatile(object, offset, value);
		else
			internalUnsafe.putReference(object, offset, value);
	}

	public static void setObjectStatic(Field field, Object value) {
		int offset = Fields.getOffset(field);

		if ((field.getModifiers() & Modifier.VOLATILE) != 0)
			internalUnsafe.putReferenceVolatile(field.getDeclaringClass(), offset, value);
		else
			internalUnsafe.putReference(field.getDeclaringClass(), offset, value);
	}

	public static Object getObject(Field field, Object object) {
		int offset = Fields.getOffset(field);

		return (field.getModifiers() & Modifier.VOLATILE) != 0 ?
				internalUnsafe.getReferenceVolatile(object, offset) :
				internalUnsafe.getReference(object, offset);
	}

	public static Object getObjectStatic(Field field) {
		int offset = Fields.getOffset(field);

		return (field.getModifiers() & Modifier.VOLATILE) != 0 ?
				internalUnsafe.getReferenceVolatile(field.getDeclaringClass(), offset) :
				internalUnsafe.getReference(field.getDeclaringClass(), offset);
	}

	public static void set(Field field, Object object, Object value) {
		int offset = Fields.getOffset(field);
		Class<?> clazz = field.getType();
		if ((field.getModifiers() & Modifier.VOLATILE) != 0)
			putVolatile(value, object, offset, clazz);
		else
			put(value, object, offset, clazz);
	}

	public static void setStatic(Field field, Object value) {
		Object base = field.getDeclaringClass();
		int offset = Fields.getOffset(field);
		Class<?> clazz = field.getType();

		if ((field.getModifiers() & Modifier.VOLATILE) != 0)
			putVolatile(value, base, offset, clazz);
		else
			put(value, base, offset, clazz);
	}

	public static Object get(Field field, Object object) {
		int offset = Fields.getOffset(field);
		Class<?> clazz = field.getType();

		return (field.getModifiers() & Modifier.VOLATILE) != 0 ?
				getVolatile(object, offset, clazz) :
				get(object, offset, clazz);
	}

	public static Object getStatic(Field field) {
		Object base = field.getDeclaringClass();
		int offset = Fields.getOffset(field);
		Class<?> clazz = field.getType();

		return (field.getModifiers() & Modifier.VOLATILE) != 0 ?
				getVolatile(base, offset, clazz) :
				get(base, offset, clazz);
	}

	static void put(Object value, Object object, long offset, Class<?> type) {
		if (type.isPrimitive()) {
			if (type == int.class) internalUnsafe.putInt(object, offset, (int) value);
			else if (type == float.class) internalUnsafe.putFloat(object, offset, (float) value);
			else if (type == boolean.class) internalUnsafe.putBoolean(object, offset, (boolean) value);
			else if (type == byte.class) internalUnsafe.putByte(object, offset, (byte) value);
			else if (type == double.class) internalUnsafe.putDouble(object, offset, (double) value);
			else if (type == long.class) internalUnsafe.putLong(object, offset, (long) value);
			else if (type == char.class) internalUnsafe.putChar(object, offset, (char) value);
			else if (type == short.class) internalUnsafe.putShort(object, offset, (short) value);
			else throw new IllegalArgumentException("unknown type of field " + type);
		} else {
			if (value != null && !type.isInstance(value)) throw new IllegalArgumentException();

			internalUnsafe.putReference(object, offset, value);
		}
	}

	static void putVolatile(Object value, Object object, long offset, Class<?> type) {
		if (type.isPrimitive()) {
			if (type == int.class) internalUnsafe.putIntVolatile(object, offset, (int) value);
			else if (type == float.class) internalUnsafe.putFloatVolatile(object, offset, (float) value);
			else if (type == boolean.class) internalUnsafe.putBooleanVolatile(object, offset, (boolean) value);
			else if (type == byte.class) internalUnsafe.putByteVolatile(object, offset, (byte) value);
			else if (type == long.class) internalUnsafe.putLongVolatile(object, offset, (long) value);
			else if (type == double.class) internalUnsafe.putDoubleVolatile(object, offset, (double) value);
			else if (type == char.class) internalUnsafe.putCharVolatile(object, offset, (char) value);
			else if (type == short.class) internalUnsafe.putShortVolatile(object, offset, (short) value);
			else throw new IllegalArgumentException("unknown type of field " + type);
		} else {
			if (value != null && !type.isInstance(value)) throw new IllegalArgumentException();

			internalUnsafe.putReferenceVolatile(object, offset, value);
		}
	}

	static Object get(Object object, long offset, Class<?> type) {
		if (type.isPrimitive()) {
			if (type == int.class) return internalUnsafe.getInt(object, offset);
			else if (type == float.class) return internalUnsafe.getFloat(object, offset);
			else if (type == boolean.class) return internalUnsafe.getBoolean(object, offset);
			else if (type == byte.class) return internalUnsafe.getByte(object, offset);
			else if (type == long.class) return internalUnsafe.getDouble(object, offset);
			else if (type == double.class) return internalUnsafe.getLong(object, offset);
			else if (type == char.class) return internalUnsafe.getChar(object, offset);
			else if (type == short.class) return internalUnsafe.getShort(object, offset);
			else throw new IllegalArgumentException("unknown type of field " + type);
		} else {
			return internalUnsafe.getReference(object, offset);
		}
	}

	static Object getVolatile(Object object, long offset, Class<?> type) {
		if (type.isPrimitive()) {
			if (type == int.class) return internalUnsafe.getIntVolatile(object, offset);
			else if (type == float.class) return internalUnsafe.getFloatVolatile(object, offset);
			else if (type == boolean.class) return internalUnsafe.getBooleanVolatile(object, offset);
			else if (type == byte.class) return internalUnsafe.getByteVolatile(object, offset);
			else if (type == long.class) return internalUnsafe.getLongVolatile(object, offset);
			else if (type == double.class) return internalUnsafe.getDoubleVolatile(object, offset);
			else if (type == char.class) return internalUnsafe.getCharVolatile(object, offset);
			else if (type == short.class) return internalUnsafe.getShortVolatile(object, offset);
			else throw new IllegalArgumentException("unknown type of field " + type);
		} else {
			return internalUnsafe.getReferenceVolatile(object, offset);
		}
	}
}
