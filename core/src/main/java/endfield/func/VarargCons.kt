package endfield.func

@FunctionalInterface
fun interface VarargCons<P> {
	fun get(vararg params: P)
}
