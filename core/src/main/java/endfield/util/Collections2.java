package endfield.util;

import arc.func.Boolf;
import arc.func.Cons;
import arc.func.Func;
import arc.util.Eachable;
import arc.util.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

public final class Collections2 {
	private Collections2() {}

	public static boolean contains(Iterable<?> iterator, Object value) {
		for (Object obj : iterator) {
			if (obj == value) return true;
		}
		return false;
	}

	public static <T> T find(Iterable<T> iterator, Boolf<? super T> filler) {
		for (T obj : iterator) {
			if (filler.get(obj)) return obj;
		}
		return null;
	}

	public static <T> boolean any(Iterable<T> iterable, Boolf<? super T> boolf) {
		for (T t : iterable) {
			if (boolf.get(t)) return true;
		}
		return false;
	}

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

	public static <T> Single<T> iter(T item) {
		return new Single<>(item);
	}

	@SuppressWarnings("unchecked")
	public static <T> Iter<T> iter(T... array) {
		return iter(array, 0, array.length);
	}

	public static <T> Iter<T> iter(T[] array, int offset, int length) {
		return new Iter<>(array, offset, length);
	}

	public static <T> Chain<T> chain(Iterator<T> first, Iterator<T> second) {
		return new Chain<>(first, second);
	}

	public static class Single<T> implements Iterable<T>, Iterator<T>, Eachable<T> {
		protected final T item;
		protected boolean done;

		public Single(T t) {
			item = t;
		}

		@Override
		public Single<T> iterator() {
			return this;
		}

		@Override
		public boolean hasNext() {
			return !done;
		}

		@Override
		public T next() {
			if (done) return null;
			done = true;
			return item;
		}

		@Override
		public void each(Cons<? super T> cons) {
			if (!done) cons.get(item);
		}
	}

	public static class Iter<T> implements Iterable<T>, Iterator<T>, Eachable<T> {
		protected final T[] array;
		protected final int offset, length;
		protected int index = 0;

		public Iter(T[] arr, int off, int len) {
			array = arr;
			offset = off;
			length = len;
		}

		public int length() {
			return length;
		}

		public void reset() {
			index = 0;
		}

		@Override
		public Iter<T> iterator() {
			return this;
		}

		@Override
		public boolean hasNext() {
			return index < length - offset;
		}

		@Override
		public T next() {
			return hasNext() ? array[offset + index++] : null;
		}

		@Override
		public void each(Cons<? super T> cons) {
			while (hasNext()) cons.get(array[offset + index++]);
		}
	}

	public static class Chain<T> implements Iterable<T>, Iterator<T>, Eachable<T> {
		protected final Iterator<T> first, second;

		public Chain(Iterator<T> fir, Iterator<T> sec) {
			first = fir;
			second = sec;
		}

		@Override
		public Chain<T> iterator() {
			return this;
		}

		@Override
		public boolean hasNext() {
			return first.hasNext() || second.hasNext();
		}

		@Override
		public T next() {
			return first.hasNext() ? first.next() : second.next();
		}

		@Override
		public void each(Cons<? super T> cons) {
			while (first.hasNext()) cons.get(first.next());
			while (second.hasNext()) cons.get(second.next());
		}
	}

	@FunctionalInterface
	public interface IntTBiConsume<T> {
		void get(int index, T t);
	}
}
