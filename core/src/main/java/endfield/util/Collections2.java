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

	public static <T> List<T> plus(Collection<T> ca, Collection<T> cb) {
		ArrayList<T> result = new ArrayList<>((ca == null ? 0 : ca.size()) + (cb == null ? 0 : cb.size()));

		if (ca != null) result.addAll(ca);
		if (cb != null) result.addAll(cb);

		return result;
	}

	public static <T> List<T> plus(Collection<T> ca, Collection<T> cb, Collection<T> cc) {
		ArrayList<T> result = new ArrayList<>((ca == null ? 0 : ca.size()) + (cb == null ? 0 : cb.size()) + (cc == null ? 0 : cc.size()));

		if (ca != null) result.addAll(ca);
		if (cb != null) result.addAll(cb);
		if (cc != null) result.addAll(cc);

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
