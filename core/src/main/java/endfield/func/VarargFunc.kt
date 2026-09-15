package endfield.func

@FunctionalInterface
fun interface VarargFunc<P, R> {
	fun get(vararg params: P): R
}
