package endfield.util

import java.lang.reflect.Field

open class ReflectionFieldAccessor(field: Field) : AbstractFieldAccessor(field) {
	init {
		require(Reflections.setAccessible(field)) { "Unable to access field: $field" }
	}

	@Suppress("UNCHECKED_CAST")
	override fun <T> get(any: Any?): T = field.get(any) as T

	override fun set(any: Any?, value: Any?) {
		field.set(any, value)
	}

	@Suppress("UNCHECKED_CAST")
	override fun <T> getObject(any: Any?): T = field.get(any) as T

	override fun setObject(any: Any?, value: Any?) {
		field.set(any, value)
	}

	override fun getBoolean(any: Any?): Boolean = field.getBoolean(any)

	override fun setBoolean(any: Any?, value: Boolean) {
		field.setBoolean(any, value)
	}

	override fun getByte(any: Any?): Byte = field.getByte(any)

	override fun setByte(any: Any?, value: Byte) {
		field.setByte(any, value)
	}

	override fun getChar(any: Any?): Char = field.getChar(any)

	override fun setChar(any: Any?, value: Char) {
		field.setChar(any, value)
	}

	override fun getShort(any: Any?): Short = field.getShort(any)

	override fun setShort(any: Any?, value: Short) {
		field.setShort(any, value)
	}

	override fun getInt(any: Any?): Int = field.getInt(any)

	override fun setInt(any: Any?, value: Int) {
		field.setInt(any, value)
	}

	override fun getLong(any: Any?): Long = field.getLong(any)

	override fun setLong(any: Any?, value: Long) {
		field.setLong(any, value)
	}

	override fun getFloat(any: Any?): Float = field.getFloat(any)

	override fun setFloat(any: Any?, value: Float) {
		field.setFloat(any, value)
	}

	override fun getDouble(any: Any?): Double = field.getDouble(any)

	override fun setDouble(any: Any?, value: Double) {
		field.setDouble(any, value)
	}
}
