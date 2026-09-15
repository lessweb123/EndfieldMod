package endfield.util;

import arc.func.Boolf;
import arc.func.Cons;
import arc.func.Floatf;
import arc.func.Func;
import arc.func.Func2;
import arc.func.Intf;
import arc.func.Prov;
import arc.math.Mathf;
import arc.math.Rand;
import arc.struct.FloatSeq;
import arc.struct.IntSeq;
import arc.struct.Sort;
import arc.util.ArcRuntimeException;
import arc.util.Eachable;
import arc.util.Structs;
import endfield.math.Mathm;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Array;
import java.util.AbstractList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.UnaryOperator;

/**
 * Implementation of Java Collection Framework {@code List} based on {@code Seq}, used in places that require Java
 * specifications and the feature of {@code Seq} not creating nodes.
 */
public class CollectionList<E> extends AbstractList<E> implements Eachable<E>, Cloneable {
	public static int iteratorsAllocated = 0;

	public final Class<E> componentType;

	//public final boolean specifiedType

	public E[] items;

	public int size;
	public boolean ordered;

	protected transient Iter iterator1, iterator2, lastIterator1, lastIterator2;

	public CollectionList() {
		this(Object.class);
		//specifiedType = true;
	}

	public CollectionList(Class<?> type) {
		this(16, type);
	}

	public CollectionList(int capacity, Class<?> type) {
		this(capacity, true, type);
	}

	@SuppressWarnings("unchecked")
	public CollectionList(int capacity, boolean ordered, Class<?> type) {
		this.ordered = ordered;
		componentType = (Class<E>) type;
		items = (E[]) Array.newInstance(type, capacity);
	}

	public CollectionList(CollectionList<? extends E> array) {
		this(array.size, array.ordered, array.componentType);
		size = array.size;
		System.arraycopy(array.items, 0, items, 0, size);
	}

	public CollectionList(E[] array, int start, int count) {
		this(array, start, count, true);
	}

	public CollectionList(E[] array, int start, int count, boolean ordered) {
		this(count, ordered, array.getClass().getComponentType());
		size = count;
		System.arraycopy(array, start, items, 0, size);
	}

	@SuppressWarnings("unchecked")
	public CollectionList(E[] array) {
		componentType = (Class<E>) array.getClass().getComponentType();
		size = array.length;
		items = array;
	}

	public CollectionList(Collection<? extends E> collection, Class<?> type) {
		this(collection.size(), type);
		addAll(collection);
	}

	@SuppressWarnings("unchecked")
	public static <T> CollectionList<T> withArrays(Class<?> arrayType, Object... arrays) {
		CollectionList<T> result = new CollectionList<>(arrayType);
		for (Object a : arrays) {
			if (a instanceof CollectionList<?>) {
				result.addAll((CollectionList<? extends T>) a);
			} else {
				result.add((T) a);
			}
		}
		return result;
	}

	@SafeVarargs
	public static <T> CollectionList<T> with(T... array) {
		return new CollectionList<>(array);
	}

	public static <T> CollectionList<T> with(Class<?> arrayType, Iterable<? extends T> array) {
		CollectionList<T> out = new CollectionList<>(arrayType);
		for (T thing : array) {
			out.add(thing);
		}
		return out;
	}

	public static <T> CollectionList<T> select(T[] array, Boolf<? super T> test) {
		CollectionList<T> out = new CollectionList<>(array.length, array.getClass().getComponentType());
		for (T t : array) {
			if (test.get(t)) {
				out.add(t);
			}
		}
		return out;
	}

	public <K, V> CollectionObjectMap<K, V> asMap(Func<? super E, ? extends K> keygen, Func<? super E, ? extends V> valgen, Class<?> keyType, Class<?> valueType) {
		CollectionObjectMap<K, V> map = new CollectionObjectMap<>(keyType, valueType);
		for (int i = 0; i < size; i++) {
			map.put(keygen.get(items[i]), valgen.get(items[i]));
		}
		return map;
	}

	public <K> CollectionObjectMap<K, E> asMap(Func<? super E, ? extends K> keygen, Class<?> keyType, Class<?> valueType) {
		return asMap(keygen, t -> t, keyType, valueType);
	}

	public CollectionObjectSet<E> asSet() {
		return CollectionObjectSet.with(this);
	}

	@SuppressWarnings("unchecked")
	public CollectionList<E> copy() {
		try {
			CollectionList<E> out = (CollectionList<E>) super.clone();
			out.items = items.clone();

			// These fields must be reset.
			out.iterator1 = out.iterator2 = null;
			out.lastIterator1 = out.lastIterator2 = null;

			return out;
		} catch (CloneNotSupportedException e) {
			return new CollectionList<>(this);
		}
	}

	public float sumf(Floatf<? super E> summer) {
		float sum = 0f;
		for (int i = 0; i < size; i++) {
			sum += summer.get(items[i]);
		}
		return sum;
	}

	public int sum(Intf<? super E> summer) {
		int sum = 0;
		for (int i = 0; i < size; i++) {
			sum += summer.get(items[i]);
		}
		return sum;
	}

	public void each(Boolf<? super E> predicate, Cons<? super E> consumer) {
		for (int i = 0; i < size; i++) {
			if (predicate.get(items[i])) consumer.get(items[i]);
		}
	}

	@Override
	public void each(Cons<? super E> consumer) {
		for (int i = 0; i < size; i++) {
			consumer.get(items[i]);
		}
	}

	@Override
	public void forEach(Consumer<? super E> action) {
		for (int i = 0; i < size; i++) {
			action.accept(items[i]);
		}
	}

	public void replace(Func<? super E, ? extends E> mapper) {
		for (int i = 0; i < size; i++) {
			items[i] = mapper.get(items[i]);
		}
	}

	@SuppressWarnings("unchecked")
	public <R> CollectionList<R> flatten() {
		CollectionList<R> arr = new CollectionList<>(size, componentType);
		for (int i = 0; i < size; i++) {
			arr.addAll((CollectionList<R>) items[i]);
		}
		return arr;
	}

	public <R> CollectionList<R> flatMap(Func<? super E, Iterable<? extends R>> mapper) {
		CollectionList<R> arr = new CollectionList<>(size, componentType);
		for (int i = 0; i < size; i++) {
			arr.addAll(mapper.get(items[i]));
		}
		return arr;
	}

	public <R> CollectionList<R> map(Func<? super E, ? extends R> mapper) {
		CollectionList<R> arr = new CollectionList<>(size, componentType);
		for (int i = 0; i < size; i++) {
			arr.add(mapper.get(items[i]));
		}
		return arr;
	}

	public IntSeq mapInt(Intf<? super E> mapper) {
		IntSeq arr = new IntSeq(size);
		for (int i = 0; i < size; i++) {
			arr.add(mapper.get(items[i]));
		}
		return arr;
	}

	public IntSeq mapInt(Intf<? super E> mapper, Boolf<? super E> retain) {
		IntSeq arr = new IntSeq(size);
		for (int i = 0; i < size; i++) {
			E item = items[i];
			if (retain.get(item)) {
				arr.add(mapper.get(item));
			}
		}
		return arr;
	}

	public FloatSeq mapFloat(Floatf<? super E> mapper) {
		FloatSeq arr = new FloatSeq(size);
		for (int i = 0; i < size; i++) {
			arr.add(mapper.get(items[i]));
		}
		return arr;
	}

	public <R> R reduce(R initial, Func2<? super E, ? super R, ? extends R> reducer) {
		R result = initial;
		for (int i = 0; i < size; i++) {
			result = reducer.get(items[i], result);
		}
		return result;
	}

	public boolean allMatch(Boolf<? super E> predicate) {
		for (int i = 0; i < size; i++) {
			if (!predicate.get(items[i])) {
				return false;
			}
		}
		return true;
	}

	public boolean contains(Boolf<? super E> predicate) {
		for (int i = 0; i < size; i++) {
			if (predicate.get(items[i])) {
				return true;
			}
		}
		return false;
	}

	public E min(Comparator<? super E> func) {
		E result = null;
		for (int i = 0; i < size; i++) {
			E t = items[i];
			if (result == null || func.compare(result, t) > 0) {
				result = t;
			}
		}
		return result;
	}

	public E max(Comparator<? super E> func) {
		E result = null;
		for (int i = 0; i < size; i++) {
			E t = items[i];
			if (result == null || func.compare(result, t) < 0) {
				result = t;
			}
		}
		return result;
	}

	public E min(Boolf<? super E> filter, Floatf<? super E> func) {
		E result = null;
		float min = Float.MAX_VALUE;
		for (int i = 0; i < size; i++) {
			E t = items[i];
			if (!filter.get(t)) continue;
			float val = func.get(t);
			if (val <= min) {
				result = t;
				min = val;
			}
		}
		return result;
	}

	public E min(Boolf<? super E> filter, Comparator<? super E> func) {
		E result = null;
		for (int i = 0; i < size; i++) {
			E t = items[i];
			if (filter.get(t) && (result == null || func.compare(result, t) > 0)) {
				result = t;
			}
		}
		return result;
	}

	public E min(Floatf<? super E> func) {
		E result = null;
		float min = Float.MAX_VALUE;
		for (int i = 0; i < size; i++) {
			E t = items[i];
			float val = func.get(t);
			if (val <= min) {
				result = t;
				min = val;
			}
		}
		return result;
	}

	public E max(Floatf<? super E> func) {
		E result = null;
		float max = Float.NEGATIVE_INFINITY;
		for (int i = 0; i < size; i++) {
			E t = items[i];
			float val = func.get(t);
			if (val >= max) {
				result = t;
				max = val;
			}
		}
		return result;
	}

	public @Nullable E find(Boolf<? super E> predicate) {
		for (int i = 0; i < size; i++) {
			if (predicate.get(items[i])) {
				return items[i];
			}
		}
		return null;
	}

	public CollectionList<E> with(Cons<? super CollectionList<E>> cons) {
		cons.get(this);
		return this;
	}

	public boolean addUnique(E value) {
		if (!contains(value)) {
			add(value);
			return true;
		}
		return false;
	}

	@Override
	public boolean add(E value) {
		E[] es = items;
		if (size == es.length) es = resize(Math.max(8, (int) (size * 1.75f)));
		es[size++] = value;
		return true;
	}

	public boolean add(E value1, E value2) {
		E[] es = items;
		if (size + 1 >= es.length) es = resize(Math.max(8, (int) (size * 1.75f)));
		es[size] = value1;
		es[size + 1] = value2;
		size += 2;
		return true;
	}

	public boolean add(E value1, E value2, E value3) {
		E[] es = items;
		if (size + 2 >= es.length) es = resize(Math.max(8, (int) (size * 1.75f)));
		es[size] = value1;
		es[size + 1] = value2;
		es[size + 2] = value3;
		size += 3;
		return true;
	}

	public boolean add(E value1, E value2, E value3, E value4) {
		E[] es = items;
		if (size + 3 >= es.length)
			es = resize(Math.max(8, (int) (size * 1.8f)));
		es[size] = value1;
		es[size + 1] = value2;
		es[size + 2] = value3;
		es[size + 3] = value4;
		size += 4;
		return true;
	}

	public void add(CollectionList<? extends E> array) {
		addAll(array.items, 0, array.size);
	}

	public void add(E[] array) {
		addAll(array, 0, array.length);
	}

	public void addAll(CollectionList<? extends E> array) {
		addAll(array.items, 0, array.size);
	}

	public void addAll(CollectionList<? extends E> array, int start, int count) {
		if (start + count > array.size)
			throw new IllegalArgumentException("start + count must be <= size: " + start + " + " + count + " <= " + array.size);
		addAll(array.items, start, count);
	}

	@SuppressWarnings("unchecked")
	public void addAll(E... array) {
		addAll(array, 0, array.length);
	}

	public void addAll(E[] array, int start, int count) {
		int sizeNeeded = size + count;
		if (sizeNeeded > items.length) items = resize(Math.max(8, (int) (sizeNeeded * 1.75f)));
		System.arraycopy(array, start, items, size, count);
		size += count;
	}

	@Override
	public boolean addAll(Collection<? extends E> c) {
		return super.addAll(c);
	}

	public void addAll(Iterable<? extends E> items) {
		if (items instanceof CollectionList<? extends E> list) {
			addAll(list);
		} else {
			for (E t : items) {
				add(t);
			}
		}
	}

	public void set(CollectionList<? extends E> array) {
		clear();
		addAll(array);
	}

	public void set(E[] array) {
		clear();
		addAll(array);
	}

	public E getFrac(float index) {
		if (isEmpty()) return null;
		return get(Mathm.clamp((int) (index * size), 0, size - 1));
	}

	@Override
	public E get(int index) {
		if (index >= size) throw new IndexOutOfBoundsException("index can't be >= size: " + index + " >= " + size);
		return items[index];
	}

	public E get(int index, E def) {
		if (index >= size || index <= 0) return def;
		return items[index];
	}

	@Override
	public E set(int index, E element) {
		if (index >= size) throw new IndexOutOfBoundsException("index can't be >= size: " + index + " >= " + size);
		E value = items[index];
		items[index] = element;
		return value;
	}

	@Override
	public void add(int index, E element) {
		insert(index, element);
	}

	public void insert(int index, E element) {
		if (index > size) throw new IndexOutOfBoundsException("index can't be > size: " + index + " > " + size);
		E[] es = items;
		if (size == es.length) es = resize(Math.max(8, (int) (size * 1.75f)));
		if (ordered)
			System.arraycopy(es, index, es, index + 1, size - index);
		else
			es[size] = es[index];
		size++;
		es[index] = element;
	}

	public void swap(int first, int second) {
		if (first >= size) throw new IndexOutOfBoundsException("first can't be >= size: " + first + " >= " + size);
		if (second >= size) throw new IndexOutOfBoundsException("second can't be >= size: " + second + " >= " + size);
		E[] es = items;
		E firstValue = es[first];
		es[first] = es[second];
		es[second] = firstValue;
	}

	public boolean replace(E from, E to) {
		int idx = indexOf(from);
		if (idx != -1) {
			items[idx] = to;
			return true;
		}
		return false;
	}

	public boolean containsAll(CollectionList<E> list) {
		return containsAll(list, false);
	}

	public boolean containsAll(CollectionList<E> list, boolean identity) {
		E[] others = list.items;

		for (int i = 0; i < list.size; i++) {
			if (!contains(others[i], identity)) {
				return false;
			}
		}
		return true;
	}

	@Override
	public boolean contains(Object o) {
		return contains(o, false);
	}

	public boolean contains(Object o, boolean identity) {
		E[] es = items;
		int i = size - 1;
		if (identity || o == null) {
			while (i >= 0)
				if (es[i--] == o) return true;
		} else {
			while (i >= 0)
				if (o.equals(es[i--])) return true;
		}
		return false;
	}

	@Override
	public int indexOf(Object o) {
		E[] es = items;
		if (o == null) {
			for (int i = 0, n = size; i < n; i++)
				if (es[i] == null) return i;
		} else {
			for (int i = 0, n = size; i < n; i++)
				if (o.equals(es[i])) return i;
		}
		return -1;
	}

	@Override
	public int lastIndexOf(Object o) {
		E[] es = items;
		if (o == null) {
			for (int i = size; i >= 0; i--)
				if (es[i] == null) return i;
		} else {
			for (int i = size; i >= 0; i--)
				if (o.equals(es[i])) return i;
		}
		return -1;
	}

	public int indexOf(Object o, boolean identity) {
		E[] es = items;
		if (identity || o == null) {
			for (int i = 0, n = size; i < n; i++)
				if (es[i] == o) return i;
		} else {
			for (int i = 0, n = size; i < n; i++)
				if (o.equals(es[i])) return i;
		}
		return -1;
	}

	public int indexOf(Boolf<E> value) {
		E[] es = items;
		for (int i = 0, n = size; i < n; i++)
			if (value.get(es[i])) return i;
		return -1;
	}

	public int lastIndexOf(Object o, boolean identity) {
		E[] es = items;
		if (identity || o == null) {
			for (int i = size - 1; i >= 0; i--)
				if (es[i] == o) return i;
		} else {
			for (int i = size - 1; i >= 0; i--)
				if (o.equals(es[i])) return i;
		}
		return -1;
	}

	@Override
	public boolean remove(Object o) {
		return remove(o, false);
	}

	public boolean remove(Boolf<E> value) {
		E[] es = items;
		for (int i = 0; i < size; i++) {
			if (value.get(es[i])) {
				remove(i);
				return true;
			}
		}
		return false;
	}

	public boolean remove(Object o, boolean identity) {
		E[] es = items;
		if (identity || o == null) {
			for (int i = 0, n = size; i < n; i++) {
				if (es[i] == o) {
					remove(i);
					return true;
				}
			}
		} else {
			for (int i = 0, n = size; i < n; i++) {
				if (o.equals(es[i])) {
					remove(i);
					return true;
				}
			}
		}
		return false;
	}

	public boolean removeAll(Object o, boolean identity) {
		boolean modified = false;

		E[] es = items;
		if (identity || o == null) {
			for (int i = 0, n = size; i < n; i++) {
				if (es[i] == o) {
					remove(i);
					modified = true;
				}
			}
		} else {
			for (int i = 0, n = size; i < n; i++) {
				if (o.equals(es[i])) {
					remove(i);
					modified = true;
				}
			}
		}
		return modified;
	}

	@Override
	public E remove(int index) {
		if (index >= size) throw new IndexOutOfBoundsException("index can't be >= size: " + index + " >= " + size);
		E[] es = items;
		E value = es[index];
		size--;
		if (ordered)
			System.arraycopy(es, index + 1, es, index, size - index);
		else
			es[index] = es[size];
		es[size] = null;
		return value;
	}

	@Override
	public void removeRange(int start, int end) {
		if (end >= size) throw new IndexOutOfBoundsException("end can't be >= size: " + end + " >= " + size);
		if (start > end) throw new IndexOutOfBoundsException("start can't be > end: " + start + " > " + end);
		E[] es = items;
		int count = end - start + 1;
		if (ordered)
			System.arraycopy(es, start + count, es, start, size - (start + count));
		else {
			int lastIndex = size - 1;
			for (int i = 0; i < count; i++)
				es[start + i] = es[lastIndex - i];
		}
		size -= count;
	}

	public void removeAll(Boolf<E> pred) {
		Iterator<E> iter = iterator();
		while (iter.hasNext()) {
			if (pred.get(iter.next())) {
				iter.remove();
			}
		}
	}

	public E pop(Prov<? extends E> constructor) {
		if (size == 0) return constructor.get();
		return pop();
	}

	public E pop() {
		if (size == 0) throw new IllegalStateException("Array is empty.");
		--size;
		E item = items[size];
		items[size] = null;
		return item;
	}

	public E peek() {
		if (size == 0) throw new IllegalStateException("Array is empty.");
		return items[size - 1];
	}

	public E first() {
		if (size == 0) throw new IllegalStateException("Array is empty.");
		return items[0];
	}

	public E peek(Prov<? extends E> constructor) {
		if (size == 0) return constructor.get();
		return items[size - 1];
	}

	public E first(Prov<? extends E> constructor) {
		if (size == 0) return constructor.get();
		return items[0];
	}

	public E firstOpt() {
		if (size == 0) return null;
		return items[0];
	}

	@Override
	public int size() {
		return size;
	}

	@Override
	public boolean isEmpty() {
		return size == 0;
	}

	public boolean any() {
		return size > 0;
	}

	@Override
	public void clear() {
		E[] es = items;
		for (int i = 0, n = size; i < n; i++)
			es[i] = null;
		size = 0;
	}

	public void clear(int offset) {
		E[] es = items;
		for (int i = offset, n = size; i < n; i++)
			es[i] = null;
		size = offset;
	}

	public E[] shrink() {
		if (items.length != size) resize(size);
		return items;
	}

	public E[] ensureCapacity(int additionalCapacity) {
		if (additionalCapacity < 0)
			throw new IllegalArgumentException("additionalCapacity must be >= 0: " + additionalCapacity);
		int sizeNeeded = size + additionalCapacity;
		if (sizeNeeded > items.length) resize(Math.max(8, sizeNeeded));
		return items;
	}

	public E[] setSize(int newSize) {
		truncate(newSize);
		if (newSize > items.length) resize(Math.max(8, newSize));
		size = newSize;
		return items;
	}

	@SuppressWarnings("unchecked")
	protected E[] resize(int newSize) {
		//avoid reflection when possible
		E[] newItems = (E[]) Array.newInstance(componentType, newSize);
		System.arraycopy(items, 0, newItems, 0, Math.min(size, newItems.length));
		items = newItems;
		return newItems;
	}

	public void sort() {
		Sort.instance().sort(items, 0, size);
	}

	@Override
	public void sort(Comparator<? super E> comparator) {
		Sort.instance().sort(items, comparator, 0, size);
	}

	public void sort(Floatf<? super E> comparator) {
		Sort.instance().sort(items, Structs.comparingFloat(comparator), 0, size);
	}

	public <U extends Comparable<? super U>> void sortComparing(Func<? super E, ? extends U> keyExtractor) {
		sort(Structs.comparing(keyExtractor));
	}

	public void selectFrom(CollectionList<E> base, Boolf<E> predicate) {
		clear();
		for (E e : base.items) {
			if (predicate.get(e)) {
				add(e);
			}
		}
	}

	public void distinct() {
		CollectionObjectSet<E> set = asSet();
		clear();
		addAll(set);
	}

	@SuppressWarnings("unchecked")
	public <R> CollectionList<R> as() {
		return (CollectionList<R>) this;
	}

	public CollectionList<E> select(Boolf<E> predicate) {
		CollectionList<E> arr = new CollectionList<>(componentType);
		for (int i = 0; i < size; i++) {
			if (predicate.get(items[i])) {
				arr.add(items[i]);
			}
		}
		return arr;
	}

	public void retainAll(Boolf<E> predicate) {
		removeAll(e -> !predicate.get(e));
	}

	@Override
	public void replaceAll(UnaryOperator<E> operator) {
		for (int i = 0; i < size; i++) {
			items[i] = operator.apply(items[i]);
		}
	}

	public int count(Boolf<E> predicate) {
		int count = 0;
		for (int i = 0; i < size; i++) {
			if (predicate.get(items[i])) {
				count++;
			}
		}
		return count;
	}

	public E selectRanked(Comparator<? super E> comparator, int kthLowest) {
		if (kthLowest < 1) {
			throw new ArcRuntimeException("nth_lowest must be greater than 0, 1 = first, 2 = second...");
		}
		return Arrays2.select(items, comparator, kthLowest, size);
	}

	public int selectRankedIndex(Comparator<? super E> comparator, int kthLowest) {
		if (kthLowest < 1) {
			throw new ArcRuntimeException("nth_lowest must be greater than 0, 1 = first, 2 = second...");
		}
		return Arrays2.selectIndex(items, comparator, kthLowest, size);
	}

	public void reverse() {
		E[] es = items;
		for (int i = 0, lastIndex = size - 1, n = size / 2; i < n; i++) {
			int ii = lastIndex - i;
			E temp = es[i];
			es[i] = es[ii];
			es[ii] = temp;
		}
	}

	public void shuffle() {
		E[] es = items;
		for (int i = size - 1; i >= 0; i--) {
			int j = Mathf.random(i);
			E temp = es[i];
			es[i] = es[j];
			es[j] = temp;
		}
	}

	public void truncate(int newSize) {
		if (newSize < 0) throw new IllegalArgumentException("newSize must be >= 0: " + newSize);
		if (size <= newSize) return;
		for (int i = newSize; i < size; i++)
			items[i] = null;
		size = newSize;
	}

	public E random(Rand rand) {
		if (size == 0) return null;
		return items[rand.random(0, size - 1)];
	}

	public E random() {
		return random(Mathf.rand);
	}

	public E random(E exclude) {
		if (exclude == null) return random();
		if (size == 0) return null;
		if (size == 1) return first();

		int eidx = indexOf(exclude);
		if (eidx == -1) return random();

		int index = Mathf.random(0, size - 2);
		if (index >= eidx) {
			index++;
		}
		return items[index];
	}

	@Override
	public Object[] toArray() {
		return toArray(Object.class);
	}

	@SuppressWarnings("unchecked")
	public <T> T[] toArray(Class<?> type) {
		T[] result = (T[]) Array.newInstance(type, size);
		System.arraycopy(items, 0, result, 0, size);
		return result;
	}

	@Override
	public <T> T[] toArray(T[] a) {
		if (a.length < size) {
			return toArray(a.getClass().getComponentType());
		}

		System.arraycopy(items, 0, a, 0, size);
		if (a.length > size)
			a[size] = null;
		return a;
	}

	@Override
	public int hashCode() {
		if (!ordered) return super.hashCode();
		E[] es = items;
		int hashCode = 1;
		for (int i = 0; i < size; i++) {
			E item = es[i];
			hashCode = 31 * hashCode + (item == null ? 0 : item.hashCode());
		}
		return hashCode;
	}

	@Override
	public boolean equals(Object o) {
		if (o == this) return true;
		if (!ordered) return false;
		if (!(o instanceof List<?> other)) return false;
		int n = size;
		if (n != other.size()) return false;
		Object[] array = items;
		for (int i = 0; i < n; i++) {
			Object o1 = array[i];
			Object o2 = other.get(i);
			if (!Objects.equals(o1, o2)) return false;
		}
		return true;
	}

	@Override
	public String toString() {
		if (size == 0) return "[]";
		E[] es = items;
		StringBuilder buffer = new StringBuilder(32);
		buffer.append('[');
		buffer.append(es[0]);
		for (int i = 1; i < size; i++) {
			buffer.append(", ");
			buffer.append(es[i]);
		}
		buffer.append(']');
		return buffer.toString();
	}

	public String toString(String separator, Func<E, String> stringifier) {
		if (size == 0) return "";
		E[] es = items;
		StringBuilder buffer = new StringBuilder(32);
		buffer.append(stringifier.get(es[0]));
		for (int i = 1; i < size; i++) {
			buffer.append(separator);
			buffer.append(stringifier.get(es[i]));
		}
		return buffer.toString();
	}

	public String toString(String separator) {
		return toString(separator, String::valueOf);
	}

	@Override
	public Iterator<E> iterator() {
		if (iterator1 == null) iterator1 = new Iter();

		if (iterator1.done) {
			iterator1.cursor = 0;
			iterator1.done = false;
			return iterator1;
		}

		if (iterator2 == null) iterator2 = new Iter();

		if (iterator2.done) {
			iterator2.cursor = 0;
			iterator2.done = false;
			return iterator2;
		}
		return new Iter();
	}

	@Override
	public ListIterator<E> listIterator() {
		return listIterator(0);
	}

	@Override
	public ListIterator<E> listIterator(final int index) {
		if (index > size || index < 0)
			throw new IndexOutOfBoundsException("index can't be > size: " + index + " > " + size);

		if (lastIterator1 == null) lastIterator1 = new Iter(index);

		if (lastIterator1.done) {
			lastIterator1.cursor = index;
			lastIterator1.done = false;
			return lastIterator1;
		}

		if (lastIterator2 == null) lastIterator2 = new Iter(index);

		if (lastIterator2.done) {
			lastIterator2.cursor = index;
			lastIterator2.done = false;
			return lastIterator2;
		}

		return new Iter(index);
	}

	public class Iter implements ListIterator<E> {
		public int cursor;
		public boolean done = true;

		public Iter(int index) {
			cursor = index;
			iteratorsAllocated++;
		}

		public Iter() {
			iteratorsAllocated++;
		}

		@Override
		public boolean hasNext() {
			if (cursor >= size) done = true;
			return cursor < size;
		}

		@Override
		public E next() {
			if (cursor >= size) throw new NoSuchElementException(String.valueOf(cursor));
			return items[cursor++];
		}

		@Override
		public boolean hasPrevious() {
			return cursor > 0;
		}

		@Override
		public E previous() {
			if (!hasPrevious()) throw new NoSuchElementException("No previous");
			return items[cursor - 1];
		}

		@Override
		public int nextIndex() {
			return cursor;
		}

		@Override
		public int previousIndex() {
			return cursor - 1;
		}

		@Override
		public void remove() {
			cursor--;
			CollectionList.this.remove(cursor);
		}

		@Override
		public void set(E t) {
			CollectionList.this.set(cursor, t);
		}

		@Override
		public void add(E t) {
			CollectionList.this.add(t);
		}
	}
}
