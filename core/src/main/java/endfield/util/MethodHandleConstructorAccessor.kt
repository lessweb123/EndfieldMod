package endfield.util

import java.lang.invoke.MethodHandle
import java.lang.invoke.MethodType
import java.lang.reflect.Constructor

open class MethodHandleConstructorAccessor<T>(constructor: Constructor<T>) : AbstractConstructorAccessor<T>(constructor) {
	@JvmField
	val target: MethodHandle
	@JvmField
	val spread: MethodHandle

	init {
		target = HandleSupport.unreflectConstructor(constructor).asFixedArity()

		val paramCount = target.type().parameterCount()
		val spreader = target.asSpreader(Array<Any>::class.java, paramCount)
		spread = spreader.asType(MethodType.methodType(Any::class.java, Array<Any>::class.java))
	}

	@Suppress("UNCHECKED_CAST")
	override fun newInstance(vararg args: Any?): T = spread.invokeExact(args) as T
}