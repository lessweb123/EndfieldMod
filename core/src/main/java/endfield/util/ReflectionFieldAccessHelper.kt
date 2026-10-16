package endfield.util

import java.lang.reflect.Field

open class ReflectionFieldAccessHelper : AbstractFieldAccessorHelper() {
	override fun setByte(any: Any, name: String, value: Byte) {
		getField(any.javaClass, name, false).setByte(any, value)
	}

	override fun setByteStatic(clazz: Class<*>, name: String, value: Byte) {
		getField(clazz, name, true).setByte(null, value)
	}

	override fun getByte(any: Any, name: String): Byte = getField(any.javaClass, name, false).getByte(any)

	override fun getByteStatic(clazz: Class<*>, name: String): Byte = getField(clazz, name, true).getByte(null)

	override fun setShort(any: Any, name: String, value: Short) {
		getField(any.javaClass, name, false).setShort(any, value)
	}

	override fun setShortStatic(clazz: Class<*>, name: String, value: Short) {
		getField(clazz, name, true).setShort(null, value)
	}

	override fun getShort(any: Any, name: String): Short = getField(any.javaClass, name, false).getShort(any)

	override fun getShortStatic(clazz: Class<*>, name: String): Short = getField(clazz, name, true).getShort(null)

	override fun setInt(any: Any, name: String, value: Int) {
		getField(any.javaClass, name, false).setInt(any, value)
	}

	override fun setIntStatic(clazz: Class<*>, name: String, value: Int) {
		getField(clazz, name, true).setInt(null, value)
	}

	override fun getInt(any: Any, name: String): Int = getField(any.javaClass, name, false).getInt(any)

	override fun getIntStatic(clazz: Class<*>, name: String): Int = getField(clazz, name, true).getInt(null)

	override fun setLong(any: Any, name: String, value: Long) {
		getField(any.javaClass, name, false).setLong(any, value)
	}

	override fun setLongStatic(clazz: Class<*>, name: String, value: Long) {
		getField(clazz, name, true).setLong(null, value)
	}

	override fun getLong(any: Any, name: String): Long = getField(any.javaClass, name, false).getLong(any)

	override fun getLongStatic(clazz: Class<*>, name: String): Long = getField(clazz, name, true).getLong(null)

	override fun setFloat(any: Any, name: String, value: Float) {
		getField(any.javaClass, name, false).setFloat(any, value)
	}

	override fun setFloatStatic(clazz: Class<*>, name: String, value: Float) {
		getField(clazz, name, true).setFloat(null, value)
	}

	override fun getFloat(any: Any, name: String): Float = getField(any.javaClass, name, false).getFloat(any)

	override fun getFloatStatic(clazz: Class<*>, name: String): Float = getField(clazz, name, true).getFloat(null)

	override fun setDouble(any: Any, name: String, value: Double) {
		getField(any.javaClass, name, false).setDouble(any, value)
	}

	override fun setDoubleStatic(clazz: Class<*>, name: String, value: Double) {
		getField(clazz, name, true).setDouble(null, value)
	}

	override fun getDouble(any: Any, name: String): Double = getField(any.javaClass, name, false).getDouble(any)

	override fun getDoubleStatic(clazz: Class<*>, name: String): Double = getField(clazz, name, true).getDouble(null)

	override fun setChar(any: Any, name: String, value: Char) {
		getField(any.javaClass, name, false).setChar(any, value)
	}

	override fun setCharStatic(clazz: Class<*>, name: String, value: Char) {
		getField(clazz, name, true).setChar(null, value)
	}

	override fun getChar(any: Any, name: String): Char = getField(any.javaClass, name, false).getChar(any)

	override fun getCharStatic(clazz: Class<*>, name: String): Char = getField(clazz, name, true).getChar(null)

	override fun setBoolean(any: Any, name: String, value: Boolean) {
		getField(any.javaClass, name, false).setBoolean(any, value)
	}

	override fun setBooleanStatic(clazz: Class<*>, name: String, value: Boolean) {
		getField(clazz, name, true).setBoolean(null, value)
	}

	override fun getBoolean(any: Any, name: String): Boolean = getField(any.javaClass, name, false).getBoolean(any)

	override fun getBooleanStatic(clazz: Class<*>, name: String): Boolean = getField(clazz, name, true).getBoolean(null)

	override fun setObject(any: Any, name: String, value: Any?) {
		getField(any.javaClass, name, false).set(any, value)
	}

	override fun setObjectStatic(clazz: Class<*>, name: String, value: Any?) {
		getField(clazz, name, true).set(null, value)
	}

	@Suppress("UNCHECKED_CAST")
	override fun <T> getObject(any: Any, name: String): T = getField(any.javaClass, name, false).get(any) as T

	@Suppress("UNCHECKED_CAST")
	override fun <T> getObjectStatic(clazz: Class<*>, name: String): T = getField(clazz, name, true).get(null) as T

	override fun set(any: Any, name: String, value: Any?) {
		getField(any.javaClass, name, false).set(any, value)
	}

	override fun setStatic(clazz: Class<*>, name: String, value: Any?) {
		getField(clazz, name, true).set(null, value)
	}

	@Suppress("UNCHECKED_CAST")
	override fun <T> get(any: Any, name: String): T = getField(any.javaClass, name, false).get(any) as T

	@Suppress("UNCHECKED_CAST")
	override fun <T> getStatic(clazz: Class<*>, name: String): T = getField(clazz, name, true).get(null) as T

	override fun setByte(any: Any, field: Field, value: Byte) {
		field.setByte(any, value)
	}

	override fun setByteStatic(field: Field, value: Byte) {
		field.setByte(null, value)
	}

	override fun getByte(any: Any, field: Field): Byte = field.getByte(any)

	override fun getByteStatic(field: Field): Byte = field.getByte(null)

	override fun setShort(any: Any, field: Field, value: Short) {
		field.setShort(any, value)
	}

	override fun setShortStatic(field: Field, value: Short) {
		field.setShort(null, value)
	}

	override fun getShort(any: Any, field: Field): Short = field.getShort(any)

	override fun getShortStatic(field: Field): Short = field.getShort(null)

	override fun setInt(any: Any, field: Field, value: Int) {
		field.setInt(any, value)
	}

	override fun setIntStatic(field: Field, value: Int) {
		field.setInt(null, value)
	}

	override fun getInt(any: Any, field: Field): Int = field.getInt(any)

	override fun getIntStatic(field: Field): Int = field.getInt(null)

	override fun setLong(any: Any, field: Field, value: Long) {
		field.setLong(any, value)
	}

	override fun setLongStatic(field: Field, value: Long) {
		field.setLong(null, value)
	}

	override fun getLong(any: Any, field: Field): Long = field.getLong(any)

	override fun getLongStatic(field: Field): Long = field.getLong(null)

	override fun setFloat(any: Any, field: Field, value: Float) {
		field.setFloat(any, value)
	}

	override fun setFloatStatic(field: Field, value: Float) {
		field.setFloat(null, value)
	}

	override fun getFloat(any: Any, field: Field): Float = field.getFloat(any)

	override fun getFloatStatic(field: Field): Float = field.getFloat(null)

	override fun setDouble(any: Any, field: Field, value: Double) {
		field.setDouble(any, value)
	}

	override fun setDoubleStatic(field: Field, value: Double) {
		field.setDouble(null, value)
	}

	override fun getDouble(any: Any, field: Field): Double = field.getDouble(any)

	override fun getDoubleStatic(field: Field): Double = field.getDouble(null)

	override fun setChar(any: Any, field: Field, value: Char) {
		field.setChar(any, value)
	}

	override fun setCharStatic(field: Field, value: Char) {
		field.setChar(null, value)
	}

	override fun getChar(any: Any, field: Field): Char = field.getChar(any)

	override fun getCharStatic(field: Field): Char = field.getChar(null)

	override fun setBoolean(any: Any, field: Field, value: Boolean) {
		field.setBoolean(any, value)
	}

	override fun setBooleanStatic(field: Field, value: Boolean) {
		field.setBoolean(null, value)
	}

	override fun getBoolean(any: Any, field: Field): Boolean = field.getBoolean(any)

	override fun getBooleanStatic(field: Field): Boolean = field.getBoolean(null)

	override fun setObject(any: Any, field: Field, value: Any?) {
		field.set(any, value)
	}

	override fun setObjectStatic(field: Field, value: Any?) {
		field.set(null, value)
	}

	@Suppress("UNCHECKED_CAST")
	override fun <T> getObject(any: Any, field: Field): T = field.get(any) as T

	@Suppress("UNCHECKED_CAST")
	override fun <T> getObjectStatic(field: Field): T = field.get(null) as T

	override fun set(any: Any, field: Field, value: Any?) {
		field.set(any, value)
	}

	override fun setStatic(field: Field, value: Any?) {
		field.set(null, value)
	}

	@Suppress("UNCHECKED_CAST")
	override fun <T> get(any: Any, field: Field): T = field.get(any) as T

	@Suppress("UNCHECKED_CAST")
	override fun <T> getStatic(field: Field): T = field.get(null) as T
}
