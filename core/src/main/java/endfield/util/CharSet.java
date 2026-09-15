package endfield.util;

import arc.util.ArcRuntimeException;
import endfield.func.Charc;

import java.util.Arrays;
import java.util.NoSuchElementException;

import static endfield.util.CollectionObjectSet.tableSize;
import static endfield.util.Constant.INDEX_ILLEGAL;
import static endfield.util.Constant.INDEX_ZERO;

public class CharSet implements Cloneable {
	public int size;

	protected char[] keyTable;
	protected boolean hasZeroValue;

	protected float loadFactor;
	protected int threshold;

	protected int shift;

	protected int mask;

	protected transient CharSetIterator iterator1, iterator2;

	public CharSet() {
		this(51, 0.8f);
	}

	public CharSet(int initialCapacity) {
		this(initialCapacity, 0.8f);
	}

	public CharSet(int initialCapacity, float loadFactor) {
		if (loadFactor <= 0f || loadFactor >= 1f)
			throw new IllegalArgumentException("loadFactor must be > 0 and < 1: " + loadFactor);
		this.loadFactor = loadFactor;

		int tableSize = tableSize(initialCapacity, loadFactor);
		threshold = (int) (tableSize * loadFactor);
		mask = tableSize - 1;
		shift = Long.numberOfLeadingZeros(mask);

		keyTable = new char[tableSize];
	}

	public CharSet(CharSet set) {
		this((int) (set.keyTable.length * set.loadFactor), set.loadFactor);
		System.arraycopy(set.keyTable, 0, keyTable, 0, set.keyTable.length);
		size = set.size;
		hasZeroValue = set.hasZeroValue;
	}

	public CharSet copy() {
		try {
			CharSet out = (CharSet) super.clone();
			out.keyTable = keyTable.clone();

			out.iterator1 = out.iterator2 = null;

			return out;
		} catch (CloneNotSupportedException e) {
			return new CharSet(this);
		}
	}

	public static CharSet with(char... array) {
		CharSet set = new CharSet();
		set.addAll(array);
		return set;
	}

	public void each(Charc cons) {
		CharSetIterator iter = iterator();
		while (iter.hasNext) {
			cons.get(iter.next());
		}
	}

	protected int place(char item) {
		return (int) (item * 0x9E3779B97F4A7C15L >>> shift);
	}

	protected int locateKey(char key) {
		char[] ks = keyTable;
		for (int i = place(key); ; i = i + 1 & mask) {
			int other = ks[i];
			if (other == 0) return -(i + 1);
			if (other == key) return i;
		}
	}

	public boolean add(char key) {
		if (key == 0) {
			if (hasZeroValue) return false;
			hasZeroValue = true;
			size++;
			return true;
		}
		int i = locateKey(key);
		if (i >= 0) return false;
		i = -(i + 1);
		keyTable[i] = key;
		if (++size >= threshold) resize(keyTable.length << 1);
		return true;
	}

	public void addAll(CharSeq array) {
		addAll(array.items, 0, array.size);
	}

	public void addAll(CharSeq array, int offset, int length) {
		if (offset + length > array.size)
			throw new IllegalArgumentException("offset + length must be <= size: " + offset + " + " + length + " <= " + array.size);
		addAll(array.items, offset, length);
	}

	public void addAll(char... array) {
		addAll(array, 0, array.length);
	}

	public void addAll(char[] array, int offset, int length) {
		ensureCapacity(length);
		for (int i = offset, n = i + length; i < n; i++)
			add(array[i]);
	}

	public void addAll(CharSet set) {
		ensureCapacity(set.size);
		CharSetIterator iterator = set.iterator();
		while (iterator.hasNext)
			add(iterator.next());
	}

	public void add(String str) {
		addAll(str.toCharArray(), 0, str.length());
	}

	protected void addResize(char key) {
		char[] ks = keyTable;
		for (int i = place(key); ; i = (i + 1) & mask) {
			if (ks[i] == 0) {
				ks[i] = key;
				return;
			}
		}
	}

	public boolean remove(char key) {
		if (key == 0) {
			if (!hasZeroValue) return false;
			hasZeroValue = false;
			size--;
			return true;
		}

		int i = locateKey(key);
		if (i < 0) return false;
		char[] ks = keyTable;
		int m = mask, next = i + 1 & m;
		while ((key = ks[next]) != 0) {
			int placement = place(key);
			if ((next - placement & m) > (i - placement & m)) {
				ks[i] = key;
				i = next;
			}
			next = next + 1 & m;
		}
		ks[i] = 0;
		size--;
		return true;
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
		size = 0;
		Arrays.fill(keyTable, '\u0000');
		hasZeroValue = false;
	}

	public boolean contains(char key) {
		if (key == 0) return hasZeroValue;
		return locateKey(key) >= 0;
	}

	public int first() {
		if (hasZeroValue) return 0;
		char[] ks = keyTable;
		for (char k : ks) {
			if (k != 0) return k;
		}
		throw new IllegalStateException("CharSet is empty.");
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

		char[] oldKeyTable = keyTable;

		keyTable = new char[newSize];

		if (size > 0) {
			for (int i = 0; i < oldCapacity; i++) {
				char key = oldKeyTable[i];
				if (key != 0) addResize(key);
			}
		}
	}

	@Override
	public int hashCode() {
		int h = size;
		char[] ks = keyTable;
		for (int key : ks) {
			if (key != 0) h += key;
		}
		return h;
	}

	@Override
	public boolean equals(Object o) {
		if (!(o instanceof CharSet other)) return false;
		if (other.size != size) return false;
		if (other.hasZeroValue != hasZeroValue) return false;
		char[] ks = keyTable;
		for (char k : ks) {
			if (k != 0 && !other.contains(k)) return false;
		}
		return true;
	}

	@Override
	public String toString() {
		if (size == 0) return "[]";
		StringBuilder buffer = new StringBuilder(32);
		buffer.append('[');
		char[] table = keyTable;
		int i = table.length;
		if (hasZeroValue)
			buffer.append("0");
		else {
			while (i-- > 0) {
				int key = table[i];
				if (key == 0) continue;
				buffer.append(key);
				break;
			}
		}
		while (i-- > 0) {
			int key = table[i];
			if (key == 0) continue;
			buffer.append(", ");
			buffer.append(key);
		}
		buffer.append(']');
		return buffer.toString();
	}

	public CharSetIterator iterator() {
		if (iterator1 == null) {
			iterator1 = new CharSetIterator();
			iterator2 = new CharSetIterator();
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

	public class CharSetIterator {
		public boolean hasNext;
		protected int nextIndex, currentIndex;
		protected boolean valid = true;

		public CharSetIterator() {
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
			char[] ks = keyTable;
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
				char[] ks = keyTable;
				int m = mask, next = i + 1 & m;
				char key;
				while ((key = ks[next]) != 0) {
					int placement = place(key);
					if ((next - placement & m) > (i - placement & m)) {
						ks[i] = key;
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

		public char next() {
			if (!hasNext) throw new NoSuchElementException();
			if (!valid) throw new ArcRuntimeException("#iterator() cannot be used nested.");
			char key = nextIndex == INDEX_ZERO ? 0 : keyTable[nextIndex];
			currentIndex = nextIndex;
			findNextIndex();
			return key;
		}

		/** Returns a new array containing the remaining keys. */
		public CharSeq toArray() {
			CharSeq array = new CharSeq(true, size);
			while (hasNext)
				array.add(next());
			return array;
		}
	}
}
