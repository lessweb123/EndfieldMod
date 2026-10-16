@file:Suppress("UNCHECKED_CAST")

package endfield.util

import java.lang.invoke.MethodHandle
import java.lang.invoke.MethodType
import java.lang.reflect.Field
import java.lang.reflect.Modifier

open class MethodHandleFieldAccessor internal constructor(field: Field) : AbstractFieldAccessor(field) {
	companion object {
		@JvmStatic
		fun getMethodHandleFieldAccessor(field: Field): FieldAccessor {
			val type = field.type

			return if ((field.modifiers and Modifier.STATIC) != 0) {
				if (type.isPrimitive) {
					if (type === Boolean::class.javaPrimitiveType) MethodHandleStaticBooleanFieldAccessor(field)
					else if (type === Byte::class.javaPrimitiveType) MethodHandleStaticByteFieldAccessor(field)
					else if (type === Character.TYPE) MethodHandleStaticCharFieldAccessor(field)
					else if (type === Short::class.javaPrimitiveType) MethodHandleStaticShortFieldAccessor(field)
					else if (type === Integer.TYPE) MethodHandleStaticIntFieldAccessor(field)
					else if (type === Long::class.javaPrimitiveType) MethodHandleStaticLongFieldAccessor(field)
					else if (type === Float::class.javaPrimitiveType) MethodHandleStaticFloatFieldAccessor(field)
					else if (type === Double::class.javaPrimitiveType) MethodHandleStaticDoubleFieldAccessor(field)
					else throw IllegalArgumentException("unknown type of field $field")
				} else {
					MethodHandleStaticObjectFieldAccessor(field)
				}
			} else {
				if (type.isPrimitive) {
					if (type === Boolean::class.javaPrimitiveType) MethodHandleBooleanFieldAccessor(field)
					else if (type === Byte::class.javaPrimitiveType) MethodHandleByteFieldAccessor(field)
					else if (type === Character.TYPE) MethodHandleCharFieldAccessor(field)
					else if (type === Short::class.javaPrimitiveType) MethodHandleShortFieldAccessor(field)
					else if (type === Integer.TYPE) MethodHandleIntFieldAccessor(field)
					else if (type === Long::class.javaPrimitiveType) MethodHandleLongFieldAccessor(field)
					else if (type === Float::class.javaPrimitiveType) MethodHandleFloatFieldAccessor(field)
					else if (type === Double::class.javaPrimitiveType) MethodHandleDoubleFieldAccessor(field)
					else throw IllegalArgumentException("unknown type of field $field")
				} else {
					MethodHandleObjectFieldAccessor(field)
				}
			}
		}
	}

	@JvmField
	protected val getter: MethodHandle
	@JvmField
	protected val setter: MethodHandle

	init {
		val refc = field.declaringClass
		val name = field.name
		val type = field.type
		val returnType = if (type.isPrimitive) type else Any::class.java
		val modifiers = field.modifiers

		Reflections.setAccessible(field)

		if ((modifiers and Modifier.STATIC) != 0) {
			getter = HandleSupport.findStaticGetter(refc, name, type).asType(MethodType.methodType(returnType))
			setter = HandleSupport.findStaticSetter(refc, name, type).asType(MethodType.methodType(Void.TYPE, returnType))
		} else {
			getter = HandleSupport.findGetter(refc, name, type).asType(MethodType.methodType(returnType, Any::class.java))
			setter = HandleSupport.findSetter(refc, name, type).asType(MethodType.methodType(Void.TYPE, arrayOf(Any::class.java, returnType)))
		}
	}

	override fun <T> get(any: Any?): T {
		throw IllegalArgumentException(Reflections.getGetMessage(field, Any::class.java.getName()))
	}

	override fun set(any: Any?, value: Any?) {
		throw IllegalArgumentException(Reflections.getSetMessage(field, value))
	}

	override fun <T> getObject(any: Any?): T {
		throw IllegalArgumentException(Reflections.getGetMessage(field, Any::class.java.getName()))
	}

	override fun setObject(any: Any?, value: Any?) {
		throw IllegalArgumentException(Reflections.getSetMessage(field, value))
	}

	override fun getBoolean(any: Any?): Boolean {
		throw IllegalArgumentException(Reflections.getGetMessage(field, "boolean"))
	}

	override fun setBoolean(any: Any?, value: Boolean) {
		throw IllegalArgumentException(Reflections.getSetMessage(field, "boolean", value.toString()))
	}

	override fun getByte(any: Any?): Byte {
		throw IllegalArgumentException(Reflections.getGetMessage(field, "boolean"))
	}

	override fun setByte(any: Any?, value: Byte) {
		throw IllegalArgumentException(Reflections.getSetMessage(field, "byte", value.toInt().toString()))
	}

	override fun getChar(any: Any?): Char {
		throw IllegalArgumentException(Reflections.getGetMessage(field, "char"))
	}

	override fun setChar(any: Any?, value: Char) {
		throw IllegalArgumentException(Reflections.getSetMessage(field, "char", value.toString()))
	}

	override fun getShort(any: Any?): Short {
		throw IllegalArgumentException(Reflections.getGetMessage(field, "short"))
	}

	override fun setShort(any: Any?, value: Short) {
		throw IllegalArgumentException(Reflections.getSetMessage(field, "short", value.toInt().toString()))
	}

	override fun getInt(any: Any?): Int {
		throw IllegalArgumentException(Reflections.getGetMessage(field, "int"))
	}

	override fun setInt(any: Any?, value: Int) {
		throw IllegalArgumentException(Reflections.getSetMessage(field, "int", value.toString()))
	}

	override fun getLong(any: Any?): Long {
		throw IllegalArgumentException(Reflections.getGetMessage(field, "long"))
	}

	override fun setLong(any: Any?, value: Long) {
		throw IllegalArgumentException(Reflections.getSetMessage(field, "long", value.toString()))
	}

	override fun getFloat(any: Any?): Float {
		throw IllegalArgumentException(Reflections.getGetMessage(field, "float"))
	}

	override fun setFloat(any: Any?, value: Float) {
		throw IllegalArgumentException(Reflections.getSetMessage(field, "float", value.toString()))
	}

	override fun getDouble(any: Any?): Double {
		throw IllegalArgumentException(Reflections.getGetMessage(field, "double"))
	}

	override fun setDouble(any: Any?, value: Double) {
		throw IllegalArgumentException(Reflections.getSetMessage(field, "double", value.toString()))
	}
}

internal class MethodHandleObjectFieldAccessor(field: Field) : MethodHandleFieldAccessor(field) {
	override fun <T> get(any: Any?): T = getObject(any)

	override fun set(any: Any?, value: Any?) {
		setObject(any, value)
	}

	override fun <T> getObject(any: Any?): T = getter.invokeExact(any) as T

	override fun setObject(any: Any?, value: Any?) {
		setter.invokeExact(any, value)
	}
}

internal class MethodHandleBooleanFieldAccessor(field: Field) : MethodHandleFieldAccessor(field) {
	override fun <T> get(any: Any?): T = getBoolean(any) as T

	override fun set(any: Any?, value: Any?) {
		setBoolean(any, value as Boolean)
	}

	override fun getBoolean(any: Any?): Boolean = getter.invokeExact(any) as Boolean

	override fun setBoolean(any: Any?, value: Boolean) {
		setter.invokeExact(any, value)
	}
}

internal class MethodHandleByteFieldAccessor(field: Field) : MethodHandleFieldAccessor(field) {
	override fun <T> get(any: Any?): T = getByte(any) as T

	override fun set(any: Any?, value: Any?) {
		setByte(any, (value as Number).toByte())
	}

	override fun getByte(any: Any?): Byte = getter.invokeExact(any) as Byte

	override fun setByte(any: Any?, value: Byte) {
		setter.invokeExact(any, value)
	}

	override fun getShort(any: Any?): Short = getByte(any).toShort()

	override fun getInt(any: Any?): Int = getByte(any).toInt()

	override fun getLong(any: Any?): Long = getByte(any).toLong()

	override fun getFloat(any: Any?): Float = getByte(any).toFloat()

	override fun getDouble(any: Any?): Double = getByte(any).toDouble()
}

internal class MethodHandleCharFieldAccessor(field: Field) : MethodHandleFieldAccessor(field) {
	override fun <T> get(any: Any?): T = Character.valueOf(getChar(any)) as T

	override fun set(any: Any?, value: Any?) {
		setChar(any, value as Char)
	}

	override fun getChar(any: Any?): Char = getter.invokeExact(any) as Char

	override fun setChar(any: Any?, value: Char) {
		setter.invokeExact(any, value)
	}

	override fun getInt(any: Any?): Int = getChar(any).code

	override fun getLong(any: Any?): Long = getChar(any).code.toLong()

	override fun getFloat(any: Any?): Float = getChar(any).code.toFloat()

	override fun getDouble(any: Any?): Double = getChar(any).code.toDouble()
}

internal class MethodHandleShortFieldAccessor(field: Field) : MethodHandleFieldAccessor(field) {
	override fun <T> get(any: Any?): T {
		return getShort(any) as T
	}

	override fun set(any: Any?, value: Any?) {
		setShort(any, (value as Number).toShort())
	}

	override fun setByte(any: Any?, value: Byte) {
		setShort(any, value.toShort())
	}

	override fun getShort(any: Any?): Short = getter.invokeExact(any) as Short

	override fun setShort(any: Any?, value: Short) {
		setter.invokeExact(any, value)
	}

	override fun getInt(any: Any?): Int = getShort(any).toInt()

	override fun getLong(any: Any?): Long = getShort(any).toLong()

	override fun getFloat(any: Any?): Float = getShort(any).toFloat()

	override fun getDouble(any: Any?): Double = getShort(any).toDouble()
}

internal class MethodHandleIntFieldAccessor(field: Field) : MethodHandleFieldAccessor(field) {
	override fun <T> get(any: Any?): T = getInt(any) as T

	override fun set(any: Any?, value: Any?) {
		setInt(any, (value as Number).toInt())
	}

	override fun setByte(any: Any?, value: Byte) {
		setInt(any, value.toInt())
	}

	override fun setChar(any: Any?, value: Char) {
		setInt(any, value.code)
	}

	override fun setShort(any: Any?, value: Short) {
		setInt(any, value.toInt())
	}

	override fun getInt(any: Any?): Int = getter.invokeExact(any) as Int

	override fun setInt(any: Any?, value: Int) {
		setter.invokeExact(any, value)
	}

	override fun getLong(any: Any?): Long = getInt(any).toLong()

	override fun getFloat(any: Any?): Float = getInt(any).toFloat()

	override fun getDouble(any: Any?): Double = getInt(any).toDouble()
}

internal class MethodHandleLongFieldAccessor(field: Field) : MethodHandleFieldAccessor(field) {
	override fun <T> get(any: Any?): T = getLong(any) as T

	override fun set(any: Any?, value: Any?) {
		setLong(any, (value as Number).toLong())
	}

	override fun setByte(any: Any?, value: Byte) {
		setLong(any, value.toLong())
	}

	override fun setShort(any: Any?, value: Short) {
		setLong(any, value.toLong())
	}

	override fun setChar(any: Any?, value: Char) {
		setLong(any, value.code.toLong())
	}

	override fun setInt(any: Any?, value: Int) {
		setLong(any, value.toLong())
	}

	override fun getLong(any: Any?): Long = getter.invokeExact(any) as Long

	override fun setLong(any: Any?, value: Long) {
		setter.invokeExact(any, value)
	}

	override fun getFloat(any: Any?): Float = getLong(any).toFloat()

	override fun getDouble(any: Any?): Double = getLong(any).toDouble()
}

internal class MethodHandleFloatFieldAccessor(field: Field) : MethodHandleFieldAccessor(field) {
	override fun <T> get(any: Any?): T {
		return getFloat(any) as T
	}

	override fun set(any: Any?, value: Any?) {
		setFloat(any, (value as Number).toFloat())
	}

	override fun setByte(any: Any?, value: Byte) {
		setFloat(any, value.toFloat())
	}

	override fun setChar(any: Any?, value: Char) {
		setFloat(any, value.code.toFloat())
	}

	override fun setShort(any: Any?, value: Short) {
		setFloat(any, value.toFloat())
	}

	override fun setInt(any: Any?, value: Int) {
		setFloat(any, value.toFloat())
	}

	override fun setLong(any: Any?, value: Long) {
		setFloat(any, value.toFloat())
	}

	override fun getFloat(any: Any?): Float = getter.invokeExact(any) as Float

	override fun setFloat(any: Any?, value: Float) {
		setter.invokeExact(any, value)
	}

	override fun getDouble(any: Any?): Double {
		return getFloat(any).toDouble()
	}
}

internal class MethodHandleDoubleFieldAccessor(field: Field) : MethodHandleFieldAccessor(field) {
	override fun <T> get(any: Any?): T = getDouble(any) as T

	override fun set(any: Any?, value: Any?) {
		setDouble(any, (value as Number).toDouble())
	}

	override fun setByte(any: Any?, value: Byte) {
		setDouble(any, value.toDouble())
	}

	override fun setChar(any: Any?, value: Char) {
		setDouble(any, value.code.toDouble())
	}

	override fun setShort(any: Any?, value: Short) {
		setDouble(any, value.toDouble())
	}

	override fun setInt(any: Any?, value: Int) {
		setDouble(any, value.toDouble())
	}

	override fun setLong(any: Any?, value: Long) {
		setDouble(any, value.toDouble())
	}

	override fun setFloat(any: Any?, value: Float) {
		setDouble(any, value.toDouble())
	}

	override fun getDouble(any: Any?): Double = getter.invokeExact(any) as Double

	override fun setDouble(any: Any?, value: Double) {
		setter.invokeExact(any, value)
	}
}

internal class MethodHandleStaticObjectFieldAccessor(field: Field) : MethodHandleFieldAccessor(field) {
	override fun <T> get(any: Any?): T = getObject(any)

	override fun set(any: Any?, value: Any?) {
		setObject(any, value)
	}

	override fun <T> getObject(any: Any?): T = getter.invokeExact() as T

	override fun setObject(any: Any?, value: Any?) {
		setter.invokeExact(value)
	}
}

internal class MethodHandleStaticBooleanFieldAccessor(field: Field) : MethodHandleFieldAccessor(field) {
	override fun <T> get(any: Any?): T = getBoolean(any) as T

	override fun set(any: Any?, value: Any?) {
		setBoolean(any, value as Boolean)
	}

	override fun getBoolean(any: Any?): Boolean = getter.invokeExact() as Boolean

	override fun setBoolean(any: Any?, value: Boolean) {
		setter.invokeExact(value)
	}
}

internal class MethodHandleStaticByteFieldAccessor(field: Field) : MethodHandleFieldAccessor(field) {
	override fun <T> get(any: Any?): T = getByte(any) as T

	override fun set(any: Any?, value: Any?) {
		setByte(any, (value as Number).toByte())
	}

	override fun getByte(any: Any?): Byte = getter.invokeExact() as Byte

	override fun setByte(any: Any?, value: Byte) {
		setter.invokeExact(value)
	}

	override fun getShort(any: Any?): Short = getByte(any).toShort()

	override fun getInt(any: Any?): Int = getByte(any).toInt()

	override fun getLong(any: Any?): Long = getByte(any).toLong()

	override fun getFloat(any: Any?): Float = getByte(any).toFloat()

	override fun getDouble(any: Any?): Double = getByte(any).toDouble()
}

internal class MethodHandleStaticCharFieldAccessor(field: Field) : MethodHandleFieldAccessor(field) {
	override fun <T> get(any: Any?): T = Character.valueOf(getChar(any)) as T

	override fun set(any: Any?, value: Any?) {
		setChar(any, value as Char)
	}

	override fun getChar(any: Any?): Char = getter.invokeExact() as Char

	override fun setChar(any: Any?, value: Char) {
		setter.invokeExact(value)
	}

	override fun getInt(any: Any?): Int = getChar(any).code

	override fun getLong(any: Any?): Long = getChar(any).code.toLong()

	override fun getFloat(any: Any?): Float = getChar(any).code.toFloat()

	override fun getDouble(any: Any?): Double = getChar(any).code.toDouble()
}

internal class MethodHandleStaticShortFieldAccessor(field: Field) : MethodHandleFieldAccessor(field) {
	override fun <T> get(any: Any?): T {
		return getShort(any) as T
	}

	override fun set(any: Any?, value: Any?) {
		setShort(any, (value as Number).toShort())
	}

	override fun setByte(any: Any?, value: Byte) {
		setShort(any, value.toShort())
	}

	override fun getShort(any: Any?): Short = getter.invokeExact() as Short

	override fun setShort(any: Any?, value: Short) {
		setter.invokeExact(value)
	}

	override fun getInt(any: Any?): Int = getShort(any).toInt()

	override fun getLong(any: Any?): Long = getShort(any).toLong()

	override fun getFloat(any: Any?): Float = getShort(any).toFloat()

	override fun getDouble(any: Any?): Double = getShort(any).toDouble()
}

internal class MethodHandleStaticIntFieldAccessor(field: Field) : MethodHandleFieldAccessor(field) {
	override fun <T> get(any: Any?): T = getInt(any) as T

	override fun set(any: Any?, value: Any?) {
		setInt(any, (value as Number).toInt())
	}

	override fun setByte(any: Any?, value: Byte) {
		setInt(any, value.toInt())
	}

	override fun setChar(any: Any?, value: Char) {
		setInt(any, value.code)
	}

	override fun setShort(any: Any?, value: Short) {
		setInt(any, value.toInt())
	}

	override fun getInt(any: Any?): Int = getter.invokeExact() as Int

	override fun setInt(any: Any?, value: Int) {
		setter.invokeExact(value)
	}

	override fun getLong(any: Any?): Long = getInt(any).toLong()

	override fun getFloat(any: Any?): Float = getInt(any).toFloat()

	override fun getDouble(any: Any?): Double = getInt(any).toDouble()
}

internal class MethodHandleStaticLongFieldAccessor(field: Field) : MethodHandleFieldAccessor(field) {
	override fun <T> get(any: Any?): T = getLong(any) as T

	override fun set(any: Any?, value: Any?) {
		setLong(any, (value as Number).toLong())
	}

	override fun setByte(any: Any?, value: Byte) {
		setLong(any, value.toLong())
	}

	override fun setChar(any: Any?, value: Char) {
		setLong(any, value.code.toLong())
	}

	override fun setShort(any: Any?, value: Short) {
		setLong(any, value.toLong())
	}

	override fun setInt(any: Any?, value: Int) {
		setLong(any, value.toLong())
	}

	override fun getLong(any: Any?): Long = getter.invokeExact() as Long

	override fun setLong(any: Any?, value: Long) {
		setter.invokeExact(value)
	}

	override fun getFloat(any: Any?): Float = getLong(any).toFloat()

	override fun getDouble(any: Any?): Double = getLong(any).toDouble()
}

internal class MethodHandleStaticFloatFieldAccessor(field: Field) : MethodHandleFieldAccessor(field) {
	override fun <T> get(any: Any?): T = getFloat(any) as T

	override fun set(any: Any?, value: Any?) {
		setFloat(any, (value as Number).toFloat())
	}

	override fun setByte(any: Any?, value: Byte) {
		setFloat(any, value.toFloat())
	}

	override fun setChar(any: Any?, value: Char) {
		setFloat(any, value.code.toFloat())
	}

	override fun setShort(any: Any?, value: Short) {
		setFloat(any, value.toFloat())
	}

	override fun setInt(any: Any?, value: Int) {
		setFloat(any, value.toFloat())
	}

	override fun setLong(any: Any?, value: Long) {
		setFloat(any, value.toFloat())
	}

	override fun getFloat(any: Any?): Float = getter.invokeExact() as Float

	override fun setFloat(any: Any?, value: Float) {
		setter.invokeExact(value)
	}

	override fun getDouble(any: Any?): Double = getFloat(any).toDouble()
}

internal class MethodHandleStaticDoubleFieldAccessor(field: Field) : MethodHandleFieldAccessor(field) {
	override fun <T> get(any: Any?): T = getDouble(any) as T

	override fun set(any: Any?, value: Any?) {
		setDouble(any, (value as Number).toDouble())
	}

	override fun setByte(any: Any?, value: Byte) {
		setDouble(any, value.toDouble())
	}

	override fun setChar(any: Any?, value: Char) {
		setDouble(any, value.code.toDouble())
	}

	override fun setShort(any: Any?, value: Short) {
		setDouble(any, value.toDouble())
	}

	override fun setInt(any: Any?, value: Int) {
		setDouble(any, value.toDouble())
	}

	override fun setLong(any: Any?, value: Long) {
		setDouble(any, value.toDouble())
	}

	override fun setFloat(any: Any?, value: Float) {
		setDouble(any, value.toDouble())
	}

	override fun getDouble(any: Any?): Double = getter.invokeExact() as Double

	override fun setDouble(any: Any?, value: Double) {
		setter.invokeExact(value)
	}
}