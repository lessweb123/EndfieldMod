package endfield.util;

import arc.util.ArcRuntimeException;
import endfield.util.holder.ObjectHolder;

import java.util.NoSuchElementException;

/**
 * Implementation of Java Collection Framework {@code Map} based on {@code OrderedMap}, used in places that require
 * Java specifications and the feature of {@code OrderedMap} not creating nodes.
 */
public class CollectionOrderedMap<K, V> extends CollectionObjectMap<K, V> {
	public final CollectionList<K> orderedKeys;

	public CollectionOrderedMap() {
		orderedKeys = new CollectionList<>();
	}

	public CollectionOrderedMap(Class<?> keyType, Class<?> valueType) {
		super(keyType, valueType, 16, 0.75f);
		orderedKeys = new CollectionList<>(keyType);
	}

	public CollectionOrderedMap(Class<?> keyType, Class<?> valueType, int capacity) {
		super(keyType, valueType, capacity, 0.75f);
		orderedKeys = new CollectionList<>(capacity, keyType);
	}

	public CollectionOrderedMap(Class<?> keyType, Class<?> valueType, int capacity, float loadFactor) {
		super(keyType, valueType, capacity, loadFactor);
		orderedKeys = new CollectionList<>(capacity, keyType);
	}

	public CollectionOrderedMap(CollectionOrderedMap<? extends K, ? extends V> map) {
		super(map);
		orderedKeys = new CollectionList<>(map.size, map.keyComponentType);
		putAll(map);
	}

	@Override
	public V put(K key, V value) {
		if (key == null) throw new IllegalArgumentException("key cannot be null.");
		int i = locateKey(key);
		if (i >= 0) { // Existing key was found.
			V oldValue = valueTable[i];
			valueTable[i] = value;
			return oldValue;
		}
		i = -(i + 1);
		keyTable[i] = key;
		valueTable[i] = value;
		orderedKeys.add(key);
		if (++size >= threshold) resize(keyTable.length << 1);
		return null;
	}

	@Override
	public V putMissing(K key, V value) {
		if (key == null) throw new IllegalArgumentException("key cannot be null.");
		int i = locateKey(key);
		if (i >= 0) return valueTable[i];
		i = -(i + 1);
		keyTable[i] = key;
		valueTable[i] = value;
		orderedKeys.add(key);
		if (++size >= threshold) resize(keyTable.length << 1);
		return null;
	}

	public void putAll(CollectionOrderedMap<? extends K, ? extends V> map) {
		ensureCapacity(map.size);
		K[] keys = map.orderedKeys.items;
		for (int i = 0, n = map.orderedKeys.size; i < n; i++) {
			K key = keys[i];
			put(key, map.get(key));
		}
	}

	@Override
	public V remove(Object key) {
		orderedKeys.remove(key, false);
		return super.remove(key);
	}

	public V removeIndex(int index) {
		return super.remove(orderedKeys.remove(index));
	}

	public boolean alter(K before, K after) {
		if (containsKey(after)) return false;
		int index = orderedKeys.indexOf(before, false);
		if (index == -1) return false;
		super.put(after, super.remove(before));
		orderedKeys.set(index, after);
		return true;
	}

	public boolean alterIndex(int index, K after) {
		if (index < 0 || index >= size || containsKey(after)) return false;
		super.put(after, super.remove(orderedKeys.get(index)));
		orderedKeys.set(index, after);
		return true;
	}

	@Override
	public void clear(int maximumCapacity) {
		orderedKeys.clear();
		super.clear(maximumCapacity);
	}

	@Override
	public void clear() {
		orderedKeys.clear();
		super.clear();
	}

	public CollectionList<K> orderedKeys() {
		return orderedKeys;
	}

	@Override
	public Entries iterator() {
		if (entries1 == null) {
			entries1 = new OrderedMapEntries();
			entries2 = new OrderedMapEntries();
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
	public Keys keySet() {
		if (keys1 == null) {
			keys1 = new OrderedMapKeys();
			keys2 = new OrderedMapKeys();
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

	@Override
	public Values values() {
		if (values1 == null) {
			values1 = new OrderedMapValues();
			values2 = new OrderedMapValues();
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
	public String toString(String separator, boolean braces) {
		if (size == 0) return braces ? "{}" : "";
		StringBuilder buffer = new StringBuilder(32);
		if (braces) buffer.append('{');
		CollectionList<K> keys = orderedKeys;
		for (int i = 0, n = keys.size; i < n; i++) {
			K key = keys.get(i);
			if (i > 0) buffer.append(separator);
			buffer.append(key == this ? "(this)" : key);
			buffer.append('=');
			V value = get(key);
			buffer.append(value == this ? "(this)" : value);
		}
		if (braces) buffer.append('}');
		return buffer.toString();
	}

	public class OrderedMapEntries extends Entries {
		@Override
		public void reset() {
			nextIndex = 0;
			hasNext = size > 0;
		}

		@Override
		public ObjectHolder<K, V> next() {
			if (!hasNext) throw new NoSuchElementException();
			if (!valid) throw new ArcRuntimeException("#iterator() cannot be used nested.");
			currentIndex = nextIndex;
			entry.key = orderedKeys.get(nextIndex);
			entry.value = get(entry.key);
			nextIndex++;
			hasNext = nextIndex < size;
			return entry;
		}

		@Override
		public void remove() {
			if (currentIndex < 0) throw new IllegalStateException("next must be called before remove.");
			CollectionOrderedMap.this.remove(entry.key);
			nextIndex--;
			currentIndex = -1;
		}
	}

	public class OrderedMapKeys extends Keys {
		@Override
		public void reset() {
			nextIndex = 0;
			hasNext = size > 0;
		}

		@Override
		public K next() {
			if (!hasNext) throw new NoSuchElementException();
			if (!valid) throw new ArcRuntimeException("#iterator() cannot be used nested.");
			K key = orderedKeys.get(nextIndex);
			currentIndex = nextIndex;
			nextIndex++;
			hasNext = nextIndex < size;
			return key;
		}

		@Override
		public void remove() {
			if (currentIndex < 0) throw new IllegalStateException("next must be called before remove.");
			removeIndex(nextIndex - 1);
			nextIndex = currentIndex;
			currentIndex = -1;
		}
	}

	public class OrderedMapValues extends Values {
		@Override
		public void reset() {
			nextIndex = 0;
			hasNext = size > 0;
		}

		@Override
		public V next() {
			if (!hasNext) throw new NoSuchElementException();
			if (!valid) throw new ArcRuntimeException("#iterator() cannot be used nested.");
			V value = get(orderedKeys.get(nextIndex));
			currentIndex = nextIndex;
			nextIndex++;
			hasNext = nextIndex < size;
			return value;
		}

		@Override
		public void remove() {
			if (currentIndex < 0) throw new IllegalStateException("next must be called before remove.");
			removeIndex(currentIndex);
			nextIndex = currentIndex;
			currentIndex = -1;
		}
	}
}
