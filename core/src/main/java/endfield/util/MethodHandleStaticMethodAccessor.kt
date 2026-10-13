package endfield.util

import java.lang.invoke.MethodHandle
import java.lang.invoke.MethodType
import java.lang.reflect.Method

class MethodHandleStaticMethodAccessor(method: Method) : AbstractMethodAccessor(method) {
	@JvmField
	val spreadHandle: MethodHandle

	init {
		val target = Reflects.lookup(method.declaringClass).unreflect(method).asFixedArity()

		val paramCount = target.type().parameterCount()

		spreadHandle = target.asSpreader(Array<Any>::class.java, paramCount)
			.asType(MethodType.methodType(Any::class.java, Array<Any>::class.java))
	}

	@Suppress("UNCHECKED_CAST")
	override fun <T> invoke(any: Any?, vararg args: Any?): T = spreadHandle.invokeExact(args) as T
}