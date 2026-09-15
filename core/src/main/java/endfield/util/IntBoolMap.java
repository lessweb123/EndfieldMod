package endfield.util;

import arc.struct.BoolSeq;
import arc.struct.IntSeq;
import arc.util.ArcRuntimeException;
import endfield.util.holder.IntBoolHolder;

import java.util.Arrays;
import java.util.Iterator;
import java.util.NoSuchElementException;

import static endfield.util.CollectionObjectSet.tableSize;
import static endfield.util.Constant.INDEX_ILLEGAL;
import static endfield.util.Constant.INDEX_ZERO;

/**
 * An unordered map where the keys are ints and values are booleans. This implementation is a cuckoo hash map using 3 hashes, random
 * walking, and a small stash for problematic keys. Null keys are not allowed. No allocation is done except when growing the table
 * size. <br>
 * <br>
 * This map performs very fast get, containsKey, and remove (typically O(1), worst case O(log(n))). Put may be a bit slower,
 * depending on hash collisions. Load factors greater than 0.91 greatly increase the chances the map will have to rehash to the
 * next higher POT size.
 *
 * @author Nathan Sweet
 */
public class IntBoolMap implements Iterable<IntBoolHolder>, Cloneable {
	public int size;

	protected int[] keyTable;
	protected boolean[] valueTable;

	protected boolean zeroValue;
	protected boolean hasZeroValue;

	protected float loadFactor;
	protected int threshold;

	protected int shift;

	protected int mask;

	protected transient Entries entries1, entries2;
	protected transient Values values1, values2;
	protected transient Keys keys1, keys2;

	/** Creates a new map with an initial capacity of 51 and a load factor of 0.8. */
	public IntBoolMap() {
		this(51, 0.8f);
	}

	/**
	 * Creates a new map with a load factor of 0.8.
	 *
	 * @param initialCapacity If not a power of two, it is increased to the next nearest power of two.
	 */
	public IntBoolMap(int initialCapacity) {
		this(initialCapacity, 0.8f);
	}

	/**
	 * Creates a new map with the specified initial capacity and load factor. This map will hold initialCapacity items before
	 * growing the backing table.
	 *
	 * @param initialCapacity If not a power of two, it is increased to the next nearest power of two.
	 */
	public IntBoolMap(int initialCapacity, float loadFactor) {
		if (loadFactor <= 0f || loadFactor >= 1f)
			throw new IllegalArgumentException("loadFactor must be > 0 and < 1: " + loadFactor);
		this.loadFactor = loadFactor;

		int tableSize = tableSize(initialCapacity, loadFactor);
		threshold = (int) (tableSize * loadFactor);
		mask = tableSize - 1;
		shift = Long.numberOfLeadingZeros(mask);

		keyTable = new int[tableSize];
		valueTable = new boolean[tableSize];
	}

	/** Creates a new map identical to the specified map. */
	public IntBoolMap(IntBoolMap map) {
		this((int) (map.keyTable.length * map.loadFactor), map.loadFactor);
		System.arraycopy(map.keyTable, 0, keyTable, 0, map.keyTable.length);
		System.arraycopy(map.valueTable, 0, valueTable, 0, map.valueTable.length);
		size = map.size;
		zeroValue = map.zeroValue;
		hasZeroValue = map.hasZeroValue;
	}

	public static IntBoolMap of(Object... values) {
		IntBoolMap map = new IntBoolMap();
		for (int i = 0; i < values.length; i += 2) {
			map.put((int) values[i], (boolean) values[i + 1]);
		}
		return map;
	}

	protected int place(int item) {
		return (int) (item * 0x9E3779B97F4A7C15L >>> shift);
	}

	protected int locateKey(int key) {
		int[] ks = keyTable;
		for (int i = place(key); ; i = i + 1 & mask) {
			int other = ks[i];
			if (other == 0) return -(i + 1); // Empty space is available.
			if (other == key) return i; // Same key was found.
		}
	}

	public IntBoolMap copy() {
		try {
			IntBoolMap out = (IntBoolMap) super.clone();
			out.keyTable = keyTable.clone();
			out.valueTable = valueTable.clone();

			out.entries1 = out.entries2 = null;
			out.values1 = out.values2 = null;
			out.keys1 = out.keys2 = null;

			return out;
		} catch (CloneNotSupportedException e) {
			return new IntBoolMap(this);
		}
	}

	public void put(int key, boolean value) {
		if (key == 0) {
			zeroValue = value;
			if (!hasZeroValue) {
				hasZeroValue = true;
				size++;
			}
			return;
		}
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

	public boolean put(int key, boolean value, boolean defaultValue) {
		if (key == 0) {
			boolean oldValue = zeroValue;
			zeroValue = value;
			if (!hasZeroValue) {
				hasZeroValue = true;
				size++;
				return defaultValue;
			}
			return oldValue;
		}
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
		return defaultValue;
	}

	public void putMissing(int key, boolean value) {
		if (key == 0) {
			if (!hasZeroValue) {
				zeroValue = value;
				hasZeroValue = true;
				size++;
			}
			return;
		}
		int i = locateKey(key);
		if (i >= 0) return;
		i = -(i + 1);
		keyTable[i] = key;
		valueTable[i] = value;
		if (++size >= threshold) resize(keyTable.length << 1);
	}

	public boolean putMissing(int key, boolean value, boolean defaultValue) {
		if (key == 0) {
			if (!hasZeroValue) {
				zeroValue = value;
				hasZeroValue = true;
				size++;
				return defaultValue;
			}
			return zeroValue;
		}
		int i = locateKey(key);
		if (i >= 0) return valueTable[i];
		i = -(i + 1);
		keyTable[i] = key;
		valueTable[i] = value;
		if (++size >= threshold) resize(keyTable.length << 1);
		return defaultValue;
	}

	public void putAll(IntBoolMap map) {
		ensureCapacity(map.size);
		if (map.hasZeroValue) put(0, map.zeroValue);
		int[] ks = map.keyTable;
		boolean[] vs = map.valueTable;
		for (int i = 0, n = ks.length; i < n; i++) {
			int key = ks[i];
			if (key != 0) put(key, vs[i]);
		}
	}

	protected void putResize(int key, boolean value) {
		int[] ks = keyTable;
		for (int i = place(key); ; i = (i + 1) & mask) {
			if (ks[i] == 0) {
				ks[i] = key;
				valueTable[i] = value;
				return;
			}
		}
	}

	public boolean get(int key) {
		return get(key, false);
	}

	public boolean get(int key, boolean defaultValue) {
		if (key == 0) return hasZeroValue ? zeroValue : defaultValue;
		int i = locateKey(key);
		return i >= 0 ? valueTable[i] : defaultValue;
	}

	public boolean remove(int key) {
		return remove(key, false);
	}

	public boolean remove(int key, boolean defaultValue) {
		if (key == 0) {
			if (!hasZeroValue) return defaultValue;
			hasZeroValue = false;
			size--;
			return zeroValue;
		}

		int i = locateKey(key);
		if (i < 0) return defaultValue;
		int[] ks = keyTable;
		boolean[] vs = valueTable;
		boolean oldValue = vs[i];
		int m = mask, next = i + 1 & m;
		while ((key = ks[next]) != 0) {
			int placement = place(key);
			if ((next - placement & m) > (i - placement & m)) {
				ks[i] = key;
				vs[i] = vs[next];
				i = next;
			}
			next = next + 1 & m;
		}
		ks[i] = 0;
		size--;
		return oldValue;
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
		hasZeroValue = false;
		resize(tableSize);
	}

	public void clear() {
		if (size == 0) return;
		Arrays.fill(keyTable, 0);
		size = 0;
		hasZeroValue = false;
	}

	public boolean containsValue(boolean value) {
		if (hasZeroValue && zeroValue == value) return true;
		int[] ks = keyTable;
		boolean[] vs = valueTable;
		for (int i = vs.length - 1; i >= 0; i--)
			if (ks[i] != 0 && vs[i] == value) return true;
		return false;
	}

	public boolean containsKey(int key) {
		if (key == 0) return hasZeroValue;
		return locateKey(key) >= 0;
	}

	public int findKey(boolean value, int notFound) {
		if (hasZeroValue && zeroValue == value) return 0;
		int[] ks = keyTable;
		boolean[] vs = valueTable;
		for (int i = vs.length - 1; i >= 0; i--)
			if (ks[i] != 0 && vs[i] == value) return ks[i];
		return notFound;
	}

	public void ensureCapacity(int additionalCapacity) {
		int tableSize = tableSize(size + additionalCapacity, loadFactor);
		if (keyTable.length < tableSize) resize(tableSize);
	}

	protected void resize(int newSize) {
		int oldCapacity = keyTable.length;
		threshold = (int) (newSize * loadFactor);
		mask = newSize - 1;
		shift = Long.numberOfLeadingZeros(mask);

		int[] oldKeyTable = keyTable;
		boolean[] oldValueTable = valueTable;

		keyTable = new int[newSize];
		valueTable = new boolean[newSize];

		if (size > 0) {
			for (int i = 0; i < oldCapacity; i++) {
				int key = oldKeyTable[i];
				if (key != 0) putResize(key, oldValueTable[i]);
			}
		}
	}

	@Override
	public int hashCode() {
		int h = size;
		if (hasZeroValue) h += Boolean.hashCode(zeroValue);
		int[] ks = keyTable;
		boolean[] vs = valueTable;
		for (int i = 0, n = ks.length; i < n; i++) {
			int key = ks[i];
			if (key != 0) h += key * 31 + Boolean.hashCode(vs[i]);
		}
		return h;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this) return true;
		if (!(obj instanceof IntBoolMap other)) return false;
		if (other.size != size) return false;
		if (other.hasZeroValue != hasZeroValue) return false;
		if (hasZeroValue) {
			if (other.zeroValue != zeroValue) return false;
		}
		int[] ks = keyTable;
		boolean[] vs = valueTable;
		for (int i = 0, n = ks.length; i < n; i++) {
			int key = ks[i];
			if (key != 0) {
				boolean otherValue = other.get(key, false);
				if (!other.containsKey(key)) return false;
				if (otherValue != vs[i]) return false;
			}
		}
		return true;
	}

	@Override
	public String toString() {
		if (size == 0) return "[]";
		StringBuilder buffer = new StringBuilder(32);
		buffer.append('[');
		int[] ks = keyTable;
		boolean[] vs = valueTable;
		int i = ks.length;
		if (hasZeroValue) {
			buffer.append("0=");
			buffer.append(zeroValue);
		} else {
			while (i-- > 0) {
				int key = ks[i];
				if (key == 0) continue;
				buffer.append(key);
				buffer.append('=');
				buffer.append(vs[i]);
				break;
			}
		}
		while (i-- > 0) {
			int key = ks[i];
			if (key == 0) continue;
			buffer.append(", ");
			buffer.append(key);
			buffer.append('=');
			buffer.append(vs[i]);
		}
		buffer.append(']');
		return buffer.toString();
	}

	@Override
	public Iterator<IntBoolHolder> iterator() {
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

	public class MapIterator {
		public boolean hasNext;

		protected int nextIndex, currentIndex;
		protected boolean valid = true;

		public MapIterator() {
			reset();
		}

		public void reset() {
			currentIndex = INDEX_ILLEGAL;
			nextIndex = INDEX_ZERO;
			if (hasZeroValue)
				hasNext = true;
			else
				findNextIndex();
		}

		protected void findNextIndex() {
			int[] ks = keyTable;
			for (int n = ks.length; ++nextIndex < n; ) {
				if (ks[nextIndex] != 0) {
					hasNext = true;
					return;
				}
			}
			hasNext = false;
		}

		public void remove() {
			int i = currentIndex;
			if (i == INDEX_ZERO && hasZeroValue) {
				hasZeroValue = false;
			} else if (i < 0) {
				throw new IllegalStateException("next must be called before remove.");
			} else {
				int[] ks = keyTable;
				boolean[] vs = valueTable;
				int m = mask, next = i + 1 & m, key;
				while ((key = ks[next]) != 0) {
					int placement = place(key);
					if ((next - placement & m) > (i - placement & m)) {
						ks[i] = key;
						vs[i] = vs[next];
						i = next;
					}
					next = next + 1 & m;
				}
				ks[i] = 0;
				if (i != currentIndex) --nextIndex;
			}
			currentIndex = INDEX_ILLEGAL;
			size--;
		}
	}

	public class Entries extends MapIterator implements Iterable<IntBoolHolder>, Iterator<IntBoolHolder> {
		protected IntBoolHolder entry = new IntBoolHolder();

		public Entries() {}

		public IntBoolHolder next() {
			if (!hasNext) throw new NoSuchElementException();
			if (!valid) throw new ArcRuntimeException("#iterator() cannot be used nested.");
			if (nextIndex == INDEX_ZERO) {
				entry.key = 0;
				entry.value = zeroValue;
			} else {
				entry.key = keyTable[nextIndex];
				entry.value = valueTable[nextIndex];
			}
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
		public Iterator<IntBoolHolder> iterator() {
			return this;
		}
	}

	public class Values extends MapIterator {
		public Values() {}

		public boolean hasNext() {
			if (!valid) throw new ArcRuntimeException("#iterator() cannot be used nested.");
			return hasNext;
		}

		public boolean next() {
			if (!hasNext) throw new NoSuchElementException();
			if (!valid) throw new ArcRuntimeException("#iterator() cannot be used nested.");
			boolean value = nextIndex == INDEX_ZERO ? zeroValue : valueTable[nextIndex];
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
	}

	public class Keys extends MapIterator {
		public Keys() {}

		public boolean hasNext() {
			if (!valid) throw new ArcRuntimeException("#iterator() cannot be used nested.");
			return hasNext;
		}

		public int next() {
			if (!hasNext) throw new NoSuchElementException();
			if (!valid) throw new ArcRuntimeException("#iterator() cannot be used nested.");
			int key = nextIndex == INDEX_ZERO ? 0 : keyTable[nextIndex];
			currentIndex = nextIndex;
			findNextIndex();
			return key;
		}

		public IntSeq toSeq() {
			IntSeq array = new IntSeq(true, size);
			while (hasNext)
				array.add(next());
			return array;
		}
	}
}
