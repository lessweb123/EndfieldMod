package endfield.util;

import arc.func.Cons;
import arc.func.Cons2;
import arc.func.Prov;
import arc.math.Mathf;
import arc.util.ArcRuntimeException;
import arc.util.Eachable;
import endfield.util.holder.ObjectHolder;

import java.lang.reflect.Array;
import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.function.Function;

import static endfield.util.CollectionObjectSet.tableSize;

/**
 * Implementation of Java Collection Framework {@code Map} based on {@code ObjectMap}, used in places that require
 * Java specifications and the feature of {@code ObjectMap} not creating nodes.
 */
public class CollectionObjectMap<K, V> extends AbstractMap<K, V> implements Iterable<ObjectHolder<K, V>>, Eachable<ObjectHolder<K, V>>, Cloneable {
	public int size;

	public final Class<K> keyComponentType;
	public final Class<V> valueComponentType;

	protected K[] keyTable;
	protected V[] valueTable;

	protected float loadFactor;
	protected int threshold;

	protected int shift;

	protected int mask;

	protected transient Entries entries1, entries2;
	protected transient Values values1, values2;
	protected transient Keys keys1, keys2;

	@SuppressWarnings("unchecked")
	public static <K, V> CollectionObjectMap<K, V> of(Class<?> keyType, Class<?> valueType, Object... values) {
		CollectionObjectMap<K, V> map = new CollectionObjectMap<>(keyType, valueType);

		for (int i = 0; i < values.length / 2; i++) {
			map.put((K) values[i * 2], (V) values[i * 2 + 1]);
		}

		return map;
	}

	public CollectionObjectMap() {
		this(Object.class, Object.class);
	}

	public CollectionObjectMap(Class<?> keyType, Class<?> valueType) {
		this(keyType, valueType, 51, 0.8f);
	}

	public CollectionObjectMap(Class<?> keyType, Class<?> valueType, int initialCapacity) {
		this(keyType, valueType, initialCapacity, 0.8f);
	}

	@SuppressWarnings("unchecked")
	public CollectionObjectMap(Class<?> keyType, Class<?> valueType, int initialCapacity, float loadFactor) {
		if (loadFactor <= 0f || loadFactor >= 1f)
			throw new IllegalArgumentException("loadFactor must be > 0 and < 1: " + loadFactor);
		this.loadFactor = loadFactor;

		int tableSize = tableSize(initialCapacity, loadFactor);
		threshold = (int) (tableSize * loadFactor);
		mask = tableSize - 1;
		shift = Long.numberOfLeadingZeros(mask);

		keyComponentType = (Class<K>) keyType;
		valueComponentType = (Class<V>) valueType;

		keyTable = (K[]) Array.newInstance(keyType, tableSize);
		valueTable = (V[]) Array.newInstance(valueType, tableSize);
	}

	public CollectionObjectMap(CollectionObjectMap<? extends K, ? extends V> map) {
		this(map.keyComponentType, map.valueComponentType, (int) (map.keyTable.length * map.loadFactor), map.loadFactor);
		System.arraycopy(map.keyTable, 0, keyTable, 0, map.keyTable.length);
		System.arraycopy(map.valueTable, 0, valueTable, 0, map.valueTable.length);
		size = map.size;
	}

	public CollectionObjectMap(Map<? extends K, ? extends V> map, Class<?> keyType, Class<?> valueType) {
		this(keyType, valueType, map.size());
		putAll(map);
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

	@Override
	public V put(K key, V value) {
		if (key == null) throw new IllegalArgumentException("key cannot be null.");
		int i = locateKey(key);
		if (i >= 0) {
			V oldValue = valueTable[i];
			valueTable[i] = value;
			return oldValue;
		}
		i = -(i + 1);
		keyTable[i] = key;
		valueTable[i] = value;
		if (++size >= threshold) resize(keyTable.length << 1);
		return null;
	}

	public V putMissing(K key, V value) {
		if (key == null) throw new IllegalArgumentException("key cannot be null.");
		int i = locateKey(key);
		if (i >= 0) return valueTable[i];
		i = -(i + 1);
		keyTable[i] = key;
		valueTable[i] = value;
		if (++size >= threshold) resize(keyTable.length << 1);
		return null;
	}

	public V put(Entry<? extends K, ? extends V> entry) {
		if (entry == null) return null;

		return put(entry.getKey(), entry.getValue());
	}

	@SuppressWarnings("unchecked")
	public void putAll(Object... values) {
		for (int i = 0; i < values.length / 2; i++) {
			put((K) values[i * 2], (V) values[i * 2 + 1]);
		}
	}

	public CollectionObjectMap<K, V> merge(CollectionObjectMap<? extends K, ? extends V> map) {
		putAll(map);
		return this;
	}

	public void each(Cons2<? super K, ? super V> cons) {
		for (ObjectHolder<K, V> entry : iterator()) {
			cons.get(entry.key, entry.value);
		}
	}

	@Override
	public void each(Cons<? super ObjectHolder<K, V>> cons) {
		for (ObjectHolder<K, V> entry : iterator()) {
			cons.get(entry);
		}
	}

	@SuppressWarnings("unchecked")
	public CollectionObjectMap<K, V> copy() {
		try {
			CollectionObjectMap<K, V> out = (CollectionObjectMap<K, V>) super.clone();
			out.keyTable = keyTable.clone();
			out.valueTable = valueTable.clone();

			out.entries1 = out.entries2 = null;
			out.values1 = out.values2 = null;
			out.keys1 = out.keys2 = null;

			return out;
		} catch (CloneNotSupportedException e) {
			CollectionObjectMap<K, V> out = new CollectionObjectMap<>(keyComponentType, valueComponentType);
			out.putAll(this);
			return out;
		}
	}

	protected void putResize(K key, V value) {
		K[] ks = keyTable;
		for (int i = place(key); ; i = (i + 1) & mask) {
			if (ks[i] == null) {
				ks[i] = key;
				valueTable[i] = value;
				return;
			}
		}
	}

	public V getThrow(Object key) {
		V value = get(key);
		if (value == null) throw new NoSuchElementException(String.valueOf(key));
		return value;
	}

	public V getThrow(Object key, Prov<? extends RuntimeException> error) {
		if (!containsKey(key)) {
			throw error.get();
		}
		return get(key);
	}

	public V get(K key, Prov<? extends V> supplier) {
		V value = get(key);
		if (value == null) {
			put(key, value = supplier.get());
		}
		return value;
	}

	@Override
	public V computeIfAbsent(K key, Function<? super K, ? extends V> mappingFunction) {
		V value = get(key);
		if (value == null) {
			put(key, value = mappingFunction.apply(key));
		}
		return value;
	}

	@Override
	public V get(Object key) {
		if (key == null) return null;
		int i = locateKey(key);
		return i < 0 ? null : valueTable[i];
	}

	public V get(Object key, V defaultValue) {
		if (key == null) return null;
		int i = locateKey(key);
		return i < 0 ? defaultValue : valueTable[i];
	}

	@Override
	public V remove(Object key) {
		if (key == null) return null;
		int i = locateKey(key);
		if (i < 0) return null;
		K[] ks = keyTable;
		V[] vs = valueTable;
		V oldValue = vs[i];
		int m = mask, next = i + 1 & m;
		K k;
		while ((k = ks[next]) != null) {
			int placement = place(k);
			if ((next - placement & m) > (i - placement & m)) {
				ks[i] = k;
				vs[i] = vs[next];
				i = next;
			}
			next = next + 1 & m;
		}
		ks[i] = null;
		vs[i] = null;
		size--;
		return oldValue;
	}

	@Override
	public void putAll(Map<? extends K, ? extends V> m) {
		ensureCapacity(m.size());
		for (ObjectHolder<K, V> holder : iterator()) {
			put(holder.getKey(), holder.getValue());
		}
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
		Arrays.fill(valueTable, null);
	}

	public boolean containsValue(Object value, boolean identity) {
		V[] vs = valueTable;
		if (value == null) {
			K[] ks = keyTable;
			for (int i = vs.length - 1; i >= 0; i--)
				if (ks[i] != null && vs[i] == null) return true;
		} else if (identity) {
			for (int i = vs.length - 1; i >= 0; i--)
				if (vs[i] == value) return true;
		} else {
			for (int i = vs.length - 1; i >= 0; i--)
				if (value.equals(vs[i])) return true;
		}
		return false;
	}

	@Override
	public boolean containsKey(Object key) {
		return key != null && locateKey(key) >= 0;
	}

	@Override
	public boolean containsValue(Object value) {
		return containsValue(value, false);
	}

	public K findKey(Object value, boolean identity) {
		V[] vs = valueTable;
		if (value == null) {
			K[] ks = keyTable;
			for (int i = vs.length - 1; i >= 0; i--)
				if (ks[i] != null && vs[i] == null) return ks[i];
		} else if (identity) {
			for (int i = vs.length - 1; i >= 0; i--)
				if (vs[i] == value) return keyTable[i];
		} else {
			for (int i = vs.length - 1; i >= 0; i--)
				if (value.equals(vs[i])) return keyTable[i];
		}
		return null;
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

		K[] oldKeyTable = keyTable;
		V[] oldValueTable = valueTable;

		keyTable = (K[]) Array.newInstance(keyComponentType, newSize);
		valueTable = (V[]) Array.newInstance(valueComponentType, newSize);

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
		V[] vs = valueTable;
		for (int i = 0, n = ks.length; i < n; i++) {
			K key = ks[i];
			if (key != null) {
				h += key.hashCode();
				V value = vs[i];
				if (value != null) h += value.hashCode();
			}
		}
		return h;
	}

	@Override
	public boolean equals(Object o) {
		if (o == this) return true;
		if (!(o instanceof Map<?, ?> other)) return false;
		if (other.size() != size) return false;
		K[] ks = keyTable;
		V[] vs = valueTable;
		for (int i = 0, n = ks.length; i < n; i++) {
			K key = ks[i];
			if (key != null) {
				V value = vs[i];
				if (value == null) {
					if (!other.containsKey(key) || other.get(key) != null) return false;
				} else {
					if (!value.equals(other.get(key))) return false;
				}
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
		StringBuilder buffer = new StringBuilder(32);
		if (braces) buffer.append('{');
		K[] ks = keyTable;
		V[] vs = valueTable;
		int i = ks.length;
		while (i-- > 0) {
			K key = ks[i];
			if (key == null) continue;
			buffer.append(key == this ? "(this)" : key);
			buffer.append('=');
			V value = vs[i];
			buffer.append(value == this ? "(this)" : value);
			break;
		}
		while (i-- > 0) {
			K key = ks[i];
			if (key == null) continue;
			buffer.append(separator);
			buffer.append(key == this ? "(this)" : key);
			buffer.append('=');
			V value = vs[i];
			buffer.append(value == this ? "(this)" : value);
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

	@Override
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

	@Override
	public EntrySet entrySet() {
		return new EntrySet();
	}

	@Override
	public Keys keySet() {
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

	public class EntrySet extends AbstractSet<Entry<K, V>> {
		protected final MapItr itr = new MapItr();
		protected final MapEnt ent = new MapEnt();

		@Override
		public int size() {
			return size;
		}

		@Override
		public void clear() {
			CollectionObjectMap.this.clear();
		}

		@Override
		public Iterator<Entry<K, V>> iterator() {
			itr.entries = CollectionObjectMap.this.iterator();
			return itr;
		}

		@Override
		public boolean contains(Object o) {
			if (o instanceof Entry<?, ?> e) {
				Object key = e.getKey();
				return containsKey(key);
			}
			return false;
		}

		@Override
		public boolean remove(Object o) {
			if (o instanceof Entry<?, ?> e) {
				Object key = e.getKey();
				return CollectionObjectMap.this.remove(key) != null;
			}
			return false;
		}

		protected class MapItr implements Iterator<Entry<K, V>> {
			Entries entries;

			@Override
			public boolean hasNext() {
				return entries.hasNext();
			}

			@Override
			public Entry<K, V> next() {
				ent.entry = entries.next();
				return ent;
			}
		}

		protected class MapEnt implements Entry<K, V> {
			ObjectHolder<K, V> entry;

			@Override
			public K getKey() {
				return entry.key;
			}

			@Override
			public V getValue() {
				return entry.value;
			}

			@Override
			public V setValue(V value) {
				return put(entry.key, value);
			}
		}
	}

	protected abstract class MapIterator<E> extends AbstractSet<E> implements Iterator<E> {
		public boolean hasNext;

		public int nextIndex, currentIndex;
		public boolean valid = true;

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

		@Override
		public void remove() {
			int i = currentIndex;
			if (i < 0) throw new IllegalStateException("next must be called before remove.");
			K[] ks = keyTable;
			V[] vs = valueTable;
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
			vs[i] = null;
			size--;
			if (i != currentIndex) --nextIndex;
			currentIndex = -1;
		}

		@Override
		public int size() {
			return size;
		}

		@Override
		public boolean isEmpty() {
			return CollectionObjectMap.this.isEmpty();
		}

		@Override
		public boolean remove(Object o) {
			return CollectionObjectMap.this.remove(o) != null;
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
		public Object[] toArray() {
			return toList().toArray();
		}

		@Override
		public <T> T[] toArray(T[] a) {
			return toList().toArray(a);
		}

		@Override
		public void clear() {
			CollectionObjectMap.this.clear();
		}

		/** Returns a new array containing the remaining keys. */
		public abstract CollectionList<E> toList();

		/** Adds the remaining keys to the array. */
		public CollectionList<E> toList(CollectionList<E> array) {
			while (hasNext)
				array.add(next());
			return array;
		}
	}

	public class Entries extends MapIterator<ObjectHolder<K, V>> {
		protected ObjectHolder<K, V> entry = new ObjectHolder<>();

		@Override
		public ObjectHolder<K, V> next() {
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

		@Override
		public CollectionList<ObjectHolder<K, V>> toList() {
			return toList(new CollectionList<>(size, ObjectHolder.class));
		}
	}

	public class Values extends MapIterator<V> implements Collection<V> {
		@Override
		public boolean hasNext() {
			if (!valid) throw new ArcRuntimeException("#iterator() cannot be used nested.");
			return hasNext;
		}

		@Override
		public V next() {
			if (!hasNext) throw new NoSuchElementException();
			if (!valid) throw new ArcRuntimeException("#iterator() cannot be used nested.");
			V value = valueTable[nextIndex];
			currentIndex = nextIndex;
			findNextIndex();
			return value;
		}

		@Override
		public boolean contains(Object o) {
			return containsValue(o);
		}

		@Override
		public Values iterator() {
			return this;
		}

		@Override
		public CollectionList<V> toList() {
			return toList(new CollectionList<>(size, valueComponentType));
		}
	}

	public class Keys extends MapIterator<K> implements Set<K> {
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
		public int hashCode() {
			int hashCode = 0;
			for (K obj : this) {
				if (obj != null)
					hashCode += obj.hashCode();
			}
			return hashCode;
		}

		@Override
		public boolean contains(Object o) {
			return containsKey(o);
		}

		@Override
		public Keys iterator() {
			return this;
		}

		@Override
		public CollectionList<K> toList() {
			return toList(new CollectionList<>(size, keyComponentType));
		}
	}
}
