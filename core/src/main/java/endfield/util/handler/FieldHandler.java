package endfield.util.handler;

import java.lang.reflect.Field;

import static endfield.Vars2.fieldAccessHelper;

/**
 * A collection of static methods for field operations, including read, write, and other operations. All
 * checked exceptions thrown by references are caught and encapsulated in {@link RuntimeException}, without the
 * need for manual try or throw.
 *
 * @since 1.0.9
 */
public final class FieldHandler {
	private FieldHandler() {}

	/**
	 * Setting the selected property value of a specified object will ignore the access modifier and final
	 * modifier of that property. If the target object is null, the field set will be static.
	 * <p>Unless the field is static, passing an empty target object is not allowed.
	 *
	 * @param obj   Object to change attribute value
	 * @param key   The attribute name to be changed
	 * @param value The value to be written
	 * @throws NullPointerException If the target object passed in is null and the field is not static
	 */
	public static void set(Object obj, String key, Object value) {
		fieldAccessHelper.set(obj, key, value);
	}

	public static void setStatic(Class<?> clazz, String key, Object value) {
		fieldAccessHelper.setStatic(clazz, key, value);
	}

	/**
	 * Retrieve the specified field value and return it. If the field does not exist, an exception will be
	 * thrown. If the target object is null, the set field will be static.
	 * <p>Unless the field is static, passing an empty target object is not allowed.
	 *
	 * @param obj If the field of the target object is not static, it is required not to be empty. If the
	 *            field is static, this parameter will be ignored.
	 * @param key field name
	 * @return The value of the field, if it exists
	 * @throws NullPointerException If the target object passed in is null and the field is not static
	 */
	public static <T> T get(Object obj, String key) {
		return fieldAccessHelper.get(obj, key);
	}

	public static <T> T getStatic(Class<?> clazz, String key) {
		return fieldAccessHelper.getStatic(clazz, key);
	}

	public static void setObject(Object obj, String key, Object value) {
		fieldAccessHelper.setObject(obj, key, value);
	}

	public static void setObjectStatic(Class<?> clazz, String key, Object value) {
		fieldAccessHelper.setObjectStatic(clazz, key, value);
	}

	public static <T> T getObject(Object obj, String key) {
		return fieldAccessHelper.getObject(obj, key);
	}

	public static <T> T getObjectStatic(Class<?> clazz, String key) {
		return fieldAccessHelper.getObjectStatic(clazz, key);
	}

	public static void setByte(Object obj, String key, byte value) {
		fieldAccessHelper.setByte(obj, key, value);
	}

	public static void setByteStatic(Class<?> clazz, String key, byte value) {
		fieldAccessHelper.setByteStatic(clazz, key, value);
	}

	public static byte getByte(Object obj, String key) {
		return fieldAccessHelper.getByte(obj, key);
	}

	public static byte getByteStatic(Class<?> clazz, String key) {
		return fieldAccessHelper.getByteStatic(clazz, key);
	}

	public static void setShort(Object obj, String key, short value) {
		fieldAccessHelper.setShort(obj, key, value);
	}

	public static void setShortStatic(Class<?> clazz, String key, short value) {
		fieldAccessHelper.setShortStatic(clazz, key, value);
	}

	public static short getShort(Object obj, String key) {
		return fieldAccessHelper.getShort(obj, key);
	}

	public static short getShortStatic(Class<?> clazz, String key) {
		return fieldAccessHelper.getShortStatic(clazz, key);
	}

	public static void setInt(Object obj, String key, int value) {
		fieldAccessHelper.setInt(obj, key, value);
	}

	public static void setIntStatic(Class<?> clazz, String key, int value) {
		fieldAccessHelper.setIntStatic(clazz, key, value);
	}

	public static int getInt(Object obj, String key) {
		return fieldAccessHelper.getInt(obj, key);
	}

	public static int getIntStatic(Class<?> clazz, String key) {
		return fieldAccessHelper.getIntStatic(clazz, key);
	}

	public static void setFloat(Object obj, String key, float value) {
		fieldAccessHelper.setFloat(obj, key, value);
	}

	public static void setFloatStatic(Class<?> clazz, String key, float value) {
		fieldAccessHelper.setFloatStatic(clazz, key, value);
	}

	public static float getFloat(Object obj, String key) {
		return fieldAccessHelper.getFloat(obj, key);
	}

	public static float getFloatStatic(Class<?> clazz, String key) {
		return fieldAccessHelper.getFloatStatic(clazz, key);
	}

	public static void setDouble(Object obj, String key, double value) {
		fieldAccessHelper.setDouble(obj, key, value);
	}

	public static void setDoubleStatic(Class<?> clazz, String key, double value) {
		fieldAccessHelper.setDoubleStatic(clazz, key, value);
	}

	public static double getDouble(Object obj, String key) {
		return fieldAccessHelper.getDouble(obj, key);
	}

	public static double getDoubleStatic(Class<?> clazz, String key) {
		return fieldAccessHelper.getDoubleStatic(clazz, key);
	}

	public static void setBoolean(Object obj, String key, boolean value) {
		fieldAccessHelper.setBoolean(obj, key, value);
	}

	public static void setBooleanStatic(Class<?> clazz, String key, boolean value) {
		fieldAccessHelper.setBooleanStatic(clazz, key, value);
	}

	public static boolean getBoolean(Object obj, String key) {
		return fieldAccessHelper.getBoolean(obj, key);
	}

	public static boolean getBooleanStatic(Class<?> clazz, String key) {
		return fieldAccessHelper.getBooleanStatic(clazz, key);
	}

	/**
	 * Set the value of the field. Fields with inline values during compilation are invalid.
	 *
	 * @param object the object whose field should be modified, If the field is static, please set it to null
	 */
	public static void set(Object object, Field field, Object value) {
		if (object == null) fieldAccessHelper.setStatic(field, value);
		else fieldAccessHelper.set(object, field, value);
	}

	public static <T> T get(Object object, Field field) {
		return object == null ? fieldAccessHelper.getStatic(field) : fieldAccessHelper.get(object, field);
	}

	public static void setObject(Object object, Field field, Object value) {
		if (object == null) fieldAccessHelper.setObjectStatic(field, value);
		else fieldAccessHelper.setObject(object, field, value);
	}

	public static <T> T getObject(Object object, Field field) {
		return object == null ? fieldAccessHelper.getObjectStatic(field) : fieldAccessHelper.getObject(object, field);
	}

	public static void setBoolean(Object object, Field field, boolean value) {
		if (object == null) fieldAccessHelper.setBooleanStatic(field, value);
		else fieldAccessHelper.setBoolean(object, field, value);
	}

	public static boolean getBoolean(Object object, Field field) {
		return object == null ? fieldAccessHelper.getBooleanStatic(field) : fieldAccessHelper.getBoolean(object, field);
	}

	/**
	 * Set the value of the int field. Fields with inline values during compilation are invalid.
	 *
	 * @param field the object whose field should be modified, If the field is static, please set it to null
	 */
	public static void setInt(Object object, Field field, int value) {
		if (object == null) fieldAccessHelper.setIntStatic(field, value);
		else fieldAccessHelper.setInt(object, field, value);
	}

	public static int getInt(Object object, Field field) {
		return object == null ? fieldAccessHelper.getIntStatic(field) : fieldAccessHelper.getInt(object, field);
	}

	public static void setFloat(Object object, Field field, float value) {
		if (object == null) fieldAccessHelper.setFloatStatic(field, value);
		else fieldAccessHelper.setFloat(object, field, value);
	}

	public static float getFloat(Object object, Field field) {
		return object == null ? fieldAccessHelper.getFloatStatic(field) : fieldAccessHelper.getFloat(object, field);
	}
}
