package endfield.util;

import arc.func.Prov;
import arc.util.Nullable;

import java.lang.reflect.Field;
import java.util.function.Function;

import static endfield.util.GetKt.sneakyThrow;

public abstract class AbstractFieldAccessorHelper implements FieldAccessHelper {
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
			Class<?> current = clazz;
			while (current != null) {
				field = findField(current, name);
				if (field != null) {
					map.put(name, field);
					return field;
				}

				current = current.getSuperclass();
			}
		}

		throw sneakyThrow(new NoSuchFieldException("field " + name + " was not found in class: " + clazz));
	}

	protected @Nullable Field findField(Class<?> clazz, String name) {
		Field[] fields = fieldsMap.computeIfAbsent(clazz, function1);
		for (Field field : fields) {
			if (field.getName().equals(name)) return field;
		}
		return null;
	}

	@Override
	public void clear() {
		fieldsMap.clear();
	}

	@Override
	public void clear(Class<?> clazz) {
		fieldsMap.remove(clazz);
	}
}
