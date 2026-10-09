package endfield.util;

import java.lang.reflect.Field;

public interface FieldAccessHelper {
	void setByte(Object object, String name, byte value);

	void setByteStatic(Class<?> clazz, String name, byte value);

	byte getByte(Object object, String name);

	byte getByteStatic(Class<?> clazz, String name);

	void setShort(Object object, String name, short value);

	void setShortStatic(Class<?> clazz, String name, short value);

	short getShort(Object object, String name);

	short getShortStatic(Class<?> clazz, String name);

	void setInt(Object object, String name, int value);

	void setIntStatic(Class<?> clazz, String name, int value);

	int getInt(Object object, String name);

	int getIntStatic(Class<?> clazz, String name);

	void setLong(Object object, String name, long value);

	void setLongStatic(Class<?> clazz, String name, long value);

	long getLong(Object object, String name);

	long getLongStatic(Class<?> clazz, String name);

	void setFloat(Object object, String name, float value);

	void setFloatStatic(Class<?> clazz, String name, float value);

	float getFloat(Object object, String name);

	float getFloatStatic(Class<?> clazz, String name);

	void setDouble(Object object, String name, double value);

	void setDoubleStatic(Class<?> clazz, String name, double value);

	double getDouble(Object object, String name);

	double getDoubleStatic(Class<?> clazz, String name);

	void setChar(Object object, String name, char value);

	void setCharStatic(Class<?> clazz, String name, char value);

	char getChar(Object object, String name);

	char getCharStatic(Class<?> clazz, String name);

	void setBoolean(Object object, String name, boolean value);

	void setBooleanStatic(Class<?> clazz, String name, boolean value);

	boolean getBoolean(Object object, String name);

	boolean getBooleanStatic(Class<?> clazz, String name);

	void setObject(Object object, String name, Object value);

	void setObjectStatic(Class<?> clazz, String name, Object value);

	<T> T getObject(Object object, String name);

	<T> T getObjectStatic(Class<?> clazz, String name);

	void set(Object object, String name, Object value);

	void setStatic(Class<?> clazz, String name, Object value);

	<T> T get(Object object, String name);

	<T> T getStatic(Class<?> clazz, String name);

	void setByte(Object object, Field field, byte value);

	void setByteStatic(Field field, byte value);

	byte getByte(Object object, Field field);

	byte getByteStatic(Field field);

	void setShort(Object object, Field field, short value);

	void setShortStatic(Field field, short value);

	short getShort(Object object, Field field);

	short getShortStatic(Field field);

	void setInt(Object object, Field field, int value);

	void setIntStatic(Field field, int value);

	int getInt(Object object, Field field);

	int getIntStatic(Field field);

	void setLong(Object object, Field field, long value);

	void setLongStatic(Field field, long value);

	long getLong(Object object, Field field);

	long getLongStatic(Field field);

	void setFloat(Object object, Field field, float value);

	void setFloatStatic(Field field, float value);

	float getFloat(Object object, Field field);

	float getFloatStatic(Field field);

	void setDouble(Object object, Field field, double value);

	void setDoubleStatic(Field field, double value);

	double getDouble(Object object, Field field);

	double getDoubleStatic(Field field);

	void setChar(Object object, Field field, char value);

	void setCharStatic(Field field, char value);

	char getChar(Object object, Field field);

	char getCharStatic(Field field);

	void setBoolean(Object object, Field field, boolean value);

	void setBooleanStatic(Field field, boolean value);

	boolean getBoolean(Object object, Field field);

	boolean getBooleanStatic(Field field);

	void setObject(Object object, Field field, Object value);

	void setObjectStatic(Field field, Object value);

	<T> T getObject(Object object, Field field);

	<T> T getObjectStatic(Field field);

	void set(Object object, Field field, Object value);

	void setStatic(Field field, Object value);

	<T> T get(Object object, Field field);

	<T> T getStatic(Field field);

	/** Clear the field cache of all classes. */
	default void clear() {}

	/** Clear the field cache of the specified class. */
	default void clear(Class<?> clazz) {}
}
