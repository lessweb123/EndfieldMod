package endfield.util

/** (Used in Java) to throw a checked exception. */
fun sneakyThrow(t: Throwable): RuntimeException {
	throw t
}
