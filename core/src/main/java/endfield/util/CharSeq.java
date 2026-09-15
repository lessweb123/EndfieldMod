package endfield.util;

import arc.math.Mathf;
import arc.util.Strings;

import java.util.Arrays;

public class CharSeq implements CharSequence, Appendable, Cloneable {
	public char[] items;
	public int size;
	public boolean ordered;

	public CharSeq() {
		this(true, 16);
	}

	public CharSeq(int capacity) {
		this(true, capacity);
	}

	public CharSeq(boolean ordered, int capacity) {
		this.ordered = ordered;
		items = new char[capacity];
	}

	public CharSeq(CharSeq array) {
		ordered = array.ordered;
		size = array.size;
		items = new char[size];
		System.arraycopy(array.items, 0, items, 0, size);
	}

	public CharSeq(char[] array) {
		this(true, array, 0, array.length);
	}

	public CharSeq(boolean ordered, char[] array, int startIndex, int count) {
		this(ordered, count);
		size = count;
		System.arraycopy(array, startIndex, items, 0, count);
	}

	public static CharSeq with(char... array) {
		return new CharSeq(array);
	}

	public CharSeq copy() {
		try {
			CharSeq out = (CharSeq) super.clone();

			out.items = new char[size];
			System.arraycopy(items, 0, out.items, 0, size);
			return out;
		} catch (CloneNotSupportedException e) {
			return new CharSeq(this);
		}
	}

	public void add(char value) {
		char[] cs = items;
		if (size == cs.length) cs = resize(Math.max(8, (int) (size * 1.75f)));
		cs[size++] = value;
	}

	public void add(char value1, char value2) {
		char[] cs = items;
		if (size + 1 >= cs.length) cs = resize(Math.max(8, (int) (size * 1.75f)));
		cs[size] = value1;
		cs[size + 1] = value2;
		size += 2;
	}

	public void add(char value1, char value2, char value3) {
		char[] cs = items;
		if (size + 2 >= cs.length) cs = resize(Math.max(8, (int) (size * 1.75f)));
		cs[size] = value1;
		cs[size + 1] = value2;
		cs[size + 2] = value3;
		size += 3;
	}

	public void add(char value1, char value2, char value3, char value4) {
		char[] cs = items;
		if (size + 3 >= cs.length)
			cs = resize(Math.max(8, (int) (size * 1.8f))); // 1.75 isn't enough when size=5.
		cs[size] = value1;
		cs[size + 1] = value2;
		cs[size + 2] = value3;
		cs[size + 3] = value4;
		size += 4;
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
		char[] cs = items;
		int sizeNeeded = size + length;
		if (sizeNeeded > cs.length) cs = resize(Math.max(8, (int) (sizeNeeded * 1.75f)));
		System.arraycopy(array, offset, cs, size, length);
		size += length;
	}

	public void add(String str) {
		if (str == null) return;

		add(str, 0, str.length());
	}

	public void add(String str, int start, int end) {
		if (str == null || start < 0 || start > end) return;

		int length = end - start;
		if (length == 0) return;

		int sizeNeeded = size + length;
		if (sizeNeeded > items.length) resize(Math.max(8, (int) (sizeNeeded * 1.75f)));

		str.getChars(start, end, items, size);
		size += length;
	}

	public void addAll(String... array) {
		if (array == null) return;

		int totalLength = 0;
		for (String str : array) {
			totalLength += str == null ? 0 : str.length();
		}

		if (totalLength == 0) return;

		int sizeNeeded = size + totalLength;
		if (sizeNeeded > items.length) resize(Math.max(8, (int) (sizeNeeded * 1.75f)));

		for (String str : array) {
			if (str != null && !str.isEmpty()) {
				int length = str.length();
				str.getChars(0, length, items, size);
				size += length;
			}
		}
	}

	public char get(int index) {
		if (index >= size) throw new IndexOutOfBoundsException("index can't be >= size: " + index + " >= " + size);
		return items[index];
	}

	public char get(int index, char def) {
		if (index >= size) return def;
		return items[index];
	}

	public void set(int index, char value) {
		if (index >= size) throw new IndexOutOfBoundsException("index can't be >= size: " + index + " >= " + size);
		items[index] = value;
	}

	public void incr(int index, char value) {
		if (index >= size) throw new IndexOutOfBoundsException("index can't be >= size: " + index + " >= " + size);
		items[index] += value;
	}

	public void mul(int index, char value) {
		if (index >= size) throw new IndexOutOfBoundsException("index can't be >= size: " + index + " >= " + size);
		items[index] *= value;
	}

	public void insert(int index, char value) {
		if (index > size) throw new IndexOutOfBoundsException("index can't be > size: " + index + " > " + size);
		char[] cs = items;
		if (size == cs.length) cs = resize(Math.max(8, (int) (size * 1.75f)));
		if (ordered)
			System.arraycopy(cs, index, cs, index + 1, size - index);
		else
			cs[size] = cs[index];
		size++;
		cs[index] = value;
	}

	public void swap(int first, int second) {
		if (first >= size) throw new IndexOutOfBoundsException("first can't be >= size: " + first + " >= " + size);
		if (second >= size) throw new IndexOutOfBoundsException("second can't be >= size: " + second + " >= " + size);
		char[] cs = items;
		char firstValue = cs[first];
		cs[first] = cs[second];
		cs[second] = firstValue;
	}

	public boolean contains(char value) {
		int i = size - 1;
		char[] cs = items;
		while (i >= 0)
			if (cs[i--] == value) return true;
		return false;
	}

	public int indexOf(char value) {
		char[] cs = items;
		for (int i = 0, n = size; i < n; i++)
			if (cs[i] == value) return i;
		return -1;
	}

	public int lastIndexOf(char value) {
		char[] cs = items;
		for (int i = size - 1; i >= 0; i--)
			if (cs[i] == value) return i;
		return -1;
	}

	public boolean removeValue(char value) {
		char[] cs = items;
		for (int i = 0, n = size; i < n; i++) {
			if (cs[i] == value) {
				removeIndex(i);
				return true;
			}
		}
		return false;
	}

	public char removeIndex(int index) {
		if (index >= size) throw new IndexOutOfBoundsException("index can't be >= size: " + index + " >= " + size);
		char[] cs = items;
		char value = cs[index];
		size--;
		if (ordered)
			System.arraycopy(cs, index + 1, cs, index, size - index);
		else
			cs[index] = cs[size];
		return value;
	}

	public void removeRange(int start, int end) {
		if (end >= size) throw new IndexOutOfBoundsException("end can't be >= size: " + end + " >= " + size);
		if (start > end) throw new IndexOutOfBoundsException("start can't be > end: " + start + " > " + end);
		char[] cs = items;
		int count = end - start + 1;
		if (ordered)
			System.arraycopy(cs, start + count, cs, start, size - (start + count));
		else {
			int lastIndex = size - 1;
			for (int i = 0; i < count; i++)
				cs[start + i] = cs[lastIndex - i];
		}
		size -= count;
	}

	public boolean removeAll(CharSeq array) {
		int theSize = size;
		int startSize = theSize;
		char[] cs = items;
		for (int i = 0, n = array.size; i < n; i++) {
			char item = array.get(i);
			for (int ii = 0; ii < theSize; ii++) {
				if (item == cs[ii]) {
					removeIndex(ii);
					theSize--;
					break;
				}
			}
		}
		return theSize != startSize;
	}

	public char pop() {
		return items[--size];
	}

	public char peek() {
		return items[size - 1];
	}

	public char first() {
		if (size == 0) throw new IllegalStateException("Array is empty.");
		return items[0];
	}

	@Override
	public char charAt(int index) {
		if (index < 0 || index >= size)
			throw new StringIndexOutOfBoundsException(Strings.format("index @, size @", index, size));

		return items[index];
	}

	@Override
	public CharSeq subSequence(int start, int end) {
		if (start < 0 || start > end || end > size)
			throw new StringIndexOutOfBoundsException(Strings.format("start @, end @, size @", start, end, size));
		if (start == end) return new CharSeq(0);

		CharSeq out = new CharSeq( end - start);
		System.arraycopy(items, start, out.items, 0, end - start);
		return out;
	}

	@Override
	public int length() {
		return size;
	}

	@Override
	public boolean isEmpty() {
		return size == 0;
	}

	@Override
	public CharSeq append(char c) {
		add(c);
		return this;
	}

	@Override
	public CharSeq append(CharSequence csq) {
		if (csq == null) {
			add("null");
		} else {
			for (int i = 0; i < csq.length(); i++) {
				add(csq.charAt(i));
			}
		}

		return this;
	}

	@Override
	public CharSeq append(CharSequence csq, int start, int end) {
		if (csq == null) {
			add("null");
		} else {
			for (int i = start; i < end; i++) {
				add(csq.charAt(i));
			}
		}

		return this;
	}

	public void clear() {
		size = 0;
	}

	public char sum() {
		char s = 0;
		for (int i = 0; i < size; i++) {
			s += items[i];
		}
		return s;
	}

	public char[] shrink() {
		if (items.length != size) resize(size);
		return items;
	}

	public char[] ensureCapacity(int additionalCapacity) {
		if (additionalCapacity < 0)
			throw new IllegalArgumentException("additionalCapacity must be >= 0: " + additionalCapacity);
		int sizeNeeded = size + additionalCapacity;
		if (sizeNeeded > items.length) resize(Math.max(8, sizeNeeded));
		return items;
	}

	public char[] setSize(int newSize) {
		if (newSize < 0) throw new IllegalArgumentException("newSize must be >= 0: " + newSize);
		if (newSize > items.length) resize(Math.max(8, newSize));
		size = newSize;
		return items;
	}

	protected char[] resize(int newSize) {
		char[] newItems = new char[newSize];
		System.arraycopy(items, 0, newItems, 0, Math.min(size, newItems.length));
		items = newItems;
		return newItems;
	}

	public void sort() {
		Arrays.sort(items, 0, size);
	}

	public void reverse() {
		char[] cs = items;
		for (int i = 0, lastIndex = size - 1, n = size / 2; i < n; i++) {
			int ii = lastIndex - i;
			char temp = cs[i];
			cs[i] = cs[ii];
			cs[ii] = temp;
		}
	}

	public void shuffle() {
		char[] cs = items;
		for (int i = size - 1; i >= 0; i--) {
			int ii = Mathf.random(i);
			char temp = cs[i];
			cs[i] = cs[ii];
			cs[ii] = temp;
		}
	}

	public void truncate(int newSize) {
		if (size > newSize) size = newSize;
	}

	public char random() {
		if (size == 0) return 0;
		return items[Mathf.random(0, size - 1)];
	}

	public char[] toArray() {
		char[] array = new char[size];
		System.arraycopy(items, 0, array, 0, size);
		return array;
	}

	@Override
	public int hashCode() {
		if (!ordered) return super.hashCode();
		int hashCode = 1;
		for (int i = 0; i < size; i++)
			hashCode = hashCode * 31 + (int) items[i];
		return hashCode;
	}

	@Override
	public boolean equals(Object o) {
		if (o == this) return true;
		if (!ordered || !(o instanceof CharSeq array) || !array.ordered) return false;
		if (size != array.size) return false;
		char[] cs = array.items;
		char[] otherItems = array.items;
		for (int i = 0; i < size; i++)
			if (cs[i] != otherItems[i]) return false;
		return true;
	}

	@Override
	public String toString() {
		// We need to implement the requirements of the CharSequence interface.
		return String.valueOf(Arrays.copyOfRange(items, 0, size));
	}

	public String asString() {
		if (size == 0) return "[]";
		char[] cs = items;
		StringBuilder buffer = new StringBuilder(32);
		buffer.append('[');
		buffer.append(cs[0]);
		for (int i = 1; i < size; i++) {
			buffer.append(", ");
			buffer.append(cs[i]);
		}
		buffer.append(']');
		return buffer.toString();
	}
}
