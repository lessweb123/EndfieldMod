package endfield.util;

public class IntCircularQueue {
	public final int[] queue;

	public int head;
	public int tail;
	public final int capacity;

	public IntCircularQueue(int cap) {
		capacity = cap;
		queue = new int[cap];
		head = -1;
		tail = -1;
	}

	public boolean isEmpty() {
		return head == -1;
	}

	public boolean isFull() {
		return ((tail + 1) % capacity) == head;
	}

	public boolean enqueueFront(int value) {
		if (isFull())
			return false;

		if (isEmpty())
			head = tail = 0;
		else
			head = (head - 1 + capacity) % capacity;

		queue[head] = value;
		return true;
	}

	public boolean enqueueRear(int value) {
		if (isFull())
			return false;

		if (isEmpty())
			head = 0;

		tail = (tail + 1) % capacity;
		queue[tail] = value;
		return true;
	}

	public int dequeueFront() {
		if (isEmpty())
			return -1;

		int value = queue[head];
		if (head == tail)
			head = tail = -1;
		else
			head = (head + 1) % capacity;

		return value;
	}

	public int dequeueRear() {
		if (isEmpty())
			return -1;

		int value = queue[tail];
		if (head == tail)
			head = tail = -1;
		else
			tail = (tail - 1 + capacity) % capacity;

		return value;
	}

	@Override
	public String toString() {
		if (queue.length == 0) return "[]";

		int[] is = queue;
		int h = head;
		int t = tail;

		StringBuilder sb = new StringBuilder(64);
		sb.append('[');
		sb.append(is[h]);
		for (int i = (h + 1) % is.length; i != t; i = (i + 1) % is.length) {
			sb.append(", ").append(is[i]);
		}
		sb.append(']');
		return sb.toString();
	}
}
