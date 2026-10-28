package endfield.util;

import arc.func.Func;
import arc.util.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public final class Collections2 {
	private Collections2() {}

	public static <T, R> @Nullable R firstNotNullOfOrNull(Iterable<T> iterable, Func<? super T, ? extends R> transform) {
		for (T element : iterable) {
			R result = transform.get(element);
			if (result != null) {
				return result;
			}
		}
		return null;
	}

	public static <T> void forEachIndexed(Iterable<T> iterable, IntTBiConsume<? super T> action) {
		int i = 0;
		for (T t : iterable) {
			action.get(i++, t);
		}
	}

	public static <T> List<T> plus(Collection<T> c1, Collection<T> c2) {
		ArrayList<T> result = new ArrayList<>((c1 == null ? 0 : c1.size()) + (c2 == null ? 0 : c2.size()));

		if (c1 != null) result.addAll(c1);
		if (c2 != null) result.addAll(c2);

		return result;
	}

	public static <T> List<T> plus(Collection<T> c1, Collection<T> c2, Collection<T> c3) {
		ArrayList<T> result = new ArrayList<>((c1 == null ? 0 : c1.size()) + (c2 == null ? 0 : c2.size()) + (c3 == null ? 0 : c3.size()));

		if (c1 != null) result.addAll(c1);
		if (c2 != null) result.addAll(c2);
		if (c3 != null) result.addAll(c3);

		return result;
	}

	public static <T> List<T> plus(Collection<T> c1, Collection<T> c2, Collection<T> c3, Collection<T> c4) {
		ArrayList<T> result = new ArrayList<>((c1 == null ? 0 : c1.size()) + (c2 == null ? 0 : c2.size()) + (c3 == null ? 0 : c3.size()) + (c4 == null ? 0 : c4.size()));

		if (c1 != null) result.addAll(c1);
		if (c2 != null) result.addAll(c2);
		if (c3 != null) result.addAll(c3);
		if (c4 != null) result.addAll(c4);

		return result;
	}

	@SuppressWarnings("unchecked")
	public static <T> List<T> filterIsInstance(Object[] array, Class<T> type) {
		ArrayList<T> result = new ArrayList<>();
		for (Object o : array) {
			if (type.isInstance(o)) result.add((T) o);
		}
		return result;
	}

	public static <T> List<T> filterIsInstance(Iterable<?> iterable, Class<T> type) {
		return filterIsInstanceTo(new ArrayList<>(), iterable, type);
	}

	@SuppressWarnings("unchecked")
	public static <T, C extends Collection<T>> C filterIsInstanceTo(C destination, Iterable<?> iterable, Class<T> type) {
		for (Object o : iterable) {
			if (type.isInstance(o)) destination.add((T) o);
		}
		return destination;
	}

	@FunctionalInterface
	public interface IntTBiConsume<T> {
		void get(int index, T t);
	}
}
