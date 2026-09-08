package endfield.util;

import arc.func.Func;
import org.jetbrains.annotations.Nullable;

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
