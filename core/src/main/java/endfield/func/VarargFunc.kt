package endfield.func

@FunctionalInterface
fun interface VarargFunc<P, R> {
	operator fun invoke(vararg params: P): R
}
