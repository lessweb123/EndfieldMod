package endfield.util;

import arc.func.Cons;
import arc.func.Cons2;
import arc.math.Mathf;
import arc.util.ArcRuntimeException;
import arc.util.Eachable;
import endfield.util.holder.ObjectHolder;

import java.lang.reflect.Array;
import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

/**
 * Implementation of Java Collection Framework {@code Map} based on {@code ArrayMap}, used in places that require
 * Java specifications and the feature of {@code ArrayMap} not creating nodes.
 */
public class CollectionArrayMap<K, V> extends AbstractMap<K, V> implements Iterable<ObjectHolder<K, V>>, Eachable<ObjectHolder<K, V>>, Cloneable {
	public final Class<K> keyComponentType;
	public final Class<V> valueComponentType;

	public K[] keys;
	public V[] values;
	public int size;
	public boolean ordered;

	protected transient Entries entries1, entries2;
	protected transient Values valuesIter1, valuesIter2;
	protected transient Keys keysIter1, keysIter2;

	public CollectionArrayMap() {
		this(Object.class, Object.class);
	}

	public CollectionArrayMap(Class<?> keyType, Class<?> valueType) {
		this(true, 16, keyType, valueType);
	}

	public CollectionArrayMap(int capacity, Class<?> keyType, Class<?> valueType) {
		this(true, capacity, keyType, valueType);
	}

	@SuppressWarnings("unchecked")
	public CollectionArrayMap(boolean ordered, int capacity, Class<?> keyType, Class<?> valueType) {
		this.ordered = ordered;

		keyComponentType = (Class<K>) keyType;
		valueComponentType = (Class<V>) valueType;

		keys = (K[]) Array.newInstance(keyType, capacity);
		values = (V[]) Array.newInstance(valueType, capacity);
	}

	public CollectionArrayMap(CollectionArrayMap<? extends K, ? extends V> array) {
		this(array.ordered, array.size, array.keyComponentType, array.valueComponentType);
		size = array.size;
		System.arraycopy(array.keys, 0, keys, 0, size);
		System.arraycopy(array.values, 0, values, 0, size);
	}

	public CollectionArrayMap(Map<? extends K, ? extends V> map, Class<?> keyType, Class<?> valueType) {
		this(map.size(), keyType, valueType);
		putAll(map);
	}

	@SuppressWarnings("unchecked")
	public CollectionArrayMap<K, V> copy() {
		try {
			CollectionArrayMap<K, V> out = (CollectionArrayMap<K, V>) super.clone();
			out.size = size;
			out.keys = keys.clone();
			out.values = values.clone();

			out.entries1 = out.entries2 = null;
			out.valuesIter1 = out.valuesIter2 = null;
			out.keysIter1 = out.keysIter2 = null;

			return out;
		} catch (CloneNotSupportedException e) {
			return new CollectionArrayMap<>(this);
		}
	}

	public void each(Cons2<? super K, ? super V> cons) {
		for (ObjectHolder<K, V> entry : entries()) {
			cons.get(entry.getKey(), entry.getValue());
		}
	}

	@Override
	public void each(Cons<? super ObjectHolder<K, V>> cons) {
		for (ObjectHolder<K, V> entry : entries()) {
			cons.get(entry);
		}
	}

	@Override
	public V put(K key, V value) {
		if (key == null) return null;

		int index = indexOfKey(key);
		if (index == -1) {
			if (size == keys.length) resize(Math.max(8, (int) (size * 1.75f)));
			index = size++;
		}
		keys[index] = key;
		values[index] = value;
		return value;
	}

	@Override
	public V remove(Object key) {
		return removeKey(key);
	}

	@Override
	public void putAll(Map<? extends K, ? extends V> m) {
		for (var e : m.entrySet()) {
			put(e.getKey(), e.getValue());
		}
	}

	public int put(K key, V value, int index) {
		int existingIndex = indexOfKey(key);
		if (existingIndex != -1)
			removeIndex(existingIndex);
		else if (size == keys.length) //
			resize(Math.max(8, (int) (size * 1.75f)));
		System.arraycopy(keys, index, keys, index + 1, size - index);
		System.arraycopy(values, index, values, index + 1, size - index);
		keys[index] = key;
		values[index] = value;
		size++;
		return index;
	}

	public void putAll(CollectionArrayMap<? extends K, ? extends V> map) {
		putAll(map, 0, map.size());
	}

	public void putAll(CollectionArrayMap<? extends K, ? extends V> map, int offset, int length) {
		if (offset + length > map.size)
			throw new IllegalArgumentException("offset + length must be <= size: " + offset + " + " + length + " <= " + map.size);
		int sizeNeeded = size + length - offset;
		if (sizeNeeded >= keys.length) resize(Math.max(8, (int) (sizeNeeded * 1.75f)));
		System.arraycopy(map.keys, offset, keys, size, length);
		System.arraycopy(map.values, offset, values, size, length);
		size += length;
	}

	@Override
	public V get(Object key) {
		K[] ks = keys;
		int i = size - 1;
		if (key == null) {
			for (; i >= 0; i--)
				if (ks[i] == null) return values[i];
		} else {
			for (; i >= 0; i--)
				if (key.equals(ks[i])) return values[i];
		}
		return null;
	}

	public K getKey(Object value, boolean identity) {
		V[] vs = values;
		int i = size - 1;
		if (identity || value == null) {
			for (; i >= 0; i--)
				if (vs[i] == value) return keys[i];
		} else {
			for (; i >= 0; i--)
				if (value.equals(vs[i])) return keys[i];
		}
		return null;
	}

	public K getKeyAt(int index) {
		if (index >= size) throw new IndexOutOfBoundsException(String.valueOf(index));
		return keys[index];
	}

	public V getValueAt(int index) {
		if (index >= size) throw new IndexOutOfBoundsException(String.valueOf(index));
		return values[index];
	}

	public K firstKey() {
		if (size == 0) throw new IllegalStateException("Map is empty.");
		return keys[0];
	}

	public V firstValue() {
		if (size == 0) throw new IllegalStateException("Map is empty.");
		return values[0];
	}

	public void setKey(int index, K key) {
		if (index >= size) throw new IndexOutOfBoundsException(String.valueOf(index));
		keys[index] = key;
	}

	public void setValue(int index, V value) {
		if (index >= size) throw new IndexOutOfBoundsException(String.valueOf(index));
		values[index] = value;
	}

	public void insert(int index, K key, V value) {
		if (index > size) throw new IndexOutOfBoundsException(String.valueOf(index));
		K[] ks = keys;
		V[] vs = values;
		if (size == ks.length) resize(Math.max(8, (int) (size * 1.75f)));
		if (ordered) {
			System.arraycopy(ks, index, ks, index + 1, size - index);
			System.arraycopy(vs, index, vs, index + 1, size - index);
		} else {
			ks[size] = ks[index];
			vs[size] = vs[index];
		}
		size++;
		ks[index] = key;
		vs[index] = value;
	}

	@Override
	public boolean containsKey(Object key) {
		K[] ks = keys;
		int i = size - 1;
		if (key == null) {
			while (i >= 0)
				if (ks[i--] == null) return true;
		} else {
			while (i >= 0)
				if (key.equals(ks[i--])) return true;
		}
		return false;
	}

	@Override
	public boolean containsValue(Object value) {
		V[] vs = values;
		int i = size - 1;
		if (value == null) {
			while (i >= 0)
				if (vs[i--] == null) return true;
		} else {
			while (i >= 0)
				if (value.equals(vs[i--])) return true;
		}
		return false;
	}

	/**
	 * @param identity If true, == comparison will be used. If false, .equals() comparison will be used.
	 */
	public boolean containsValue(Object value, boolean identity) {
		V[] vs = values;
		int i = size - 1;
		if (identity || value == null) {
			while (i >= 0)
				if (vs[i--] == value) return true;
		} else {
			while (i >= 0)
				if (value.equals(vs[i--])) return true;
		}
		return false;
	}

	public int indexOfKey(Object key) {
		K[] ks = keys;
		if (key == null) {
			for (int i = 0, n = size; i < n; i++)
				if (ks[i] == null) return i;
		} else {
			for (int i = 0, n = size; i < n; i++)
				if (key.equals(ks[i])) return i;
		}
		return -1;
	}

	public int indexOfValue(Object value, boolean identity) {
		V[] vs = values;
		if (identity || value == null) {
			for (int i = 0, n = size; i < n; i++)
				if (vs[i] == value) return i;
		} else {
			for (int i = 0, n = size; i < n; i++)
				if (value.equals(vs[i])) return i;
		}
		return -1;
	}

	public V removeKey(Object key) {
		K[] ks = keys;
		V[] vs = values;
		if (key == null) {
			for (int i = 0, n = size; i < n; i++) {
				if (ks[i] == null) {
					V value = vs[i];
					removeIndex(i);
					return value;
				}
			}
		} else {
			for (int i = 0, n = size; i < n; i++) {
				if (key.equals(ks[i])) {
					V value = vs[i];
					removeIndex(i);
					return value;
				}
			}
		}
		return null;
	}

	public boolean removeValue(Object value, boolean identity) {
		if (identity || value == null) {
			for (int i = 0, n = size; i < n; i++) {
				if (values[i] == value) {
					removeIndex(i);
					return true;
				}
			}
		} else {
			for (int i = 0, n = size; i < n; i++) {
				if (value.equals(values[i])) {
					removeIndex(i);
					return true;
				}
			}
		}
		return false;
	}

	/** Removes and returns the key/values pair at the specified index. */
	public void removeIndex(int index) {
		if (index >= size) throw new IndexOutOfBoundsException(String.valueOf(index));
		K[] ks = keys;
		V[] vs = values;
		size--;
		if (ordered) {
			System.arraycopy(ks, index + 1, ks, index, size - index);
			System.arraycopy(vs, index + 1, vs, index, size - index);
		} else {
			ks[index] = ks[size];
			vs[index] = vs[size];
		}
		keys[size] = null;
		vs[size] = null;
	}

	@Override
	public int size() {
		return size;
	}

	@Override
	public boolean isEmpty() {
		return size == 0;
	}

	public K peekKey() {
		return keys[size - 1];
	}

	public V peekValue() {
		return values[size - 1];
	}

	public void clear(int maximumCapacity) {
		if (keys.length <= maximumCapacity) {
			clear();
			return;
		}
		size = 0;
		resize(maximumCapacity);
	}

	@Override
	public void clear() {
		K[] ks = keys;
		V[] vs = values;
		for (int i = 0, n = size; i < n; i++) {
			ks[i] = null;
			vs[i] = null;
		}
		size = 0;
	}

	@Override
	public Set<K> keySet() {
		return keys();
	}

	public void shrink() {
		if (keys.length == size) return;
		resize(size);
	}

	public void ensureCapacity(int additionalCapacity) {
		if (additionalCapacity < 0)
			throw new IllegalArgumentException("additionalCapacity must be >= 0: " + additionalCapacity);
		int sizeNeeded = size + additionalCapacity;
		if (sizeNeeded >= keys.length) resize(Math.max(8, sizeNeeded));
	}

	@SuppressWarnings("unchecked")
	protected void resize(int newSize) {
		K[] newKeys = (K[]) Array.newInstance(keys.getClass().getComponentType(), newSize);
		System.arraycopy(keys, 0, newKeys, 0, Math.min(size, newKeys.length));
		keys = newKeys;

		V[] newValues = (V[]) Array.newInstance(values.getClass().getComponentType(), newSize);
		System.arraycopy(values, 0, newValues, 0, Math.min(size, newValues.length));
		values = newValues;
	}

	public void reverse() {
		for (int i = 0, lastIndex = size - 1, n = size / 2; i < n; i++) {
			int ii = lastIndex - i;
			K tempKey = keys[i];
			keys[i] = keys[ii];
			keys[ii] = tempKey;

			V tempValue = values[i];
			values[i] = values[ii];
			values[ii] = tempValue;
		}
	}

	public void shuffle() {
		for (int i = size - 1; i >= 0; i--) {
			int ii = Mathf.random(i);
			K tempKey = keys[i];
			keys[i] = keys[ii];
			keys[ii] = tempKey;

			V tempValue = values[i];
			values[i] = values[ii];
			values[ii] = tempValue;
		}
	}

	public void truncate(int newSize) {
		if (size <= newSize) return;
		for (int i = newSize; i < size; i++) {
			keys[i] = null;
			values[i] = null;
		}
		size = newSize;
	}

	@Override
	public int hashCode() {
		K[] ks = keys;
		V[] vs = values;
		int h = 0;
		for (int i = 0, n = size; i < n; i++) {
			K key = ks[i];
			V value = vs[i];
			if (key != null) h += key.hashCode() * 31;
			if (value != null) h += value.hashCode();
		}
		return h;
	}

	@Override
	public boolean equals(Object o) {
		if (o == this) return true;
		if (!(o instanceof Map<?, ?> map))
			return false;
		if (map.size() != size) return false;
		K[] ks = keys;
		V[] vs = values;
		for (int i = 0, n = size; i < n; i++) {
			K key = ks[i];
			V value = vs[i];
			if (value == null) {
				if (!map.containsKey(key) || map.get(key) != null) return false;
			} else {
				if (!value.equals(map.get(key))) return false;
			}
		}
		return true;
	}

	@Override
	public String toString() {
		if (size == 0) return "{}";
		K[] ks = keys;
		V[] vs = values;
		StringBuilder buffer = new StringBuilder(32);
		buffer.append('{');
		buffer.append(ks[0]);
		buffer.append('=');
		buffer.append(vs[0]);
		for (int i = 1; i < size; i++) {
			buffer.append(", ");
			buffer.append(ks[i]);
			buffer.append('=');
			buffer.append(vs[i]);
		}
		buffer.append('}');
		return buffer.toString();
	}

	@Override
	public Iterator<ObjectHolder<K, V>> iterator() {
		return entries();
	}

	public Entries entries() {
		if (entries1 == null) {
			entries1 = new Entries();
			entries2 = new Entries();
		}
		if (!entries1.valid) {
			entries1.index = 0;
			entries1.valid = true;
			entries2.valid = false;
			return entries1;
		}
		entries2.index = 0;
		entries2.valid = true;
		entries1.valid = false;
		return entries2;
	}

	@Override
	public Values values() {
		if (valuesIter1 == null) {
			valuesIter1 = new Values();
			valuesIter2 = new Values();
		}
		if (!valuesIter1.valid) {
			valuesIter1.index = 0;
			valuesIter1.valid = true;
			valuesIter2.valid = false;
			return valuesIter1;
		}
		valuesIter2.index = 0;
		valuesIter2.valid = true;
		valuesIter1.valid = false;
		return valuesIter2;
	}

	@Override
	public MapEntrySet entrySet() {
		return new MapEntrySet();
	}

	public Keys keys() {
		if (keysIter1 == null) {
			keysIter1 = new Keys();
			keysIter2 = new Keys();
		}
		if (!keysIter1.valid) {
			keysIter1.index = 0;
			keysIter1.valid = true;
			keysIter2.valid = false;
			return keysIter1;
		}
		keysIter2.index = 0;
		keysIter2.valid = true;
		keysIter1.valid = false;
		return keysIter2;
	}

	public class MapEntrySet extends AbstractSet<Entry<K, V>> {
		protected final MapItr itr = new MapItr();
		protected final MapEnt ent = new MapEnt();

		@Override
		public int size() {
			return size;
		}

		@Override
		public void clear() {
			CollectionArrayMap.this.clear();
		}

		@Override
		public Iterator<Entry<K, V>> iterator() {
			itr.entries = entries();
			return itr;
		}

		@Override
		public boolean contains(Object o) {
			if (!(o instanceof Entry<?, ?> e))
				return false;
			Object key = e.getKey();
			return containsKey(key);
		}

		@Override
		public boolean remove(Object o) {
			if (o instanceof Entry<?, ?> e) {
				Object key = e.getKey();
				return CollectionArrayMap.this.remove(key) != null;
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

	protected abstract class MapIterator<I> extends AbstractSet<I> implements Iterator<I> {
		protected int index;
		protected boolean valid = true;

		@Override
		public boolean hasNext() {
			if (!valid) throw new ArcRuntimeException("#iterator() cannot be used nested.");
			return index < size;
		}

		@Override
		public Iterator<I> iterator() {
			return this;
		}

		@Override
		public int size() {
			return size;
		}

		public abstract CollectionList<I> toList();

		public abstract CollectionList<I> toList(CollectionList<I> list);
	}

	public class Entries extends MapIterator<ObjectHolder<K, V>> {
		protected ObjectHolder<K, V> entry = new ObjectHolder<>();

		@Override
		public Iterator<ObjectHolder<K, V>> iterator() {
			return this;
		}

		/** Note the same entry instance is returned each time this method is called. */
		@Override
		public ObjectHolder<K, V> next() {
			if (index >= size) throw new NoSuchElementException(String.valueOf(index));
			if (!valid) throw new ArcRuntimeException("#iterator() cannot be used nested.");
			entry.key = keys[index];
			entry.value = values[index++];
			return entry;
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
		public CollectionList<ObjectHolder<K, V>> toList() {
			return toList(new CollectionList<>(size - index, ObjectHolder.class));
		}

		@Override
		public CollectionList<ObjectHolder<K, V>> toList(CollectionList<ObjectHolder<K, V>> list) {
			while (hasNext()) {
				list.add(Arrays2.copyOf(next()));
			}
			return list;
		}
	}

	public class Values extends MapIterator<V> {
		protected int index;
		protected boolean valid = true;

		@Override
		public boolean hasNext() {
			if (!valid) throw new ArcRuntimeException("#iterator() cannot be used nested.");
			return index < size;
		}

		@Override
		public V next() {
			if (index >= size) throw new NoSuchElementException(String.valueOf(index));
			if (!valid) throw new ArcRuntimeException("#iterator() cannot be used nested.");
			return values[index++];
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
		public CollectionList<V> toList() {
			return new CollectionList<>(values, index, size - index);
		}

		@Override
		public CollectionList<V> toList(CollectionList<V> list) {
			list.addAll(values, index, size - index);
			return list;
		}
	}

	public class Keys extends MapIterator<K> {
		@Override
		public K next() {
			if (index >= size) throw new NoSuchElementException(String.valueOf(index));
			if (!valid) throw new ArcRuntimeException("#iterator() cannot be used nested.");
			return keys[index++];
		}

		@Override
		public boolean add(K k) {
			return false;
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
		public CollectionList<K> toList() {
			return new CollectionList<>(keys, index, size - index);
		}

		@Override
		public CollectionList<K> toList(CollectionList<K> list) {
			list.addAll(keys, index, size - index);
			return list;
		}
	}
}
