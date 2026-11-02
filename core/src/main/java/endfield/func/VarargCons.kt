package endfield.func

@FunctionalInterface
fun interface VarargCons<P> {
	operator fun invoke(vararg params: P)
}
