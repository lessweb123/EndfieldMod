package endfield.util

import java.lang.invoke.MethodHandle
import java.lang.reflect.Method

open class MethodHandleVirtualMethodAccessor(method: Method) : AbstractMethodAccessor(method) {
	@JvmField
	val spread: MethodHandle

	init {
		val target = HandleSupport.unreflect(method).asFixedArity()

		val paramCount = target.type().parameterCount()

		require(paramCount >= 1) { "Instance method must have e receiver" }

		val spreader = target.asSpreader(Array<Any>::class.java, paramCount - 1)
		val newType = spreader.type()
			.changeParameterType(0, Any::class.java)
			.changeReturnType(Any::class.java)
		spread = spreader.asType(newType)
	}

	@Suppress("UNCHECKED_CAST")
	override fun <T> invoke(any: Any, vararg args: Any?): T = spread.invokeExact(any, args) as T
}