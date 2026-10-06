package endfield.util;

import java.nio.Buffer;

public final class Buffers2 {
	private Buffers2() {}

	/**
	 * @return The memory address of DirectBuffer
	 * @throws IllegalArgumentException If {@code buffer} is not a DirectBuffer
	 */
	public static long addressOf(Buffer buffer) {
		if (!buffer.isDirect())
			throw new IllegalArgumentException("buffer is non-direct");

		return ((sun.nio.ch.DirectBuffer) buffer).address();
	}
}
