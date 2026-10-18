package endfield.util

import java.lang.invoke.MethodHandle
import java.lang.invoke.MethodType
import java.lang.reflect.Method

open class MethodHandleStaticMethodAccessor(method: Method) : AbstractMethodAccessor(method) {
	@JvmField
	val spread: MethodHandle

	init {
		val target = HandleSupport.unreflect(method).asFixedArity()

		val paramCount = target.type().parameterCount()

		spread = target.asSpreader(Array<Any>::class.java, paramCount)
			.asType(MethodType.methodType(Any::class.java, Array<Any>::class.java))
	}

	@Suppress("UNCHECKED_CAST")
	override fun <T> invoke(any: Any?, vararg args: Any?): T = spread.invokeExact(args) as T
}