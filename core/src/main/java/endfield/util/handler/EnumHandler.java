package endfield.util.handler;

import arc.struct.Seq;
import endfield.util.CollectionObjectMap;
import endfield.util.FieldAccessor;
import endfield.util.Reflections;

import static endfield.util.GetKt.sneakyThrow;

/**
 * The enumeration processor provides some operation methods for enum, which can create enumeration
 * instances and put them into the values of enumeration.
 * <p>This processor is an instance factory, and you need to construct an enumeration processor instance on
 * the target enumeration class in order to perform operations.
 * <p>Since the property is a private final, reflection settings are required. Here, {@link FieldHandler} is referenced to
 * complete the reflection operation. After you obtain an instance of the enumeration processor, you can
 * directly reference the methods provided by the processor.
 * <p><strong>Note that due to the mindustry source code's declaration of enumeration, the operated
 * enumeration may contain an array that holds all instances of the enumeration, but this is only when
 * values() is called during initialization.</strong>
 * <p><strong>But this array is a copy, if you have added items to the enumeration, it is best to reassign that array
 * as the changed values().</strong>
 *
 * @since 1.0.9
 */
public class EnumHandler<T extends Enum<T>> {
	public static final FieldAccessor ordinalAccessor, nameAccessor;

	static final CollectionObjectMap<Class<?>, EnumHandler<?>> handles = new CollectionObjectMap<>(Class.class, EnumHandler.class);

	final FieldAccessor valuesAccessor;

	public final Class<T> clazz;

	static {
		try {
			ordinalAccessor = Reflections.getFieldAccessor(Enum.class.getDeclaredField("ordinal"));
			nameAccessor = Reflections.getFieldAccessor(Enum.class.getDeclaredField("name"));
		} catch (NoSuchFieldException e) {
			throw sneakyThrow(e);
		}
	}

	/** Use {@link #of(Class)} */
	EnumHandler(Class<T> c) {
		if (!c.isEnum()) throw new IllegalArgumentException(c + " is not an enum class");

		clazz = c;
		valuesAccessor = Reflections.getFieldAccessor(clazz, field -> field.isSynthetic() && field.getName().contains("$VALUES"));
	}

	/**
	 * Construct an enumeration processor without a constructor implementation using the target
	 * enumeration type.
	 * <p>If the enumeration has a constructor implementation, you must provide an external implementation
	 * of the constructor, otherwise unexpected errors may occur.
	 *
	 * @param clazz The target enumeration types processed by the processor
	 */
	@SuppressWarnings("unchecked")
	public static <T extends Enum<T>> EnumHandler<T> of(Class<T> clazz) {
		return (EnumHandler<T>) handles.computeIfAbsent(clazz, t -> new EnumHandler<>((Class<T>) t));
	}

	/**
	 * Instantiate an enumeration object with a specified name, enumeration ordinal, and constructor
	 * parameters.
	 * <p>If you don't put it in the sub item list of the enumeration, its ordinal can be arbitrarily specified,
	 * although this may cause unnecessary errors.
	 *
	 * @param name    Name of enumeration
	 * @param ordinal The ordinal number of enumeration
	 * @param param   Parameters passed to constructor
	 * @return An enumeration instance with a specified name and ordinal number
	 */
	public T newEnumInstance(String name, int ordinal, Object... param) {
		Object[] params = new Object[param.length + 2];

		params[0] = name;
		params[1] = ordinal;

		System.arraycopy(param, 0, params, 2, param.length);

		return MethodHandler.newInstance(clazz, params);
	}

	/**
	 * Create an enumeration instance and insert it at the end of the enumeration
	 *
	 * @param addition The name of the instance created
	 * @param param    Additional constructor parameter list
	 */
	public T addEnumItemTail(String addition, Object... param) {
		int ordinal = valuesAccessor.<T[]>getObject(null).length;
		return addEnumItem(addition, ordinal, param);
	}

	/**
	 * Create an enumeration instance and insert it into the specified location in the enumeration item list.
	 * <p>The enumeration instances in the original position and the instances behind them will be moved
	 * backwards, and the enumeration ordinals will be updated synchronously with the updates of the list.
	 *
	 * @param addition The name of the instance created
	 * @param ordinal  The ordinal number corresponding to the insertion position
	 * @param param    Additional constructor parameter list
	 */
	public T addEnumItem(String addition, int ordinal, Object... param) {
		T newEnum = newEnumInstance(addition, ordinal, param);
		rearrange(newEnum, ordinal);
		return newEnum;
	}

	/**
	 * Place the specified enumeration item in the specified ordinal position, and reset all enumeration
	 * item ordinals to their correct positions.
	 * <p>The enumeration ordinal must be correctly specified between 0 and the total number of
	 * enumeration items, and the ordinal number of newly added items can be equal to the current total
	 * number.
	 *
	 * @param instance The target enumeration instance to be inserted
	 * @param ordinal  The enumeration ordinal of the target position to be inserted
	 * @throws IndexOutOfBoundsException If the specified ordinal is greater than or equal to the
	 * current total number of elements in the enumeration, or if the ordinal of the newly added
	 * item is greater than the total number of elements
	 */
	public void rearrange(T instance, int ordinal) {
		T[] arr = valuesAccessor.getObject(null);
		Seq<T> values = Seq.with(arr);
		if (values.contains(instance) && ordinal >= values.size)
			throw new IndexOutOfBoundsException("rearrange a exist item, ordinal should be less than amount of all items, (ordinal: " + ordinal + ", amount: " + values.size + ")");
		else if (ordinal > values.size)
			throw new IndexOutOfBoundsException("add a new item, ordinal should be equal or less than amount of all items, (ordinal: " + ordinal + ", amount: " + values.size + ")");

		values.remove(instance);

		values.insert(ordinal, instance);

		valuesAccessor.setObject(null, values.toArray(clazz));
	}

	public void swap(int fromOrdinal, int toOrdinal) {
		T[] values = valuesAccessor.getObject(null);

		if (fromOrdinal >= values.length)
			throw new IndexOutOfBoundsException("fromOrdinal can't be >= values.length: " + fromOrdinal + " >= " + values.length);
		if (toOrdinal >= values.length)
			throw new IndexOutOfBoundsException("toOrdinal can't be >= values.length: " + toOrdinal + " >= " + values.length);

		T from = values[fromOrdinal], to = values[toOrdinal];

		ordinalAccessor.setInt(from, toOrdinal);
		ordinalAccessor.setInt(to, fromOrdinal);

		values[fromOrdinal] = to;
		values[toOrdinal] = from;
	}

	public void swap(T from, T to) {
		if (!clazz.isInstance(from)) throw new IllegalArgumentException(from + " must be an instance of " + clazz);
		if (!clazz.isInstance(to)) throw new IllegalArgumentException(to + " must be an instance of " + clazz);

		int fromOrdinal = from.ordinal(), toOrdinal = to.ordinal();

		ordinalAccessor.setInt(from, toOrdinal);
		ordinalAccessor.setInt(to, fromOrdinal);

		T[] values = valuesAccessor.getObject(null);

		values[fromOrdinal] = to;
		values[toOrdinal] = from;
	}

	public void rename(T instance, String name) {
		T[] arr = valuesAccessor.getObject(null);
		for (T t : arr) {
			if (t != instance && t.name().equals(name)) return;
		}

		nameAccessor.set(instance, name);
	}

	/** Directly return the shared array of {@code values()}, do not easily change its contents. */
	public T[] values() {
		return valuesAccessor.getObject(null);
	}
}
