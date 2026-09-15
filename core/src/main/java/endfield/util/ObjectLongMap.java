package endfield.util;

import arc.func.Cons;
import arc.struct.LongSeq;
import arc.struct.Seq;
import arc.util.ArcRuntimeException;
import arc.util.Eachable;
import endfield.util.holder.ObjectLongHolder;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Iterator;
import java.util.NoSuchElementException;

import static endfield.util.CollectionObjectSet.tableSize;

public class ObjectLongMap<K> implements Iterable<ObjectLongHolder<K>>, Eachable<ObjectLongHolder<K>>, Cloneable {
	public int size;

	public final Class<K> keyComponentType;

	protected K[] keyTable;
	protected long[] valueTable;

	protected float loadFactor;
	protected int threshold;

	protected int shift;

	protected int mask;

	protected transient Entries entries1, entries2;
	protected transient Values values1, values2;
	protected transient Keys keys1, keys2;

	public ObjectLongMap() {
		this(Object.class);
	}

	public ObjectLongMap(Class<?> keyType) {
		this(keyType, 51, 0.8f);
	}

	public ObjectLongMap(Class<?> keyType, int initialCapacity) {
		this(keyType, initialCapacity, 0.8f);
	}

	@SuppressWarnings("unchecked")
	public ObjectLongMap(Class<?> keyType, int initialCapacity, float loadFactor) {
		if (loadFactor <= 0f || loadFactor >= 1f)
			throw new IllegalArgumentException("loadFactor must be > 0 and < 1: " + loadFactor);
		this.loadFactor = loadFactor;

		int tableSize = tableSize(initialCapacity, loadFactor);
		threshold = (int) (tableSize * loadFactor);
		mask = tableSize - 1;
		shift = Long.numberOfLeadingZeros(mask);

		keyComponentType = (Class<K>) keyType;

		keyTable = (K[]) Array.newInstance(keyType, tableSize);
		valueTable = new long[tableSize];
	}

	public ObjectLongMap(ObjectLongMap<? extends K> map) {
		this(map.keyComponentType, (int) (map.keyTable.length * map.loadFactor), map.loadFactor);
		System.arraycopy(map.keyTable, 0, keyTable, 0, map.keyTable.length);
		System.arraycopy(map.valueTable, 0, valueTable, 0, map.valueTable.length);
		size = map.size;
	}

	@SuppressWarnings("unchecked")
	public ObjectLongMap<K> copy() {
		try {
			ObjectLongMap<K> out = (ObjectLongMap<K>) super.clone();
			out.keyTable = keyTable.clone();
			out.valueTable = valueTable.clone();

			out.entries1 = out.entries2 = null;
			out.values1 = out.values2 = null;
			out.keys1 = out.keys2 = null;

			return out;
		} catch (CloneNotSupportedException e) {
			return new ObjectLongMap<>(this);
		}
	}

	@Override
	public void each(Cons<? super ObjectLongHolder<K>> cons) {
		for (ObjectLongHolder<K> entry : entries()) {
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

	public void put(K key, long value) {
		if (key == null) throw new IllegalArgumentException("key cannot be null.");
		int i = locateKey(key);
		if (i >= 0) {
			valueTable[i] = value;
			return;
		}
		i = -(i + 1);
		keyTable[i] = key;
		valueTable[i] = value;
		if (++size >= threshold) resize(keyTable.length << 1);
	}

	public long put(K key, long value, long defaultValue) {
		if (key == null) throw new IllegalArgumentException("key cannot be null.");
		int i = locateKey(key);
		if (i >= 0) {
			long oldValue = valueTable[i];
			valueTable[i] = value;
			return oldValue;
		}
		i = -(i + 1);
		keyTable[i] = key;
		valueTable[i] = value;
		if (++size >= threshold) resize(keyTable.length << 1);
		return defaultValue;
	}

	public void putMissing(K key, long value) {
		if (key == null) throw new IllegalArgumentException("key cannot be null.");
		int i = locateKey(key);
		if (i >= 0) return;
		i = -(i + 1);
		keyTable[i] = key;
		valueTable[i] = value;
		if (++size >= threshold) resize(keyTable.length << 1);
	}

	public long putMissing(K key, long value, long defaultValue) {
		if (key == null) throw new IllegalArgumentException("key cannot be null.");
		int i = locateKey(key);
		if (i >= 0) return valueTable[i];
		i = -(i + 1);
		keyTable[i] = key;
		valueTable[i] = value;
		if (++size >= threshold) resize(keyTable.length << 1);
		return defaultValue;
	}

	public void putAll(ObjectLongMap<? extends K> map) {
		ensureCapacity(map.size);
		K[] keyTable = map.keyTable;
		long[] valueTable = map.valueTable;
		K key;
		for (int i = 0, n = keyTable.length; i < n; i++) {
			key = keyTable[i];
			if (key != null) put(key, valueTable[i]);
		}
	}

	@SuppressWarnings("unchecked")
	public void putAll(Object... values) {
		for (int i = 0; i < values.length / 2; i++) {
			put((K) values[i * 2], (long) values[i * 2 + 1]);
		}
	}

	protected void putResize(K key, long value) {
		K[] ks = keyTable;
		for (int i = place(key); ; i = (i + 1) & mask) {
			if (ks[i] == null) {
				ks[i] = key;
				valueTable[i] = value;
				return;
			}
		}
	}

	public long get(Object key) {
		return get(key, 0);
	}

	public long get(Object key, long defaultValue) {
		if (key == null) return defaultValue;
		int i = locateKey(key);
		return i < 0 ? defaultValue : valueTable[i];
	}

	public long increment(K key) {
		return increment(key, 0, 1);
	}

	public long increment(K key, long amount) {
		return increment(key, 0, amount);
	}

	public long increment(K key, long defaultValue, long increment) {
		if (key == null) throw new IllegalArgumentException("key cannot be null.");
		int i = locateKey(key);
		if (i >= 0) { // Existing key was found.
			long oldValue = valueTable[i];
			valueTable[i] += increment;
			return oldValue;
		}
		i = -(i + 1); // Empty space was found.
		keyTable[i] = key;
		valueTable[i] = defaultValue + increment;
		if (++size >= threshold) resize(keyTable.length << 1);
		return defaultValue;
	}

	public long remove(K key) {
		return remove(key, 0);
	}

	public long remove(K key, long defaultValue) {
		if (key == null) throw new IllegalArgumentException("key cannot be null.");
		int i = locateKey(key);
		if (i < 0) return defaultValue;
		K[] ks = keyTable;
		long[] vs = valueTable;
		long oldValue = vs[i];
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

	public boolean containsValue(long value) {
		K[] ks = keyTable;
		long[] vs = valueTable;
		for (int i = vs.length - 1; i >= 0; i--)
			if (ks[i] != null && vs[i] == value) return true;
		return false;
	}

	public boolean containsKey(Object key) {
		return key != null && locateKey(key) >= 0;
	}

	public K findKey(long value) {
		K[] ks = keyTable;
		long[] vs = valueTable;
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
		long[] oldValueTable = valueTable;

		keyTable = (K[]) Array.newInstance(keyComponentType, newSize);
		valueTable = new long[newSize];

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
		long[] vs = valueTable;
		for (int i = 0, n = ks.length; i < n; i++) {
			K key = ks[i];
			if (key != null) h += key.hashCode() + Long.hashCode(vs[i]);
		}
		return h;
	}

	@Override
	public boolean equals(Object o) {
		if (o == this) return true;
		if (!(o instanceof ObjectLongMap<?> other)) return false;
		if (other.size != size) return false;
		K[] ks = keyTable;
		long[] vs = valueTable;
		for (int i = 0, n = ks.length; i < n; i++) {
			K key = ks[i];
			if (key != null) {
				long otherValue = other.get(key, 0);
				if (otherValue == 0 && !other.containsKey(key)) return false;
				if (otherValue != vs[i]) return false;
			}
		}
		return true;
	}

	public String toString(String separator) {
		return toString(separator, false);
	}

	public String toString() {
		return toString(", ", true);
	}

	public String toString(String separator, boolean braces) {
		if (size == 0) return braces ? "{}" : "";
		java.lang.StringBuilder buffer = new java.lang.StringBuilder(32);
		if (braces) buffer.append('{');
		K[] ks = keyTable;
		long[] vs = valueTable;
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
			long[] vs = valueTable;
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

	public class Entries extends MapIterator implements Iterable<ObjectLongHolder<K>>, Iterator<ObjectLongHolder<K>> {
		protected ObjectLongHolder<K> entry = new ObjectLongHolder<>();

		public CollectionList<ObjectLongHolder<K>> toList() {
			CollectionList<ObjectLongHolder<K>> out = new CollectionList<>(keyComponentType);
			for (ObjectLongHolder<K> entry : this) {
				ObjectLongHolder<K> e = new ObjectLongHolder<>();
				e.key = entry.key;
				e.value = entry.value;
				out.add(e);
			}
			return out;
		}

		@Override
		public ObjectLongHolder<K> next() {
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

		public long next() {
			if (!hasNext) throw new NoSuchElementException();
			if (!valid) throw new ArcRuntimeException("#iterator() cannot be used nested.");
			long value = valueTable[nextIndex];
			currentIndex = nextIndex;
			findNextIndex();
			return value;
		}

		public LongSeq toSeq() {
			LongSeq array = new LongSeq(true, size);
			while (hasNext)
				array.add(next());
			return array;
		}

		public long[] toArray() {
			long[] array = new long[ObjectLongMap.this.size];
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

		public Seq<K> toSeq() {
			Seq<K> seq = new Seq<>(true, size, keyComponentType);
			while (hasNext)
				seq.add(next());
			return seq;
		}

		public Seq<K> toSeq(Seq<K> seq) {
			while (hasNext)
				seq.add(next());
			return seq;
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
