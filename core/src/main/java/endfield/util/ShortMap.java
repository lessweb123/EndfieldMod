package endfield.util;

import arc.func.Cons;
import arc.func.Prov;
import arc.struct.Seq;
import arc.struct.ShortSeq;
import arc.util.ArcRuntimeException;
import arc.util.Eachable;
import endfield.util.holder.ShortHolder;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Iterator;
import java.util.NoSuchElementException;

import static endfield.util.CollectionObjectSet.tableSize;
import static endfield.util.Constant.INDEX_ILLEGAL;
import static endfield.util.Constant.INDEX_ZERO;

public class ShortMap<V> implements Iterable<ShortHolder<V>>, Eachable<ShortHolder<V>>, Cloneable {
	public final Class<V> valueComponentType;

	public int size;

	protected short[] keyTable;
	protected V[] valueTable;

	protected V zeroValue;
	protected boolean hasZeroValue;

	protected float loadFactor;
	protected int threshold;

	protected int shift;

	protected int mask;

	protected transient Entries entries1, entries2;
	protected transient Values values1, values2;
	protected transient Keys keys1, keys2;

	@SuppressWarnings("unchecked")
	public static <V> ShortMap<V> of(Class<V> keyType, Object... values) {
		ShortMap<V> map = new ShortMap<>(keyType);

		for (int i = 0; i < values.length / 2; i++) {
			Object key = values[i * 2];
			short keyInt = (short) (key instanceof Character character ? character.charValue() : key);
			map.put(keyInt, (V) values[i * 2 + 1]);
		}

		return map;
	}

	@SuppressWarnings("unchecked")
	public ShortMap<V> copy() {
		try {
			ShortMap<V> out = (ShortMap<V>) super.clone();
			out.keyTable = keyTable.clone();
			out.valueTable = valueTable.clone();

			out.entries1 = out.entries2 = null;
			out.values1 = out.values2 = null;
			out.keys1 = out.keys2 = null;

			return out;
		} catch (CloneNotSupportedException e) {
			return new ShortMap<>(this);
		}
	}

	public ShortMap() {
		this(Object.class);
	}

	public ShortMap(Class<?> keyType) {
		this(51, 0.8f, keyType);
	}

	public ShortMap(int initialCapacity, Class<?> keyType) {
		this(initialCapacity, 0.8f, keyType);
	}

	@SuppressWarnings("unchecked")
	public ShortMap(int initialCapacity, float loadFactor, Class<?> keyType) {
		if (loadFactor <= 0f || loadFactor >= 1f)
			throw new IllegalArgumentException("loadFactor must be > 0 and < 1: " + loadFactor);
		this.loadFactor = loadFactor;

		int tableSize = tableSize(initialCapacity, loadFactor);
		threshold = (int) (tableSize * loadFactor);
		mask = tableSize - 1;
		shift = Long.numberOfLeadingZeros(mask);

		valueComponentType = (Class<V>) keyType;

		keyTable = new short[tableSize];
		valueTable = (V[]) Array.newInstance(keyType, tableSize);
	}

	public ShortMap(ShortMap<? extends V> map) {
		this((int) (map.keyTable.length * map.loadFactor), map.loadFactor, map.valueComponentType);
		System.arraycopy(map.keyTable, 0, keyTable, 0, map.keyTable.length);
		System.arraycopy(map.valueTable, 0, valueTable, 0, map.valueTable.length);
		size = map.size;
		zeroValue = map.zeroValue;
		hasZeroValue = map.hasZeroValue;
	}

	@Override
	public void each(Cons<? super ShortHolder<V>> cons) {
		for (ShortHolder<V> entry : entries()) {
			cons.get(entry);
		}
	}

	protected int place(short item) {
		return (int) (item * 0x9E3779B97F4A7C15L >>> shift);
	}

	protected int locateKey(short key) {
		short[] ks = keyTable;
		for (int i = place(key); ; i = i + 1 & mask) {
			int other = ks[i];
			if (other == 0) return -(i + 1);
			if (other == key) return i;
		}
	}

	public V put(short key, V value) {
		if (key == 0) {
			V oldValue = zeroValue;
			zeroValue = value;
			if (!hasZeroValue) {
				hasZeroValue = true;
				size++;
			}
			return oldValue;
		}
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

	public V putMissing(short key, V value) {
		if (key == 0) {
			V oldValue = zeroValue;
			if (!hasZeroValue) {
				zeroValue = value;
				hasZeroValue = true;
				size++;
			}
			return oldValue;
		}
		int i = locateKey(key);
		if (i >= 0) return valueTable[i];
		i = -(i + 1);
		keyTable[i] = key;
		valueTable[i] = value;
		if (++size >= threshold) resize(keyTable.length << 1);
		return null;
	}

	public void putAll(ShortMap<? extends V> map) {
		ensureCapacity(map.size);
		if (map.hasZeroValue) put((short) 0, map.zeroValue);
		short[] keyTable = map.keyTable;
		V[] valueTable = map.valueTable;
		for (int i = 0, n = keyTable.length; i < n; i++) {
			short key = keyTable[i];
			if (key != 0) put(key, valueTable[i]);
		}
	}

	protected void putResize(short key, V value) {
		short[] ks = keyTable;
		for (int i = place(key); ; i = (i + 1) & mask) {
			if (ks[i] == 0) {
				ks[i] = key;
				valueTable[i] = value;
				return;
			}
		}
	}

	public V get(short key) {
		if (key == 0) return hasZeroValue ? zeroValue : null;
		int i = locateKey(key);
		return i >= 0 ? valueTable[i] : null;
	}

	public V get(short key, V defaultValue) {
		if (key == 0) return hasZeroValue ? zeroValue : defaultValue;
		int i = locateKey(key);
		return i >= 0 ? valueTable[i] : defaultValue;
	}

	public V get(short key, Prov<V> defaultValue) {
		V out = get(key);
		if (out == null) {
			out = defaultValue.get();
			put(key, out);
		}
		return out;
	}

	public V remove(short key) {
		if (key == 0) {
			if (!hasZeroValue) return null;
			hasZeroValue = false;
			V oldValue = zeroValue;
			zeroValue = null;
			size--;
			return oldValue;
		}

		int i = locateKey(key);
		if (i < 0) return null;
		short[] ks = keyTable;
		V[] vs = valueTable;
		V oldValue = vs[i];
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
		vs[i] = null;
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
		zeroValue = null;
		resize(tableSize);
	}

	public void clear() {
		if (size == 0) return;
		size = 0;
		Arrays.fill(keyTable, (short) 0);
		Arrays.fill(valueTable, null);
		zeroValue = null;
		hasZeroValue = false;
	}

	public boolean containsValue(Object value, boolean identity) {
		V[] vs = valueTable;
		if (value == null) {
			if (hasZeroValue && zeroValue == null) return true;
			short[] ks = keyTable;
			for (int i = vs.length - 1; i >= 0; i--)
				if (ks[i] != 0 && vs[i] == null) return true;
		} else if (identity) {
			if (value == zeroValue) return true;
			for (int i = vs.length - 1; i >= 0; i--)
				if (vs[i] == value) return true;
		} else {
			if (hasZeroValue && value.equals(zeroValue)) return true;
			for (int i = vs.length - 1; i >= 0; i--)
				if (value.equals(vs[i])) return true;
		}
		return false;
	}

	public boolean containsKey(short key) {
		if (key == 0) return hasZeroValue;
		return locateKey(key) >= 0;
	}

	public short findKey(Object value, boolean identity, short notFound) {
		V[] vs = valueTable;
		if (value == null) {
			if (hasZeroValue && zeroValue == null) return 0;
			short[] ks = keyTable;
			for (int i = vs.length - 1; i >= 0; i--)
				if (ks[i] != 0 && vs[i] == null) return ks[i];
		} else if (identity) {
			if (value == zeroValue) return 0;
			for (int i = vs.length - 1; i >= 0; i--)
				if (vs[i] == value) return keyTable[i];
		} else {
			if (hasZeroValue && value.equals(zeroValue)) return 0;
			for (int i = vs.length - 1; i >= 0; i--)
				if (value.equals(vs[i])) return keyTable[i];
		}
		return notFound;
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

		short[] oldKeyTable = keyTable;
		V[] oldValueTable = valueTable;

		keyTable = new short[newSize];
		valueTable = (V[]) new Object[newSize];

		if (size > 0) {
			for (int i = 0; i < oldCapacity; i++) {
				short key = oldKeyTable[i];
				if (key != 0) putResize(key, oldValueTable[i]);
			}
		}
	}

	@Override
	public int hashCode() {
		int h = size;
		if (hasZeroValue && zeroValue != null) h += zeroValue.hashCode();
		short[] ks = keyTable;
		V[] vs = valueTable;
		for (int i = 0, n = ks.length; i < n; i++) {
			int key = ks[i];
			if (key != 0) {
				h += key * 31;
				V value = vs[i];
				if (value != null) h += value.hashCode();
			}
		}
		return h;
	}

	@Override
	public boolean equals(Object o) {
		if (o == this) return true;
		if (!(o instanceof ShortMap<?> other)) return false;
		if (other.size != size) return false;
		if (other.hasZeroValue != hasZeroValue) return false;
		if (hasZeroValue) {
			if (other.zeroValue == null) {
				if (zeroValue != null) return false;
			} else {
				if (!other.zeroValue.equals(zeroValue)) return false;
			}
		}
		short[] ks = keyTable;
		V[] vs = valueTable;
		for (int i = 0, n = ks.length; i < n; i++) {
			short key = ks[i];
			if (key != 0) {
				V value = vs[i];
				if (value == null) {
					if (other.get(key) != null) return false;
				} else {
					if (!value.equals(other.get(key))) return false;
				}
			}
		}
		return true;
	}

	public boolean equalsIdentity(Object o) {
		if (o == this) return true;
		if (!(o instanceof ShortMap<?> other)) return false;
		if (other.size != size) return false;
		if (other.hasZeroValue != hasZeroValue) return false;
		if (hasZeroValue && zeroValue != other.zeroValue) return false;
		short[] ks = keyTable;
		V[] vs = valueTable;
		for (int i = 0, n = ks.length; i < n; i++) {
			short key = ks[i];
			if (key != 0 && vs[i] != other.get(key)) return false;
		}
		return true;
	}

	@Override
	public String toString() {
		if (size == 0) return "[]";
		StringBuilder buffer = new StringBuilder(32);
		buffer.append('[');
		short[] ks = keyTable;
		V[] vs = valueTable;
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
	public Iterator<ShortHolder<V>> iterator() {
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
			currentIndex = INDEX_ILLEGAL;
			nextIndex = INDEX_ZERO;
			if (hasZeroValue)
				hasNext = true;
			else
				findNextIndex();
		}

		protected void findNextIndex() {
			short[] ks = keyTable;
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
				zeroValue = null;
			} else if (i < 0) {
				throw new IllegalStateException("next must be called before remove.");
			} else {
				short[] ks = keyTable;
				V[] vs = valueTable;
				int m = mask, next = i + 1 & m;
				short key;
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
				vs[i] = null;
				if (i != currentIndex) --nextIndex;
			}
			currentIndex = INDEX_ILLEGAL;
			size--;
		}
	}

	public class Entries extends MapIterator implements Iterable<ShortHolder<V>>, Iterator<ShortHolder<V>> {
		protected ShortHolder<V> entry = new ShortHolder<>();

		@Override
		public ShortHolder<V> next() {
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
		public Iterator<ShortHolder<V>> iterator() {
			return this;
		}
	}

	public class Values extends MapIterator implements Iterable<V>, Iterator<V> {
		@Override
		public boolean hasNext() {
			if (!valid) throw new ArcRuntimeException("#iterator() cannot be used nested.");
			return hasNext;
		}

		@Override
		public V next() {
			if (!hasNext) throw new NoSuchElementException();
			if (!valid) throw new ArcRuntimeException("#iterator() cannot be used nested.");
			V value;
			if (nextIndex == INDEX_ZERO)
				value = zeroValue;
			else
				value = valueTable[nextIndex];
			currentIndex = nextIndex;
			findNextIndex();
			return value;
		}

		@Override
		public Iterator<V> iterator() {
			return this;
		}

		public Seq<V> toSeq() {
			Seq<V> array = new Seq<>(true, size, valueComponentType);
			while (hasNext)
				array.add(next());
			return array;
		}
	}

	public class Keys extends MapIterator {
		public short next() {
			if (!hasNext) throw new NoSuchElementException();
			if (!valid) throw new ArcRuntimeException("#iterator() cannot be used nested.");
			short key = nextIndex == INDEX_ZERO ? 0 : keyTable[nextIndex];
			currentIndex = nextIndex;
			findNextIndex();
			return key;
		}

		public ShortSeq toSeq() {
			ShortSeq array = new ShortSeq(true, size);
			while (hasNext)
				array.add(next());
			return array;
		}
	}
}
