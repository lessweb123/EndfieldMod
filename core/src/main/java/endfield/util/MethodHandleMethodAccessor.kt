package endfield.util

import java.lang.invoke.MethodHandle
import java.lang.invoke.MethodType
import java.lang.reflect.Method
import java.lang.reflect.Modifier

open class MethodHandleMethodAccessor(method: Method) : AbstractMethodAccessor(method) {
	@JvmField
	val target: MethodHandle
	@JvmField
	val spread: MethodHandle

	init {
		target = HandleSupport.unreflect(method).asFixedArity()

		val paramCount = target.type().parameterCount()

		if (method.modifiers and Modifier.STATIC != 0) {
			spread = target.asSpreader(Array<Any>::class.java, paramCount)
				.asType(MethodType.methodType(Any::class.java, Array<Any>::class.java))
		} else {
			val spreader = target.asSpreader(Array<Any>::class.java, paramCount - 1)
			val newType = spreader.type()
				.changeParameterType(0, Any::class.java)
				.changeReturnType(Any::class.java)
			spread = spreader.asType(newType)
		}
	}

	@Suppress("UNCHECKED_CAST")
	override fun <T> invoke(any: Any, vararg args: Any?): T = spread.invokeExact(any, args) as T
}