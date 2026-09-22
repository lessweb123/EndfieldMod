package endfield.util;

import arc.func.Boolf;
import arc.func.Cons;
import arc.util.Eachable;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Array;
import java.util.AbstractQueue;
import java.util.Arrays;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.function.Consumer;

/**
 * Implementation of Java Collection Framework {@code Queue} based on {@code Queue}, used in places that require Java
 * specifications and the feature of {@code Queue} not creating nodes.
 */
public class CollectionQueue<E> extends AbstractQueue<E> implements Eachable<E> {
	public final Class<E> componentType;

	public int size = 0;

	public E[] values;

	protected int head = 0;
	protected int tail = 0;

	protected transient @Nullable QueueIterator iterator1, iterator2;

	public CollectionQueue() {
		this(Object.class);
	}

	public CollectionQueue(Class<?> type) {
		this(16, type);
	}

	@SuppressWarnings("unchecked")
	public CollectionQueue(int initialSize, Class<?> type) {
		componentType = (Class<E>) type;

		values = (E[]) Array.newInstance(type, initialSize);
	}

	public void addLast(E object) {
		E[] vs = values;

		if (size == vs.length) {
			resize(vs.length << 1);// * 2
		}

		vs[tail++] = object;
		if (tail == vs.length) {
			tail = 0;
		}
		size++;
	}

	@Override
	public boolean add(E e) {
		addLast(e);
		return true;
	}

	@Override
	public boolean offer(E e) {
		addLast(e);
		return true;
	}

	@Override
	public E poll() {
		if (size < 1) return null;
		E value = values[0];

		remove(value);

		return value;
	}

	@Override
	public E element() {
		if (size < 1) throw new NoSuchElementException("this values is empty");

		return values[0];
	}

	@Override
	public E peek() {
		if (size < 1) return null;

		return values[0];
	}

	public void addFirst(E object) {
		E[] vs = values;

		if (size == vs.length) {
			resize(vs.length << 1);// * 2
		}

		head--;
		if (head == -1) {
			head = vs.length - 1;
		}
		vs[head] = object;

		size++;
	}

	public E[] shrink() {
		if (values.length != size) resize(size);
		return values;
	}

	public void ensureCapacity(int additional) {
		int needed = size + additional;
		if (values.length < needed) {
			resize(needed);
		}
	}

	@SuppressWarnings("unchecked")
	protected void resize(int newSize) {
		E[] vs = values;
		int h = head;
		int t = tail;

		E[] newArray = (E[]) Array.newInstance(componentType, newSize);
		if (h < t) {
			// Continuous
			System.arraycopy(vs, h, newArray, 0, t - h);
		} else if (size > 0) {
			// Wrapped
			int rest = vs.length - h;
			System.arraycopy(vs, h, newArray, 0, rest);
			System.arraycopy(vs, 0, newArray, rest, t);
		}
		values = newArray;
		head = 0;
		tail = size;
	}

	public E removeFirst() {
		if (size == 0) {
			// Underflow
			throw new NoSuchElementException("Queue is empty.");
		}

		E[] vs = values;

		E result = vs[head];
		vs[head] = null;
		head++;
		if (head == vs.length) {
			head = 0;
		}
		size--;

		return result;
	}

	public E removeLast() {
		if (size == 0) {
			throw new NoSuchElementException("Queue is empty.");
		}

		E[] vs = values;

		tail--;
		if (tail == -1) {
			tail = vs.length - 1;
		}
		E result = vs[tail];
		vs[tail] = null;
		size--;

		return result;
	}

	@Override
	public boolean contains(Object value) {
		return contains(value, true);
	}

	public boolean contains(Object value, boolean identity) {
		return indexOf(value, identity) != -1;
	}

	public int indexOf(Object value, boolean identity) {
		if (size == 0) return -1;
		E[] vs = values;
		if (identity || value == null) {
			if (head < tail) {
				for (int i = head; i < tail; i++)
					if (vs[i] == value) return i - head;
			} else {
				for (int i = head, n = vs.length; i < n; i++)
					if (vs[i] == value) return i - head;
				for (int i = 0; i < tail; i++)
					if (vs[i] == value) return i + vs.length - head;
			}
		} else {
			if (head < tail) {
				for (int i = head; i < tail; i++)
					if (value.equals(vs[i])) return i - head;
			} else {
				for (int i = head, n = vs.length; i < n; i++)
					if (value.equals(vs[i])) return i - head;
				for (int i = 0; i < tail; i++)
					if (value.equals(vs[i])) return i + vs.length - head;
			}
		}
		return -1;
	}

	public int indexOf(Boolf<? super E> value) {
		if (size == 0) return -1;
		E[] vs = values;
		if (head < tail) {
			for (int i = head; i < tail; i++)
				if (value.get(vs[i])) return i - head;
		} else {
			for (int i = head, n = vs.length; i < n; i++)
				if (value.get(vs[i])) return i - head;
			for (int i = 0; i < tail; i++)
				if (value.get(vs[i])) return i + vs.length - head;
		}
		return -1;
	}

	public boolean remove(Boolf<? super E> value) {
		int i = indexOf(value);
		if (i != -1) {
			removeIndex(i);
			return true;
		}
		return false;
	}

	@Override
	public boolean remove(Object value) {
		return remove(value, false);
	}

	public boolean remove(Object value, boolean identity) {
		int index = indexOf(value, identity);
		if (index == -1) return false;
		removeIndex(index);
		return true;
	}

	public E removeIndex(int index) {
		if (index < 0) throw new IndexOutOfBoundsException("index can't be < 0: " + index);
		if (index >= size) throw new IndexOutOfBoundsException("index can't be >= size: " + index + " >= " + size);

		E[] vs = values;
		index += head;
		E value;
		if (head < tail) { // index is between head and tail.
			value = vs[index];
			System.arraycopy(vs, index + 1, vs, index, tail - index);
			vs[tail] = null;
			tail--;
		} else if (index >= vs.length) { // index is between 0 and tail.
			index -= vs.length;
			value = vs[index];
			System.arraycopy(vs, index + 1, vs, index, tail - index);
			tail--;
		} else { // index is between head and values.length.
			value = vs[index];
			System.arraycopy(vs, head, vs, head + 1, index - head);
			vs[head] = null;
			head++;
			if (head == vs.length) {
				head = 0;
			}
		}
		size--;
		return value;
	}

	@Override
	public int size() {
		return size;
	}

	@Override
	public boolean isEmpty() {
		return size == 0;
	}

	public E first() {
		if (size == 0) {
			throw new NoSuchElementException("Queue is empty.");
		}
		return values[head];
	}

	public E last() {
		if (size == 0) {
			throw new NoSuchElementException("Queue is empty.");
		}
		E[] vs = values;
		tail--;
		if (tail == -1) {
			tail = vs.length - 1;
		}
		return vs[tail];
	}

	public E get(int index) {
		if (index < 0) throw new IndexOutOfBoundsException("index can't be < 0: " + index);
		if (index >= size) throw new IndexOutOfBoundsException("index can't be >= size: " + index + " >= " + size);
		E[] vs = values;

		int i = head + index;
		if (i >= vs.length) {
			i -= vs.length;
		}
		return vs[i];
	}

	@Override
	public void clear() {
		if (size == 0) return;
		E[] vs = values;
		int h = head;
		int t = tail;

		if (h < t) {
			for (int i = h; i < t; i++) {
				vs[i] = null;
			}
		} else {
			for (int i = h; i < vs.length; i++) {
				vs[i] = null;
			}
			for (int i = 0; i < t; i++) {
				vs[i] = null;
			}
		}
		head = 0;
		tail = 0;
		size = 0;
	}

	@Override
	public QueueIterator iterator() {
		if (iterator1 == null) iterator1 = new QueueIterator();

		if (iterator1.done) {
			iterator1.index = 0;
			iterator1.done = false;
			return iterator1;
		}

		if (iterator2 == null) iterator2 = new QueueIterator();

		if (iterator2.done) {
			iterator2.index = 0;
			iterator2.done = false;
			return iterator2;
		}
		return new QueueIterator();
	}

	@Override
	public Object[] toArray() {
		Object[] out = new Object[size];
		for (int i = 0; i < size; i++) {
			out[i] = get(i);
		}
		return out;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T[] toArray(T[] a) {
		final int size;
		if ((size = size()) > a.length)
			return toArray((Class<T[]>) a.getClass());
		final E[] es = values;
		for (int i = head, j = 0, len = Math.min(size, es.length - i);
				; i = 0, len = tail) {
			System.arraycopy(es, i, a, j, len);
			if ((j += len) == size) break;
		}
		if (size < a.length)
			a[size] = null;
		return a;
	}

	protected <T> T[] toArray(Class<T[]> c) {
		final E[] es = values;
		final T[] a;
		final int end;
		if ((end = tail + ((head <= tail) ? 0 : es.length)) >= 0) {
			a = Arrays.copyOfRange(es, head, end, c);
		} else {
			a = Arrays.copyOfRange(es, 0, end - head, c);
			System.arraycopy(es, head, a, 0, es.length - head);
		}
		if (end != tail)
			System.arraycopy(es, 0, a, es.length - head, tail);
		return a;
	}

	@Override
	public void each(Cons<? super E> cons) {
		E[] vs = values;

		for (int index = 0; index < size; index++) {
			int i = head + index;
			if (i >= vs.length) {
				i -= vs.length;
			}
			cons.get(vs[i]);
		}
	}

	@Override
	public void forEach(Consumer<? super E> action) {
		E[] vs = values;

		for (int index = 0; index < size; index++) {
			int i = head + index;
			if (i >= vs.length) {
				i -= vs.length;
			}
			action.accept(vs[i]);
		}
	}

	public E find(Boolf<E> func) {
		E[] vs = values;

		for (int index = 0; index < size; index++) {
			int i = head + index;
			if (i >= vs.length) {
				i -= vs.length;
			}
			E value = vs[i];
			if (func.get(value)) {
				return value;
			}
		}
		return null;
	}

	@Override
	public String toString() {
		if (size == 0) {
			return "[]";
		}
		E[] vs = values;
		int h = head;
		int t = tail;

		StringBuilder sb = new StringBuilder(64);
		sb.append('[');
		sb.append(vs[h]);
		for (int i = (h + 1) % vs.length; i != t; i = (i + 1) % vs.length) {
			sb.append(", ").append(vs[i]);
		}
		sb.append(']');
		return sb.toString();
	}

	@Override
	public int hashCode() {
		int s = size;
		E[] vs = values;
		int backingLength = vs.length;
		int index = head;

		int hash = s + 1;
		for (int i = 0; i < s; i++) {
			E value = vs[index];

			hash *= 31;
			if (value != null) hash += value.hashCode();

			index++;
			if (index == backingLength) index = 0;
		}

		return hash;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof CollectionQueue<?> other)) return false;

		if (other.size != size) return false;

		E[] myValues = values;
		int myBackingLength = myValues.length;
		Object[] itsValues = other.values;
		int itsBackingLength = itsValues.length;

		int myIndex = head;
		int itsIndex = other.head;
		for (int s = 0; s < size; s++) {
			E myValue = myValues[myIndex];
			Object itsValue = itsValues[itsIndex];

			if (!(myValue == null ? itsValue == null : myValue.equals(itsValue))) return false;
			myIndex++;
			itsIndex++;
			if (myIndex == myBackingLength) myIndex = 0;
			if (itsIndex == itsBackingLength) itsIndex = 0;
		}
		return true;
	}

	public class QueueIterator implements Iterator<E>, Iterable<E> {
		protected int index;
		protected boolean done = true;

		protected QueueIterator() {}

		@Override
		public boolean hasNext() {
			if (index >= size) done = true;
			return index < size;
		}

		@Override
		public E next() {
			if (index >= size) throw new NoSuchElementException(String.valueOf(index));
			return get(index++);
		}

		@Override
		public void remove() {
			index--;
			removeIndex(index);
		}

		public void reset() {
			index = 0;
		}

		@Override
		public Iterator<E> iterator() {
			return this;
		}
	}
}
