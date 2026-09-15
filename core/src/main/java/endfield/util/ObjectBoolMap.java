package endfield.util;

import arc.func.Cons;
import arc.struct.BoolSeq;
import arc.util.ArcRuntimeException;
import arc.util.Eachable;
import endfield.util.holder.ObjectBoolHolder;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Iterator;
import java.util.NoSuchElementException;

import static endfield.util.CollectionObjectSet.tableSize;

public class ObjectBoolMap<K> implements Iterable<ObjectBoolHolder<K>>, Eachable<ObjectBoolHolder<K>>, Cloneable {
	public int size;

	public final Class<K> keyComponentType;

	protected K[] keyTable;
	protected boolean[] valueTable;

	protected float loadFactor;
	protected int threshold;

	protected int shift;

	protected int mask;

	protected transient Entries entries1, entries2;
	protected transient Values values1, values2;
	protected transient Keys keys1, keys2;

	public ObjectBoolMap() {
		this(Object.class);
	}

	public ObjectBoolMap(Class<?> keyType) {
		this(keyType, 51, 0.8f);
	}

	public ObjectBoolMap(Class<?> keyType, int initialCapacity) {
		this(keyType, initialCapacity, 0.8f);
	}

	@SuppressWarnings("unchecked")
	public ObjectBoolMap(Class<?> keyType, int initialCapacity, float loadFactor) {
		if (loadFactor <= 0f || loadFactor >= 1f)
			throw new IllegalArgumentException("loadFactor must be > 0 and < 1: " + loadFactor);
		this.loadFactor = loadFactor;

		int tableSize = tableSize(initialCapacity, loadFactor);
		threshold = (int) (tableSize * loadFactor);
		mask = tableSize - 1;
		shift = Long.numberOfLeadingZeros(mask);

		keyComponentType = (Class<K>) keyType;

		keyTable = (K[]) Array.newInstance(keyType, tableSize);
		valueTable = new boolean[tableSize];
	}

	public ObjectBoolMap(ObjectBoolMap<? extends K> map) {
		this(map.keyComponentType, (int) (map.keyTable.length * map.loadFactor), map.loadFactor);
		System.arraycopy(map.keyTable, 0, keyTable, 0, map.keyTable.length);
		System.arraycopy(map.valueTable, 0, valueTable, 0, map.valueTable.length);
		size = map.size;
	}

	@Override
	public void each(Cons<? super ObjectBoolHolder<K>> cons) {
		for (ObjectBoolHolder<K> entry : entries()) {
			cons.get(entry);
		}
	}

	protected int place(Object item) {
		return (int) (item.hashCode() * 0x9E3779B97F4A7C15L >>> shift);
	}

	protected int locateKey(Object key) {
		K[] ks = keyTable;
		for (int i = place(key); ; i = i + 1 & mask) {
			K other = ks[i];
			if (other == null) return -(i + 1);
			if (other.equals(key)) return i;
		}
	}

	public boolean put(K key, boolean value) {
		if (key == null) throw new IllegalArgumentException("key cannot be null.");
		int i = locateKey(key);
		if (i >= 0) {
			boolean oldValue = valueTable[i];
			valueTable[i] = value;
			return oldValue;
		}
		i = -(i + 1);
		keyTable[i] = key;
		valueTable[i] = value;
		if (++size >= threshold) resize(keyTable.length << 1);
		return false;
	}

	public void putMissing(K key, boolean value) {
		if (key == null) throw new IllegalArgumentException("key cannot be null.");
		int i = locateKey(key);
		if (i >= 0) return;
		i = -(i + 1);
		keyTable[i] = key;
		valueTable[i] = value;
		if (++size >= threshold) resize(keyTable.length << 1);
	}

	public boolean putMissing(K key, boolean value, boolean defaultValue) {
		if (key == null) throw new IllegalArgumentException("key cannot be null.");
		int i = locateKey(key);
		if (i >= 0) return valueTable[i];
		i = -(i + 1);
		keyTable[i] = key;
		valueTable[i] = value;
		if (++size >= threshold) resize(keyTable.length << 1);
		return defaultValue;
	}

	public void putAll(ObjectBoolMap<? extends K> map) {
		for (ObjectBoolHolder<? extends K> entry : map.entries())
			put(entry.key, entry.value);
	}

	@SuppressWarnings("unchecked")
	public void putAll(Object... values) {
		for (int i = 0; i < values.length / 2; i++) {
			put((K) values[i * 2], (boolean) values[i * 2 + 1]);
		}
	}

	@SuppressWarnings("unchecked")
	public ObjectBoolMap<K> copy() {
		try {
			ObjectBoolMap<K> out = (ObjectBoolMap<K>) super.clone();
			out.keyTable = keyTable.clone();
			out.valueTable = valueTable.clone();

			out.entries1 = out.entries2 = null;
			out.values1 = out.values2 = null;
			out.keys1 = out.keys2 = null;

			return out;
		} catch (CloneNotSupportedException e) {
			return new ObjectBoolMap<>(this);
		}
	}

	protected void putResize(K key, boolean value) {
		K[] ks = keyTable;
		for (int i = place(key); ; i = (i + 1) & mask) {
			if (ks[i] == null) {
				ks[i] = key;
				valueTable[i] = value;
				return;
			}
		}
	}

	public boolean get(Object key) {
		return get(key, false);
	}

	public boolean get(Object key, boolean defaultValue) {
		if (key == null) return defaultValue;
		int i = locateKey(key);
		return i < 0 ? defaultValue : valueTable[i];
	}

	public boolean remove(K key) {
		return remove(key, false);
	}

	public boolean remove(K key, boolean defaultValue) {
		if (key == null) return defaultValue;
		int i = locateKey(key);
		if (i < 0) return defaultValue;
		K[] ks = keyTable;
		boolean[] vs = valueTable;
		boolean oldValue = vs[i];
		int m = mask, next = i + 1 & m;
		while ((key = ks[next]) != null) {
			int placement = place(key);
			if ((next - placement & m) > (i - placement & m)) {
				ks[i] = key;
				vs[i] = vs[next];
				i = next;
			}
			next = next + 1 & m;
		}
		ks[i] = null;
		size--;
		return oldValue;
	}

	public boolean isNotEmpty() {
		return size > 0;
	}

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

	public void clear() {
		if (size == 0) return;
		size = 0;
		Arrays.fill(keyTable, null);
	}

	public boolean containsValue(boolean value) {
		K[] ks = keyTable;
		boolean[] vs = valueTable;
		for (int i = vs.length - 1; i >= 0; i--)
			if (ks[i] != null && vs[i] == value) return true;
		return false;
	}

	public boolean containsKey(Object key) {
		return key != null && locateKey(key) >= 0;
	}

	public K findKey(boolean value) {
		K[] ks = keyTable;
		boolean[] vs = valueTable;
		for (int i = vs.length - 1; i >= 0; i--) {
			K key = ks[i];
			if (key != null && vs[i] == value) return key;
		}
		return null;
	}

	public void ensureCapacity(int additionalCapacity) {
		int tableSize = tableSize(size + additionalCapacity, loadFactor);
		if (keyTable.length < tableSize) resize(tableSize);
	}

	@SuppressWarnings("unchecked")
	protected void resize(int newSize) {
		int oldCapacity = keyTable.length;
		threshold = (int) (newSize * loadFactor);
		mask = newSize - 1;
		shift = Long.numberOfLeadingZeros(mask);

		K[] oldKeyTable = keyTable;
		boolean[] oldValueTable = valueTable;

		keyTable = (K[]) Array.newInstance(keyComponentType, newSize);
		valueTable = new boolean[newSize];

		if (size > 0) {
			for (int i = 0; i < oldCapacity; i++) {
				K key = oldKeyTable[i];
				if (key != null) putResize(key, oldValueTable[i]);
			}
		}
	}

	@Override
	public int hashCode() {
		int h = size;
		K[] ks = keyTable;
		boolean[] vs = valueTable;
		for (int i = 0, n = ks.length; i < n; i++) {
			K key = ks[i];
			if (key != null) h += key.hashCode() + Boolean.hashCode(vs[i]);
		}
		return h;
	}

	@Override
	public boolean equals(Object o) {
		if (o == this) return true;
		if (!(o instanceof ObjectBoolMap<?> other)) return false;
		if (other.size != size) return false;
		K[] ks = keyTable;
		boolean[] vs = valueTable;
		for (int i = 0, n = ks.length; i < n; i++) {
			K key = ks[i];
			if (key != null) {
				boolean otherValue = other.get(key, false);
				if (!other.containsKey(key)) return false;
				if (otherValue != vs[i]) return false;
			}
		}
		return true;
	}

	public String toString(String separator) {
		return toString(separator, false);
	}

	@Override
	public String toString() {
		return toString(", ", true);
	}

	public String toString(String separator, boolean braces) {
		if (size == 0) return braces ? "{}" : "";
		java.lang.StringBuilder buffer = new java.lang.StringBuilder(32);
		if (braces) buffer.append('{');
		K[] ks = keyTable;
		boolean[] vs = valueTable;
		int i = ks.length;
		while (i-- > 0) {
			K key = ks[i];
			if (key == null) continue;
			buffer.append(key);
			buffer.append('=');
			buffer.append(vs[i]);
			break;
		}
		while (i-- > 0) {
			K key = ks[i];
			if (key == null) continue;
			buffer.append(separator);
			buffer.append(key);
			buffer.append('=');
			buffer.append(vs[i]);
		}
		if (braces) buffer.append('}');
		return buffer.toString();
	}

	@Override
	public Entries iterator() {
		return entries();
	}

	public Entries entries() {
		if (entries1 == null) {
			entries1 = new Entries();
			entries2 = new Entries();
		}
		if (!entries1.valid) {
			entries1.reset();
			entries1.valid = true;
			entries2.valid = false;
			return entries1;
		}
		entries2.reset();
		entries2.valid = true;
		entries1.valid = false;
		return entries2;
	}

	public Values values() {
		if (values1 == null) {
			values1 = new Values();
			values2 = new Values();
		}
		if (!values1.valid) {
			values1.reset();
			values1.valid = true;
			values2.valid = false;
			return values1;
		}
		values2.reset();
		values2.valid = true;
		values1.valid = false;
		return values2;
	}

	public Keys keys() {
		if (keys1 == null) {
			keys1 = new Keys();
			keys2 = new Keys();
		}
		if (!keys1.valid) {
			keys1.reset();
			keys1.valid = true;
			keys2.valid = false;
			return keys1;
		}
		keys2.reset();
		keys2.valid = true;
		keys1.valid = false;
		return keys2;
	}

	protected class MapIterator {
		public boolean hasNext;

		protected int nextIndex, currentIndex;
		protected boolean valid = true;

		public MapIterator() {
			reset();
		}

		public void reset() {
			currentIndex = -1;
			nextIndex = -1;
			findNextIndex();
		}

		protected void findNextIndex() {
			K[] ks = keyTable;
			for (int n = ks.length; ++nextIndex < n; ) {
				if (ks[nextIndex] != null) {
					hasNext = true;
					return;
				}
			}
			hasNext = false;
		}

		public void remove() {
			int i = currentIndex;
			if (i < 0) throw new IllegalStateException("next must be called before remove.");
			K[] ks = keyTable;
			boolean[] vs = valueTable;
			int m = mask, next = i + 1 & m;
			K key;
			while ((key = ks[next]) != null) {
				int placement = place(key);
				if ((next - placement & m) > (i - placement & m)) {
					ks[i] = key;
					vs[i] = vs[next];
					i = next;
				}
				next = next + 1 & m;
			}
			ks[i] = null;
			size--;
			if (i != currentIndex) --nextIndex;
			currentIndex = -1;
		}
	}

	public class Entries extends MapIterator implements Iterable<ObjectBoolHolder<K>>, Iterator<ObjectBoolHolder<K>> {
		protected ObjectBoolHolder<K> entry = new ObjectBoolHolder<>();

		public CollectionList<ObjectBoolHolder<K>> toList() {
			CollectionList<ObjectBoolHolder<K>> out = new CollectionList<>(keyComponentType);
			for (ObjectBoolHolder<K> entry : this) {
				ObjectBoolHolder<K> e = new ObjectBoolHolder<>();
				e.key = entry.key;
				e.value = entry.value;
				out.add(e);
			}
			return out;
		}

		@Override
		public ObjectBoolHolder<K> next() {
			if (!hasNext) throw new NoSuchElementException();
			if (!valid) throw new ArcRuntimeException("#iterator() cannot be used nested.");
			entry.key = keyTable[nextIndex];
			entry.value = valueTable[nextIndex];
			currentIndex = nextIndex;
			findNextIndex();
			return entry;
		}

		@Override
		public boolean hasNext() {
			if (!valid) throw new ArcRuntimeException("#iterator() cannot be used nested.");
			return hasNext;
		}

		@Override
		public Entries iterator() {
			return this;
		}
	}

	public class Values extends MapIterator {
		public boolean hasNext() {
			if (!valid) throw new ArcRuntimeException("#iterator() cannot be used nested.");
			return hasNext;
		}

		public boolean next() {
			if (!hasNext) throw new NoSuchElementException();
			if (!valid) throw new ArcRuntimeException("#iterator() cannot be used nested.");
			boolean value = valueTable[nextIndex];
			currentIndex = nextIndex;
			findNextIndex();
			return value;
		}

		public BoolSeq toSeq() {
			BoolSeq array = new BoolSeq(true, size);
			while (hasNext)
				array.add(next());
			return array;
		}

		public boolean[] toArray() {
			boolean[] array = new boolean[ObjectBoolMap.this.size];
			int i = 0;
			while (hasNext) {
				array[i] = next();
				i++;
			}
			return array;
		}
	}

	public class Keys extends MapIterator implements Iterable<K>, Iterator<K> {
		@Override
		public boolean hasNext() {
			if (!valid) throw new ArcRuntimeException("#iterator() cannot be used nested.");
			return hasNext;
		}

		@Override
		public K next() {
			if (!hasNext) throw new NoSuchElementException();
			if (!valid) throw new ArcRuntimeException("#iterator() cannot be used nested.");
			K key = keyTable[nextIndex];
			currentIndex = nextIndex;
			findNextIndex();
			return key;
		}

		@Override
		public Keys iterator() {
			return this;
		}

		public CollectionList<K> toList() {
			CollectionList<K> array = new CollectionList<>(size, keyComponentType);
			while (hasNext)
				array.add(next());
			return array;
		}

		public CollectionList<K> toList(CollectionList<K> array) {
			while (hasNext)
				array.add(next());
			return array;
		}
	}
}
