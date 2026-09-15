package endfield.util;

import arc.struct.IntSeq;
import arc.util.ArcRuntimeException;
import endfield.util.holder.IntBoolHolder;

import java.util.NoSuchElementException;

public class IntBoolOrderedMap extends IntBoolMap {
	public final IntSeq orderedKeys;

	public IntBoolOrderedMap() {
		super();
		orderedKeys = new IntSeq();
	}

	public IntBoolOrderedMap(int initialCapacity) {
		super(initialCapacity);
		orderedKeys = new IntSeq(initialCapacity);
	}

	public IntBoolOrderedMap(int initialCapacity, float loadFactor) {
		super(initialCapacity, loadFactor);
		orderedKeys = new IntSeq(initialCapacity);
	}

	public IntBoolOrderedMap(IntBoolOrderedMap map) {
		super(map);
		orderedKeys = new IntSeq(map.size);
	}

	@Override
	public void put(int key, boolean value) {
		int i = locateKey(key);
		if (i >= 0) {
			valueTable[i] = value;
			return;
		}
		i = -(i + 1);
		keyTable[i] = key;
		valueTable[i] = value;
		orderedKeys.add(key);
		if (++size >= threshold) resize(keyTable.length << 1);
	}

	@Override
	public boolean remove(int key, boolean defaultValue) {
		orderedKeys.removeValue(key);
		return super.remove(key, defaultValue);
	}

	public boolean removeIndex(int index) {
		return super.remove(orderedKeys.removeIndex(index));
	}

	public boolean alter(int before, int after) {
		if (containsKey(after)) return false;
		int index = orderedKeys.indexOf(before);
		if (index == -1) return false;
		super.put(after, super.remove(before));
		orderedKeys.set(index, after);
		return true;
	}

	public boolean alterIndex(int index, int after) {
		if (index < 0 || index >= size || containsKey(after)) return false;
		super.put(after, super.remove(orderedKeys.get(index)));
		orderedKeys.set(index, after);
		return true;
	}

	@Override
	public void clear() {
		orderedKeys.clear();
		super.clear();
	}

	@Override
	public void clear(int maximumCapacity) {
		orderedKeys.clear();
		super.clear(maximumCapacity);
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
	public Keys keys() {
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
	public String toString() {
		if (size == 0) return "{}";
		StringBuilder buffer = new StringBuilder(32);
		buffer.append('{');
		for (int i = 0, n = orderedKeys.size; i < n; i++) {
			int key = orderedKeys.get(i);
			if (i > 0) buffer.append(", ");
			buffer.append(key);
			buffer.append('=');
			buffer.append(get(key));
		}
		buffer.append('}');
		return buffer.toString();
	}

	public class OrderedMapEntries extends Entries {
		@Override
		public void reset() {
			nextIndex = 0;
			hasNext = size > 0;
		}

		@Override
		public IntBoolHolder next() {
			if (!hasNext) throw new NoSuchElementException();
			if (!valid) throw new ArcRuntimeException("#iterator() cannot be used nested.");
			entry.key = orderedKeys.get(nextIndex);
			entry.value = get(entry.key);
			nextIndex++;
			hasNext = nextIndex < size;
			return entry;
		}

		@Override
		public void remove() {
			if (currentIndex < 0) throw new IllegalStateException("next must be called before remove.");
			IntBoolOrderedMap.this.remove(entry.key);
			nextIndex--;
		}
	}

	public class OrderedMapKeys extends Keys {
		@Override
		public void reset() {
			nextIndex = 0;
			hasNext = size > 0;
		}

		@Override
		public int next() {
			if (!hasNext) throw new NoSuchElementException();
			if (!valid) throw new ArcRuntimeException("#iterator() cannot be used nested.");
			int key = orderedKeys.get(nextIndex);
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
		public boolean next() {
			if (!hasNext) throw new NoSuchElementException();
			if (!valid) throw new ArcRuntimeException("#iterator() cannot be used nested.");
			boolean value = get(orderedKeys.get(nextIndex));
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
