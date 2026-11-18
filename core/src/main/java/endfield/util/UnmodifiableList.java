package endfield.util;

import java.lang.reflect.Array;
import java.util.AbstractList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.IntFunction;

public class UnmodifiableList<E> extends AbstractList<E> {
	final E[] values;

	public UnmodifiableList(E[] es) {
		values = es;
	}

	@SafeVarargs
	public static <E> UnmodifiableList<E> with(E... values) {
		return new UnmodifiableList<>(values);
	}

	@Override
	public E get(int index) {
		return values[index];
	}

	@Override
	public int size() {
		return values.length;
	}

	@Override
	public boolean add(E e) {
		return false;
	}

	@Override
	public boolean addAll(Collection<? extends E> c) {
		return false;
	}

	@Override
	public boolean addAll(int index, Collection<? extends E> c) {
		return false;
	}

	@Override
	public void forEach(Consumer<? super E> action) {
		for (E value : values) {
			action.accept(value);
		}
	}

	@Override
	public Object[] toArray() {
		Object[] result = new Object[values.length];
		System.arraycopy(values, 0, result, 0, values.length);
		return result;
	}

	@SuppressWarnings("unchecked")
	public <T> T[] toArray(Class<?> type) {
		T[] result = (T[]) Array.newInstance(type, values.length);
		System.arraycopy(values, 0, result, 0, values.length);
		return result;
	}

	@Override
	public <T> T[] toArray(T[] a) {
		if (a.length < values.length)
			return toArray(a.getClass().getComponentType());

		System.arraycopy(values, 0, a, 0, values.length);
		if (a.length > values.length)
			a[values.length] = null;
		return a;
	}

	@Override
	public <T> T[] toArray(IntFunction<T[]> generator) {
		T[] a = generator.apply(values.length);
		System.arraycopy(values, 0, a, 0, values.length);
		return a;
	}

	@Override
	public int hashCode() {
		E[] es = values;
		int hashCode = 1;
		for (int i = 0; i < values.length; i++) {
			E item = es[i];
			hashCode = 31 * hashCode + (item == null ? 0 : item.hashCode());
		}
		return hashCode;
	}

	@Override
	public boolean equals(Object o) {
		if (o == this) return true;
		if (!(o instanceof List<?> other)) return false;
		int n = values.length;
		if (n != other.size()) return false;
		Object[] array = values;
		for (int i = 0; i < n; i++) {
			Object o1 = array[i];
			Object o2 = other.get(i);
			if (!Objects.equals(o1, o2)) return false;
		}
		return true;
	}

	@Override
	public String toString() {
		if (values.length == 0) return "[]";
		E[] es = values;
		StringBuilder buffer = new StringBuilder(32);
		buffer.append('[');
		buffer.append(es[0]);
		for (int i = 1; i < values.length; i++) {
			buffer.append(", ");
			buffer.append(es[i]);
		}
		buffer.append(']');
		return buffer.toString();
	}
}
