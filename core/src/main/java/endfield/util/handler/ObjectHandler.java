package endfield.util.handler;

import arc.func.Boolf;
import arc.struct.ObjectSet;
import arc.struct.Seq;
import arc.util.Nullable;
import endfield.util.CollectionObjectMap;
import endfield.util.FieldAccessor;
import endfield.util.Reflections;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.function.Function;

/**
 * A set of practical tools for copying field properties of an object to another.
 *
 * @since 1.0.9
 */
public final class ObjectHandler {
	static final CollectionObjectMap<Class<?>, Field[]> fields = new CollectionObjectMap<>(Class.class, Field[].class);
	static final CollectionObjectMap<Class<?>, FieldAccessor[]> accessors = new CollectionObjectMap<>(Class.class, FieldAccessor[].class);

	static final Function<Class<?>, Field[]> getFields = ClassHandler::getFields;

	private ObjectHandler() {}

	/**
	 * Copy all attribute values of the source object completely to the target object, which must be the
	 * type or subclass of the source object.
	 *
	 * @param source Source object of attribute
	 * @param target Copy the attribute to the target object
	 * @throws IllegalArgumentException If the target object is not assigned from the source class
	 */
	public static <S, T extends S> void copyField(S source, T target) {
		/*Seq<Field> fields = getFields(source, null);

		for (Field field : fields) {
			FieldHandler.set(target, field, FieldHandler.get(source, field));
		}*/

		// Cache fields to improve performance.
		for (FieldAccessor accessor : accessors.computeIfAbsent(source.getClass(), type -> {
			Seq<FieldAccessor> result = new Seq<>(FieldAccessor.class);
			Class<?> current = type;
			while (current != Object.class) {
				for (Field field : fields.computeIfAbsent(current, getFields)) {
					if ((field.getModifiers() & Modifier.STATIC) != 0) continue;

					result.add(Reflections.getFieldAccessor(field));
				}

				current = current.getSuperclass();
			}
			return result.toArray(FieldAccessor.class);
		})) {
			accessor.set(target, accessor.get(source));
		}
	}

	public static <S, T extends S> void copyFieldAsBlack(S source, T target, String... blacks) {
		copyFieldAsBlack(source, target, ObjectSet.with(blacks));
	}

	/**
	 * Copy the values of attributes of the source object that are not on the blacklist to the target object,
	 * which must be the type or subclass of the source object.
	 *
	 * @param source Source object of attribute
	 * @param target Copy the attribute to the target object
	 * @param blacks Field blacklist
	 */
	public static <S, T extends S> void copyFieldAsBlack(S source, T target, ObjectSet<String> blacks) {
		Class<?> current = source.getClass();
		while (current != Object.class) {
			for (Field field : fields.computeIfAbsent(current, getFields)) {
				if ((field.getModifiers() & Modifier.STATIC) != 0 || blacks.contains(field.getName())) continue;

				FieldHandler.set(target, field, FieldHandler.get(source, field));
			}

			current = current.getSuperclass();
		}
	}

	public static <S, T extends S> void copyFieldAsWhite(S source, T target, String... whites) {
		copyFieldAsWhite(source, target, ObjectSet.with(whites));
	}

	/**
	 * Copy the specified attribute values of the source object to the target object, which must be the type
	 * or subclass of the source object.
	 *
	 * @param source Source object of attribute
	 * @param target Copy the attribute to the target object
	 * @param whites Field whitelist
	 */
	public static <S, T extends S> void copyFieldAsWhite(S source, T target, ObjectSet<String> whites) {
		Class<?> current = source.getClass();
		while (current != Object.class) {
			for (Field field : fields.computeIfAbsent(current, getFields)) {
				if ((field.getModifiers() & Modifier.STATIC) != 0 || !whites.contains(field.getName())) continue;

				FieldHandler.set(target, field, FieldHandler.get(source, field));
			}

			current = current.getSuperclass();
		}
	}

	public static Seq<Field> getFields(Object object, @Nullable Boolf<Field> filler) {
		Seq<Field> result = new Seq<>(Field.class);

		Class<?> current = object.getClass();
		while (current != Object.class) {
			for (Field field : fields.computeIfAbsent(current, getFields)) {
				if ((field.getModifiers() & Modifier.STATIC) != 0 || filler != null && !filler.get(field)) continue;

				result.add(field);
			}

			current = current.getSuperclass();
		}
		return result;
	}
}
