package endfield.util

import java.lang.invoke.MethodHandle
import java.lang.invoke.MethodType
import java.lang.reflect.Constructor

class MethodHandleConstructorAccessor<T>(constructor: Constructor<T>) : AbstractConstructorAccessor<T>(constructor) {
	@JvmField
	val spreadHandle: MethodHandle

	init {
		val target: MethodHandle = Reflects.lookup(constructor.declaringClass).unreflectConstructor(constructor).asFixedArity()

		val paramCount = target.type().parameterCount()
		val spread = target.asSpreader(Array<Any>::class.java, paramCount)
		spreadHandle = spread.asType(MethodType.methodType(Any::class.java, Array<Any>::class.java))
	}

	@Suppress("UNCHECKED_CAST")
	override fun newInstance(vararg args: Any?): T = spreadHandle.invokeExact(args) as T
}