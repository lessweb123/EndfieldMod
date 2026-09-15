package endfield.util;

import java.util.NoSuchElementException;

/**
 * Implementation of Java Collection Framework {@code Set} based on {@code OrderedSet}, used in places that require
 * Java specifications and the feature of {@code OrderedSet} not creating nodes.
 */
public class CollectionOrderedSet<E> extends CollectionObjectSet<E> {
	public final CollectionList<E> orderedItems;

	public CollectionOrderedSet() {
		orderedItems = new CollectionList<>();
	}

	public CollectionOrderedSet(Class<?> type) {
		super(type);
		orderedItems = new CollectionList<>(type);
	}

	public CollectionOrderedSet(Class<?> type, int initialCapacity) {
		super(type, initialCapacity);
		orderedItems = new CollectionList<>(initialCapacity, type);
	}

	public CollectionOrderedSet(Class<?> type, int initialCapacity, float loadFactor) {
		super(type, initialCapacity, loadFactor);
		orderedItems = new CollectionList<>(initialCapacity, type);
	}

	public CollectionOrderedSet(CollectionObjectSet<? extends E> set) {
		super(set);
		orderedItems = new CollectionList<>(set.size, set.elementType);
	}

	@Override
	public E first() {
		return orderedItems.first();
	}

	@Override
	public boolean add(E key) {
		if (!super.add(key)) return false;
		orderedItems.add(key);
		return true;
	}

	public boolean add(E key, int index) {
		if (!super.add(key)) {
			int oldIndex = orderedItems.indexOf(key, true);
			if (oldIndex != index) orderedItems.insert(index, orderedItems.remove(oldIndex));
			return false;
		}
		orderedItems.insert(index, key);
		return true;
	}

	public void addAll(CollectionOrderedSet<E> set) {
		ensureCapacity(set.size);
		E[] keys = set.orderedItems.items;
		for (int i = 0, n = set.orderedItems.size; i < n; i++)
			add(keys[i]);
	}

	@Override
	public void ensureCapacity(int additionalCapacity) {
		super.ensureCapacity(additionalCapacity);
		orderedItems.ensureCapacity(additionalCapacity);
	}

	@Override
	public boolean remove(Object key) {
		if (!super.remove(key)) return false;
		orderedItems.remove(key, false);
		return true;
	}

	public E removeIndex(int index) {
		E key = orderedItems.remove(index);
		super.remove(key);
		return key;
	}

	public boolean alter(E before, E after) {
		if (contains(after)) return false;
		if (!super.remove(before)) return false;
		super.add(after);
		orderedItems.set(orderedItems.indexOf(before, false), after);
		return true;
	}

	public boolean alterIndex(int index, E after) {
		if (index < 0 || index >= size || contains(after)) return false;
		super.remove(orderedItems.get(index));
		super.add(after);
		orderedItems.set(index, after);
		return true;
	}

	@Override
	public void clear(int maximumCapacity) {
		orderedItems.clear();
		super.clear(maximumCapacity);
	}

	@Override
	public void clear() {
		orderedItems.clear();
		super.clear();
	}

	public CollectionList<E> orderedItems() {
		return orderedItems;
	}

	@Override
	public Iter iterator() {
		if (iterator1 == null) {
			iterator1 = new OrderedIter();
			iterator2 = new OrderedIter();
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

	@Override
	public int hashCode() {
		int h = size;
		E[] es = orderedItems.items;
		for (int i = 0, n = orderedItems.size; i < n; i++) {
			E key = es[i];
			if (key != null) h += key.hashCode();
		}
		return h;
	}

	@Override
	public String toString() {
		if (size == 0) return "{}";
		E[] es = orderedItems.items;
		StringBuilder buffer = new StringBuilder(32);
		buffer.append('{');
		buffer.append(es[0]);
		for (int i = 1; i < size; i++) {
			buffer.append(", ");
			buffer.append(es[i]);
		}
		buffer.append('}');
		return buffer.toString();
	}

	public class OrderedIter extends Iter {
		@Override
		public void reset() {
			super.reset();
			nextIndex = 0;
			hasNext = size > 0;
		}

		@Override
		public E next() {
			if (!hasNext) throw new NoSuchElementException();
			E key = orderedItems.get(nextIndex);
			nextIndex++;
			hasNext = nextIndex < size;
			return key;
		}

		@Override
		public void remove() {
			if (nextIndex < 0) throw new IllegalStateException("next must be called before remove.");
			nextIndex--;
			removeIndex(nextIndex);
		}
	}
}
