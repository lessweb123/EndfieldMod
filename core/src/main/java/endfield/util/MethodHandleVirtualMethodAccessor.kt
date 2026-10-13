package endfield.util

import java.lang.invoke.MethodHandle
import java.lang.reflect.Method

class MethodHandleVirtualMethodAccessor(method: Method) : AbstractMethodAccessor(method) {
	@JvmField
	val spreadHandle: MethodHandle

	init {
		val target = Reflects.lookup(method.declaringClass).unreflect(method).asFixedArity()

		val paramCount = target.type().parameterCount()

		require(paramCount >= 1) { "Instance method must have e receiver" }

		val spread = target.asSpreader(Array<Any>::class.java, paramCount - 1)
		val newType = spread.type()
			.changeParameterType(0, Any::class.java)
			.changeReturnType(Any::class.java)
		spreadHandle = spread.asType(newType)
	}

	@Suppress("UNCHECKED_CAST")
	override fun <T> invoke(any: Any, vararg args: Any?): T = spreadHandle.invokeExact(any, args) as T
}