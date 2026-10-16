package endfield.util

import java.lang.invoke.MethodHandle
import java.lang.reflect.Method

/**
 * @since 1.0.10
 */
class SpecialMethodAccessor(method: Method) : AbstractMethodAccessor(method) {
	@JvmField
	val target: MethodHandle
	@JvmField
	val spread: MethodHandle

	init {
		target = HandleSupport.unreflectSpecial(method).asFixedArity()

		val paramCount = target.type().parameterCount()

		val spreader = target.asSpreader(Array<Any>::class.java, paramCount - 1)
		val newType = spreader.type()
			.changeParameterType(0, Any::class.java)
			.changeReturnType(Any::class.java)
		spread = spreader.asType(newType)
	}

	override fun <T> invoke(obj: Any, vararg args: Any?): T = spread.invokeExact(obj, args) as T
}
