package endfield.util;

import arc.func.Prov;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.util.function.Function;

public class ReflectionFieldAccessHelper implements FieldAccessHelper {
	protected static final CollectionObjectMap<Class<?>, CollectionObjectMap<String, Field>> fieldMap = new CollectionObjectMap<>(Class.class, CollectionObjectMap.class);
	protected static final CollectionObjectMap<Class<?>, Field[]> fieldsMap = new CollectionObjectMap<>(Class.class, Field[].class);

	protected static final Prov<CollectionObjectMap<String, Field>> prov1 = () -> new CollectionObjectMap<>(String.class, Field.class);
	protected static final Function<Class<?>, Field[]> function1 = Class::getDeclaredFields;

	public Field getField(Class<?> clazz, String name, boolean isStatic) {
		CollectionObjectMap<String, Field> map = fieldMap.get(clazz, prov1);
		Field field = map.get(name);
		if (field != null) return field;

		if (isStatic) {
			field = findField(clazz, name);
			if (field != null) {
				map.put(name, field);
				return field;
			}
		} else {
			Class<?> curr = clazz;
			while (curr != null) {
				field = findField(curr, name);
				if (field != null) {
					map.put(name, field);
					return field;
				}

				curr = curr.getSuperclass();
			}
		}

		throw new RuntimeException("field " + name + " was not found in class: " + clazz);
	}

	protected @Nullable Field findField(Class<?> clazz, String name) {
		Field[] fields = fieldsMap.computeIfAbsent(clazz, function1);
		for (Field field : fields) {
			if (field.getName().equals(name)) return field;
		}
		return null;
	}

	@Override
	public void setByte(Object object, String name, byte value) {
		try {
			getField(object.getClass(), name, false).setByte(object, value);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void setByteStatic(Class<?> clazz, String name, byte value) {
		try {
			getField(clazz, name, true).setByte(null, value);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public byte getByte(Object object, String name) {
		try {
			return getField(object.getClass(), name, false).getByte(object);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public byte getByteStatic(Class<?> clazz, String name) {
		try {
			return getField(clazz, name, true).getByte(null);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void setShort(Object object, String name, short value) {
		try {
			getField(object.getClass(), name, false).setInt(object, value);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void setShortStatic(Class<?> clazz, String name, short value) {
		try {
			getField(clazz, name, true).setShort(null, value);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public short getShort(Object object, String name) {
		try {
			return getField(object.getClass(), name, false).getShort(object);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public short getShortStatic(Class<?> clazz, String name) {
		try {
			return getField(clazz, name, true).getShort(null);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void setInt(Object object, String name, int value) {
		try {
			getField(object.getClass(), name, false).setInt(object, value);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void setIntStatic(Class<?> clazz, String name, int value) {
		try {
			getField(clazz, name, true).setInt(null, value);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public int getInt(Object object, String name) {
		try {
			return getField(object.getClass(), name, false).getInt(object);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public int getIntStatic(Class<?> clazz, String name) {
		try {
			return getField(clazz, name, true).getInt(null);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void setLong(Object object, String name, long value) {
		try {
			getField(object.getClass(), name, false).setLong(object, value);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void setLongStatic(Class<?> clazz, String name, long value) {
		try {
			getField(clazz, name, true).setLong(null, value);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public long getLong(Object object, String name) {
		try {
			return getField(object.getClass(), name, false).getLong(object);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public long getLongStatic(Class<?> clazz, String name) {
		try {
			return getField(clazz, name, true).getLong(null);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void setFloat(Object object, String name, float value) {
		try {
			getField(object.getClass(), name, false).setFloat(object, value);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void setFloatStatic(Class<?> clazz, String name, float value) {
		try {
			getField(clazz, name, true).setFloat(null, value);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public float getFloat(Object object, String name) {
		try {
			return getField(object.getClass(), name, false).getFloat(object);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public float getFloatStatic(Class<?> clazz, String name) {
		try {
			return getField(clazz, name, true).getFloat(null);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void setDouble(Object object, String name, double value) {
		try {
			getField(object.getClass(), name, false).setDouble(object, value);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void setDoubleStatic(Class<?> clazz, String name, double value) {
		try {
			getField(clazz, name, true).setDouble(null, value);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public double getDouble(Object object, String name) {
		try {
			return getField(object.getClass(), name, false).getDouble(object);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public double getDoubleStatic(Class<?> clazz, String name) {
		try {
			return getField(clazz, name, true).getDouble(null);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void setChar(Object object, String name, char value) {
		try {
			getField(object.getClass(), name, false).setChar(object, value);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void setCharStatic(Class<?> clazz, String name, char value) {
		try {
			getField(clazz, name, true).setChar(null, value);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public char getChar(Object object, String name) {
		try {
			return getField(object.getClass(), name, false).getChar(object);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public char getCharStatic(Class<?> clazz, String name) {
		try {
			return getField(clazz, name, true).getChar(null);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void setBoolean(Object object, String name, boolean value) {
		try {
			getField(object.getClass(), name, false).setBoolean(object, value);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void setBooleanStatic(Class<?> clazz, String name, boolean value) {
		try {
			getField(clazz, name, true).setBoolean(null, value);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public boolean getBoolean(Object object, String name) {
		try {
			return getField(object.getClass(), name, false).getBoolean(object);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public boolean getBooleanStatic(Class<?> clazz, String name) {
		try {
			return getField(clazz, name, true).getBoolean(null);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void setObject(Object object, String name, Object value) {
		try {
			getField(object.getClass(), name, false).set(object, value);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void setObjectStatic(Class<?> clazz, String name, Object value) {
		try {
			getField(clazz, name, true).set(null, value);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T getObject(Object object, String name) {
		try {
			return (T) getField(object.getClass(), name, false).get(object);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T getObjectStatic(Class<?> clazz, String name) {
		try {
			return (T) getField(clazz, name, true).get(null);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void set(Object object, String name, Object value) {
		try {
			getField(object.getClass(), name, false).set(object, value);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void setStatic(Class<?> clazz, String name, Object value) {
		try {
			getField(clazz, name, true).set(null, value);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T get(Object object, String name) {
		try {
			return (T) getField(object.getClass(), name, false).get(object);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T getStatic(Class<?> clazz, String name) {
		try {
			return (T) getField(clazz, name, true).get(null);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void setByte(Object object, Field field, byte value) {
		try {
			field.setByte(object, value);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void setByteStatic(Field field, byte value) {
		try {
			field.setByte(null, value);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public byte getByte(Object object, Field field) {
		try {
			return field.getByte(object);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public byte getByteStatic(Field field) {
		try {
			return field.getByte(null);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void setShort(Object object, Field field, short value) {
		try {
			field.setShort(object, value);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void setShortStatic(Field field, short value) {
		try {
			field.setShort(null, value);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public short getShort(Object object, Field field) {
		try {
			return field.getShort(object);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public short getShortStatic(Field field) {
		try {
			return field.getShort(null);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void setInt(Object object, Field field, int value) {
		try {
			field.setInt(object, value);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void setIntStatic(Field field, int value) {
		try {
			field.setInt(null, value);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public int getInt(Object object, Field field) {
		try {
			return field.getInt(object);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public int getIntStatic(Field field) {
		try {
			return field.getInt(null);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void setLong(Object object, Field field, long value) {
		try {
			field.setLong(object, value);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void setLongStatic(Field field, long value) {
		try {
			field.setLong(null, value);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public long getLong(Object object, Field field) {
		try {
			return field.getLong(object);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public long getLongStatic(Field field) {
		try {
			return field.getLong(null);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void setFloat(Object object, Field field, float value) {
		try {
			field.setFloat(object, value);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void setFloatStatic(Field field, float value) {
		try {
			field.setFloat(null, value);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public float getFloat(Object object, Field field) {
		try {
			return field.getFloat(object);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public float getFloatStatic(Field field) {
		try {
			return field.getFloat(null);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void setDouble(Object object, Field field, double value) {
		try {
			field.setDouble(object, value);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void setDoubleStatic(Field field, double value) {
		try {
			field.setDouble(null, value);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public double getDouble(Object object, Field field) {
		try {
			return field.getDouble(object);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public double getDoubleStatic(Field field) {
		try {
			return field.getDouble(null);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void setChar(Object object, Field field, char value) {
		try {
			field.setChar(object, value);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void setCharStatic(Field field, char value) {
		try {
			field.setChar(null, value);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public char getChar(Object object, Field field) {
		try {
			return field.getChar(object);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public char getCharStatic(Field field) {
		try {
			return field.getChar(null);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void setBoolean(Object object, Field field, boolean value) {
		try {
			field.setBoolean(object, value);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void setBooleanStatic(Field field, boolean value) {
		try {
			field.setBoolean(null, value);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public boolean getBoolean(Object object, Field field) {
		try {
			return field.getBoolean(object);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public boolean getBooleanStatic(Field field) {
		try {
			return field.getBoolean(null);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void setObject(Object object, Field field, Object value) {
		try {
			field.set(object, value);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void setObjectStatic(Field field, Object value) {
		try {
			field.set(null, value);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T getObject(Object object, Field field) {
		try {
			return (T) field.get(object);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T getObjectStatic(Field field) {
		try {
			return (T) field.get(null);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void set(Object object, Field field, Object value) {
		try {
			field.set(object, value);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void setStatic(Field field, Object value) {
		try {
			field.set(null, value);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T get(Object object, Field field) {
		try {
			return (T) field.get(object);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T getStatic(Field field) {
		try {
			return (T) field.get(null);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}
}
