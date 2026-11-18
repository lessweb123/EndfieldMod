package endfield.util

import org.objectweb.asm.Opcodes
import java.lang.invoke.MethodHandle
import java.lang.invoke.MethodType
import java.lang.reflect.Method

open class MethodHandleMethodAccessor(method: Method) : AbstractMethodAccessor(method) {
	@JvmField
	val target: MethodHandle
	@JvmField
	val spread: MethodHandle

	@JvmField
	val isStatic: Boolean

	init {
		target = HandleSupport.unreflect(method).asFixedArity()

		val paramCount = target.type().parameterCount()

		isStatic = method.modifiers and Opcodes.ACC_STATIC != 0

		if (isStatic) {
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
	override fun <T> invoke(any: Any?, vararg args: Any?): T = (if (isStatic) spread.invokeExact(args) else spread.invokeExact(any, args)) as T
}