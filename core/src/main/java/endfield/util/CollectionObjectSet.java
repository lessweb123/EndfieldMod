package endfield.util;

import arc.func.Boolf;
import arc.func.Cons;
import arc.math.Mathf;
import arc.util.ArcRuntimeException;
import arc.util.Eachable;

import java.lang.reflect.Array;
import java.util.AbstractSet;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Set;

/**
 * Implementation of Java Collection Framework {@code Set} based on {@code ObjectSet}, used in places that require
 * Java specifications and the feature of {@code ObjectSet} not creating nodes.
 */
public class CollectionObjectSet<E> extends AbstractSet<E> implements Eachable<E>, Cloneable {
	public int size;

	public final Class<E> elementType;

	protected E[] keyTable;

	protected float loadFactor;
	protected int threshold;

	protected int shift;
	protected int mask;

	protected transient Iter iterator1, iterator2;

	public CollectionObjectSet() {
		this(Object.class);
	}

	public CollectionObjectSet(Class<?> type) {
		this(type, 51, 0.8f);
	}

	public CollectionObjectSet(Class<?> type, int initialCapacity) {
		this(type, initialCapacity, 0.8f);
	}

	@SuppressWarnings("unchecked")
	public CollectionObjectSet(Class<?> type, int initialCapacity, float loadFactor) {
		if (loadFactor <= 0f || loadFactor >= 1f)
			throw new IllegalArgumentException("loadFactor must be > 0 and < 1: " + loadFactor);
		this.loadFactor = loadFactor;

		int tableSize = tableSize(initialCapacity, loadFactor);
		threshold = (int) (tableSize * loadFactor);
		mask = tableSize - 1;
		shift = Long.numberOfLeadingZeros(mask);

		elementType = (Class<E>) type;
		keyTable = (E[]) Array.newInstance(type, tableSize);
	}

	public CollectionObjectSet(CollectionObjectSet<? extends E> set) {
		this(set.elementType, (int) (set.keyTable.length * set.loadFactor), set.loadFactor);
		System.arraycopy(set.keyTable, 0, keyTable, 0, set.keyTable.length);
		size = set.size;
	}

	public CollectionObjectSet(Collection<? extends E> collection, Class<?> type) {
		this(type, collection.size());
		addAll(collection);
	}

	@SafeVarargs
	public static <T> CollectionObjectSet<T> with(T... array) {
		CollectionObjectSet<T> set = new CollectionObjectSet<>(array.getClass().getComponentType());
		set.addAll(array);
		return set;
	}

	public static <T> CollectionObjectSet<T> with(CollectionList<T> list) {
		CollectionObjectSet<T> set = new CollectionObjectSet<>(list.componentType);
		set.addAll(list);
		return set;
	}

	/** Allocates a new set with all elements that match the predicate. */
	public CollectionObjectSet<E> select(Boolf<? super E> predicate) {
		CollectionObjectSet<E> arr = new CollectionObjectSet<>(elementType);
		for (E e : this) {
			if (predicate.get(e)) arr.add(e);
		}
		return arr;
	}

	@SuppressWarnings("unchecked")
	public CollectionObjectSet<E> copy() {
		try {
			CollectionObjectSet<E> out = (CollectionObjectSet<E>) super.clone();
			out.keyTable = keyTable.clone();

			out.iterator1 = out.iterator2 = null;

			return out;
		} catch (CloneNotSupportedException e) {
			return new CollectionObjectSet<>(this);
		}
	}

	@Override
	public void each(Cons<? super E> cons) {
		for (E e : this) {
			cons.get(e);
		}
	}

	public E find(Boolf<E> predicate) {
		for (E t : this) {
			if (predicate.get(t)) {
				return t;
			}
		}
		return null;
	}

	protected int place(Object item) {
		return (int) (item.hashCode() * 0x9E3779B97F4A7C15L >>> shift);
	}

	protected int locateKey(Object key) {
		E[] es = keyTable;
		for (int i = place(key); ; i = i + 1 & mask) {
			E other = es[i];
			if (other == null) return -(i + 1);
			if (other.equals(key)) return i;
		}
	}

	public boolean any() {
		return size > 0;
	}

	@Override
	public boolean add(E key) {
		if (key == null) return false;
		int i = locateKey(key);
		if (i >= 0) return false;
		i = -(i + 1);
		keyTable[i] = key;
		if (++size >= threshold) resize(keyTable.length << 1);
		return true;
	}

	public void addAll(CollectionList<? extends E> array) {
		addAll(array.items, 0, array.size);
	}

	public void addAll(CollectionList<? extends E> array, int offset, int length) {
		if (offset + length > array.size)
			throw new IllegalArgumentException("offset + length must be <= size: " + offset + " + " + length + " <= " + array.size);
		addAll(array.items, offset, length);
	}

	@SuppressWarnings("unchecked")
	public void addAll(E... array) {
		addAll(array, 0, array.length);
	}

	public void addAll(E[] array, int offset, int length) {
		ensureCapacity(length);
		for (int i = offset, n = i + length; i < n; i++) {
			E value = array[i];
			if (value != null) add(value);
		}
	}

	public void addAll(Set<? extends E> set) {
		ensureCapacity(set.size());
		for (E key : set)
			add(key);
	}

	protected void addResize(E key) {
		E[] es = keyTable;
		for (int i = place(key); ; i = (i + 1) & mask) {
			if (es[i] == null) {
				es[i] = key;
				return;
			}
		}
	}

	public void removeAll(E[] array, int offset, int length) {
		for (int i = offset, n = i + length; i < n; i++)
			remove(array[i]);
	}

	public void removeAll(E[] array) {
		for (E e : array) {
			remove(e);
		}
	}

	public void removeAll(CollectionList<? extends E> array) {
		removeAll(array.items, 0, array.size);
	}

	@Override
	public boolean remove(Object key) {
		if (key == null) return false;
		int i = locateKey(key);
		if (i < 0) return false;
		E[] ks = keyTable;
		int m = mask, next = i + 1 & m;
		E k;
		while ((k = ks[next]) != null) {
			int placement = place(k);
			if ((next - placement & m) > (i - placement & m)) {
				ks[i] = k;
				i = next;
			}
			next = next + 1 & m;
		}
		ks[i] = null;
		size--;
		return true;
	}

	@Override
	public int size() {
		return size;
	}

	@Override
	public boolean isEmpty() {
		return size == 0;
	}

	public void shrink(int maximumCapacity) {
		if (maximumCapacity < 0) throw new IllegalArgumentException("maximumCapacity must be >= 0: " + maximumCapacity);
		if (size > maximumCapacity) maximumCapacity = size;
		int tableSize = tableSize(maximumCapacity, loadFactor);
		if (keyTable.length > tableSize) resize(tableSize);
	}

	public void clear(int maximumCapacity) {
		int tableSize = tableSize(maximumCapacity, loadFactor);
		if (keyTable.length <= tableSize) {
			clear();
			return;
		}
		size = 0;
		resize(tableSize);
	}

	@Override
	public void clear() {
		if (size == 0) return;
		size = 0;
		Arrays.fill(keyTable, null);
	}

	@Override
	public boolean contains(Object key) {
		if (key == null) return false;
		return locateKey(key) >= 0;
	}

	public E get(Object key) {
		if (key == null) return null;
		int i = locateKey(key);
		return i < 0 ? null : keyTable[i];
	}

	public E first() {
		E[] ks = keyTable;
		for (E k : ks) {
			if (k != null) return k;
		}
		throw new IllegalStateException("ObjectSet is empty.");
	}

	public void ensureCapacity(int additionalCapacity) {
		if (additionalCapacity < 0)
			throw new IllegalArgumentException("additionalCapacity must be >= 0: " + additionalCapacity);
		int sizeNeeded = size + additionalCapacity;
		if (sizeNeeded >= threshold) resize(Mathf.nextPowerOfTwo((int) Math.ceil(sizeNeeded / loadFactor)));
	}

	@SuppressWarnings("unchecked")
	protected void resize(int newSize) {
		int oldCapacity = keyTable.length;
		threshold = (int) (newSize * loadFactor);
		mask = newSize - 1;
		shift = Long.numberOfLeadingZeros(mask);
		E[] oldKeyTable = keyTable;

		keyTable = (E[]) Array.newInstance(elementType, newSize);

		if (size > 0) {
			for (int i = 0; i < oldCapacity; i++) {
				E key = oldKeyTable[i];
				if (key != null) addResize(key);
			}
		}
	}

	@Override
	public int hashCode() {
		int h = size;
		E[] ks = keyTable;
		for (E key : ks) {
			if (key != null) h += key.hashCode();
		}
		return h;
	}

	@Override
	public boolean equals(Object o) {
		if (!(o instanceof Set<?> other)) return false;
		if (other.size() != size) return false;
		E[] ks = keyTable;
		for (E k : ks) {
			if (k != null && !other.contains(k)) return false;
		}
		return true;
	}

	@Override
	public String toString() {
		if (size == 0) return "[]";
		StringBuilder buffer = new StringBuilder(32);
		int i = keyTable.length;
		while (i-- > 0) {
			E key = keyTable[i];
			if (key == null) continue;
			buffer.append(key);
			break;
		}
		while (i-- > 0) {
			E key = keyTable[i];
			if (key == null) continue;
			buffer.append(", ");
			buffer.append(key);
		}
		return buffer.toString();
	}

	@Override
	public Iter iterator() {
		if (iterator1 == null) {
			iterator1 = new Iter();
			iterator2 = new Iter();
		}
		if (!iterator1.valid) {
			iterator1.reset();
			iterator1.valid = true;
			iterator2.valid = false;
			return iterator1;
		}
		iterator2.reset();
		iterator2.valid = true;
		iterator1.valid = false;
		return iterator2;
	}

	static int tableSize(int capacity, float loadFactor) {
		if (capacity < 0) throw new IllegalArgumentException("capacity must be >= 0: " + capacity);
		int tableSize = Mathf.nextPowerOfTwo(Math.max(2, (int) Math.ceil(capacity / loadFactor)));
		if (tableSize > 1 << 30) throw new IllegalArgumentException("The required capacity is too large: " + capacity);
		return tableSize;
	}

	public class Iter implements Iterable<E>, Iterator<E> {
		public boolean hasNext;

		public int nextIndex, currentIndex;
		public boolean valid = true;

		public Iter() {
			reset();
		}

		public void reset() {
			currentIndex = -1;
			nextIndex = -1;
			findNextIndex();
		}

		protected void findNextIndex() {
			E[] ks = keyTable;
			for (int n = keyTable.length; ++nextIndex < n; ) {
				if (ks[nextIndex] != null) {
					hasNext = true;
					return;
				}
			}
			hasNext = false;
		}

		@Override
		public void remove() {
			int i = currentIndex;
			if (i < 0) throw new IllegalStateException("next must be called before remove.");
			E[] ks = keyTable;
			int m = mask, next = i + 1 & m;
			E key;
			while ((key = ks[next]) != null) {
				int placement = place(key);
				if ((next - placement & m) > (i - placement & m)) {
					ks[i] = key;
					i = next;
				}
				next = next + 1 & m;
			}
			ks[i] = null;
			size--;
			if (i != currentIndex) --nextIndex;
			currentIndex = -1;
		}

		@Override
		public boolean hasNext() {
			if (!valid) throw new ArcRuntimeException("#iterator() cannot be used nested.");
			return hasNext;
		}

		@Override
		public E next() {
			if (!hasNext) throw new NoSuchElementException();
			E key = keyTable[nextIndex];
			currentIndex = nextIndex;
			findNextIndex();
			return key;
		}

		@Override
		public Iter iterator() {
			return this;
		}

		public CollectionList<E> toList(CollectionList<E> array) {
			while (hasNext)
				array.add(next());
			return array;
		}

		public CollectionList<E> toList() {
			return toList(new CollectionList<>(size, elementType));
		}
	}
}
