package endfield.util.handler;

import arc.func.Boolf;
import arc.struct.ObjectSet;
import arc.struct.Seq;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

/**
 * A set of practical tools for copying field properties of an object to another.
 *
 * @since 1.0.9
 */
public final class ObjectHandler {
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
		Seq<Field> fields = getFields(source, null);

		for (Field field : fields) {
			FieldHandler.set(target, field, FieldHandler.get(source, field));
		}
	}

	/**
	 * Copy the values of attributes of the source object that are not on the blacklist to the target object,
	 * which must be the type or subclass of the source object.
	 *
	 * @param source Source object of attribute
	 * @param target Copy the attribute to the target object
	 * @param blacks Field blacklist
	 */
	public static <S, T extends S> void copyFieldAsBlack(S source, T target, String... blacks) {
		ObjectSet<String> black = ObjectSet.with(blacks);
		Seq<Field> fields = getFields(source, field -> !black.contains(field.getName()));

		for (Field field : fields) {
			FieldHandler.set(target, field, FieldHandler.get(source, field));
		}
	}

	/**
	 * Copy the specified attribute values of the source object to the target object, which must be the type
	 * or subclass of the source object.
	 *
	 * @param source Source object of attribute
	 * @param target Copy the attribute to the target object
	 * @param whites Field whitelist
	 */
	public static <S, T extends S> void copyFieldAsWhite(S source, T target, String... whites) {
		ObjectSet<String> black = ObjectSet.with(whites);
		Seq<Field> fields = getFields(source, field -> black.contains(field.getName()));

		for (Field field : fields) {
			FieldHandler.set(target, field, FieldHandler.get(source, field));
		}
	}

	public static Seq<Field> getFields(Object object, @Nullable Boolf<Field> filler) {
		Class<?> curr = object.getClass();
		Seq<Field> fields = new Seq<>(Field.class);

		while (curr != Object.class) {
			for (Field field : ClassHandler.getFields(curr)) {
				if ((field.getModifiers() & Modifier.STATIC) != 0 || filler != null && !filler.get(field)) continue;

				fields.add(field);
			}

			curr = curr.getSuperclass();
		}
		return fields;
	}
}
