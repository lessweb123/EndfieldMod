@file:Suppress("UNCHECKED_CAST")

package endfield.desktop

import endfield.desktop.DesktopClassHelper.getFields
import endfield.desktop.DesktopImpl.lookup
import endfield.util.AbstractFieldAccessorHelper
import endfield.util.CollectionObjectMap
import java.lang.invoke.MethodHandle
import java.lang.reflect.Field
import java.lang.reflect.Modifier

class MethodHandleFieldAccessHelper : AbstractFieldAccessorHelper() {
	companion object {
		val getters = CollectionObjectMap<Field, MethodHandle>(Field::class.java, MethodHandle::class.java)
		val setters = CollectionObjectMap<Field, MethodHandle>(Field::class.java, MethodHandle::class.java)
	}

	override fun getField(clazz: Class<*>, name: String, isStatic: Boolean): Field {
		val map = fieldMap.getOrPut(clazz) { CollectionObjectMap(String::class.java, Field::class.java) }
		var field = map[name]
		if (field != null) return field

		if (isStatic) {
			field = findField(clazz, name)
			if (field != null && (field.modifiers and Modifier.STATIC) != 0) {
				map[name] = field
				return field
			}
		} else {
			var current: Class<*>? = clazz
			while (current != null) {
				field = findField(current, name)
				if (field != null && (field.modifiers and Modifier.STATIC) == 0) {
					map[name] = field
					return field
				}

				current = current.superclass
			}
		}

		throw NoSuchFieldException("field $name was not found in class: $clazz")
	}

	override fun findField(clazz: Class<*>, name: String): Field? {
		val field = fieldsMap.getOrPut(clazz) { getFields.invokeExact(clazz, false) as Array<Field> }
		for (field in field) {
			if (field.name == name) return field
		}
		return null
	}

	fun getter(field: Field): MethodHandle = getters.getOrPut(field) {
		if ((field.modifiers and Modifier.STATIC) != 0) lookup.findStaticGetter(field.declaringClass, field.name, field.type) else lookup.findGetter(field.declaringClass, field.name, field.type)
	}

	fun setter(field: Field): MethodHandle = setters.getOrPut(field) {
		if ((field.modifiers and Modifier.STATIC) != 0) lookup.findStaticSetter(field.declaringClass, field.name, field.type) else lookup.findSetter(field.declaringClass, field.name, field.type)
	}

	override fun setByte(any: Any, name: String, value: Byte) {
		setter(getField(any.javaClass, name, false)).invoke(any, value)
	}

	override fun setByteStatic(clazz: Class<*>, name: String, value: Byte) {
		setter(getField(clazz, name, true)).invoke(value)
	}

	override fun getByte(any: Any, name: String): Byte = getter(getField(any.javaClass, name, false)).invoke(any) as Byte

	override fun getByteStatic(clazz: Class<*>, name: String): Byte = getter(getField(clazz, name, true)).invoke() as Byte

	override fun setShort(any: Any, name: String, value: Short) {
		setter(getField(any.javaClass, name, false)).invoke(any, value)
	}

	override fun setShortStatic(clazz: Class<*>, name: String, value: Short) {
		setter(getField(clazz, name, true)).invoke(value)
	}

	override fun getShort(any: Any, name: String): Short = getter(getField(any.javaClass, name, false)).invoke(any) as Short

	override fun getShortStatic(clazz: Class<*>, name: String): Short = getter(getField(clazz, name, true)).invoke() as Short

	override fun setInt(any: Any, name: String, value: Int) {
		setter(getField(any.javaClass, name, false)).invoke(any, value)
	}

	override fun setIntStatic(clazz: Class<*>, name: String, value: Int) {
		setter(getField(clazz, name, true)).invoke(value)
	}

	override fun getInt(any: Any, name: String): Int = getter(getField(any.javaClass, name, false)).invoke(any) as Int

	override fun getIntStatic(clazz: Class<*>, name: String): Int = getter(getField(clazz, name, true)).invoke() as Int

	override fun setLong(any: Any, name: String, value: Long) {
		setter(getField(any.javaClass, name, false)).invoke(any, value)
	}

	override fun setLongStatic(clazz: Class<*>, name: String, value: Long) {
		setter(getField(clazz, name, true)).invoke(value)
	}

	override fun getLong(any: Any, name: String): Long = getter(getField(any.javaClass, name, false)).invoke(any) as Long

	override fun getLongStatic(clazz: Class<*>, name: String): Long = getter(getField(clazz, name, true)).invoke() as Long

	override fun setFloat(any: Any, name: String, value: Float) {
		setter(getField(any.javaClass, name, false)).invoke(any, value)
	}

	override fun setFloatStatic(clazz: Class<*>, name: String, value: Float) {
		setter(getField(clazz, name, true)).invoke(value)
	}

	override fun getFloat(any: Any, name: String): Float = getter(getField(any.javaClass, name, false)).invoke(any) as Float

	override fun getFloatStatic(clazz: Class<*>, name: String): Float = getter(getField(clazz, name, true)).invoke() as Float

	override fun setDouble(any: Any, name: String, value: Double) {
		setter(getField(any.javaClass, name, false)).invoke(any, value)
	}

	override fun setDoubleStatic(clazz: Class<*>, name: String, value: Double) {
		setter(getField(clazz, name, true)).invoke(value)
	}

	override fun getDouble(any: Any, name: String): Double = getter(getField(any.javaClass, name, false)).invoke(any) as Double

	override fun getDoubleStatic(clazz: Class<*>, name: String): Double = getter(getField(clazz, name, true)).invoke() as Double

	override fun setChar(any: Any, name: String, value: Char) {
		setter(getField(any.javaClass, name, false)).invoke(any, value)
	}

	override fun setCharStatic(clazz: Class<*>, name: String, value: Char) {
		setter(getField(clazz, name, true)).invoke(value)
	}

	override fun getChar(any: Any, name: String): Char = getter(getField(any.javaClass, name, false)).invoke(any) as Char

	override fun getCharStatic(clazz: Class<*>, name: String): Char = getter(getField(clazz, name, true)).invoke() as Char

	override fun setBoolean(any: Any, name: String, value: Boolean) {
		setter(getField(any.javaClass, name, false)).invoke(any, value)
	}

	override fun setBooleanStatic(clazz: Class<*>, name: String, value: Boolean) {
		setter(getField(clazz, name, true)).invoke(value)
	}

	override fun getBoolean(any: Any, name: String): Boolean = getter(getField(any.javaClass, name, false)).invoke(any) as Boolean

	override fun getBooleanStatic(clazz: Class<*>, name: String): Boolean = getter(getField(clazz, name, true)).invoke() as Boolean

	override fun setObject(any: Any, name: String, value: Any?) {
		setter(getField(any.javaClass, name, false)).invoke(any, value)
	}

	override fun setObjectStatic(clazz: Class<*>, name: String, value: Any?) {
		setter(getField(clazz, name, true)).invoke(value)
	}

	override fun <T> getObject(any: Any, name: String): T = getter(getField(any.javaClass, name, false)).invoke(any) as T

	override fun <T> getObjectStatic(clazz: Class<*>, name: String): T = getter(getField(clazz, name, true)).invoke() as T

	override fun set(any: Any, name: String, value: Any?) {
		setter(getField(any.javaClass, name, false)).invoke(any, value)
	}

	override fun setStatic(clazz: Class<*>, name: String, value: Any?) {
		setter(getField(clazz, name, true)).invoke(value)
	}

	override fun <T> get(any: Any, name: String): T = getter(getField(any.javaClass, name, false)).invoke(any) as T

	override fun <T> getStatic(clazz: Class<*>, name: String): T = getter(getField(clazz, name, true)).invoke() as T

	override fun setByte(any: Any, field: Field, value: Byte) {
		setter(field).invoke(any, value)
	}

	override fun setByteStatic(field: Field, value: Byte) {
		setter(field).invoke(value)
	}

	override fun getByte(any: Any, field: Field): Byte = getter(field).invoke(any) as Byte

	override fun getByteStatic(field: Field): Byte = getter(field).invoke() as Byte

	override fun setShort(any: Any, field: Field, value: Short) {
		setter(field).invoke(any, value)
	}

	override fun setShortStatic(field: Field, value: Short) {
		setter(field).invoke(value)
	}

	override fun getShort(any: Any, field: Field): Short = getter(field).invoke(any) as Short

	override fun getShortStatic(field: Field): Short = getter(field).invoke() as Short

	override fun setInt(any: Any, field: Field, value: Int) {
		setter(field).invoke(any, value)
	}

	override fun setIntStatic(field: Field, value: Int) {
		setter(field).invoke(value)
	}

	override fun getInt(any: Any, field: Field): Int = getter(field).invoke(any) as Int

	override fun getIntStatic(field: Field): Int = getter(field).invoke() as Int

	override fun setLong(any: Any, field: Field, value: Long) {
		setter(field).invoke(any, value)
	}

	override fun setLongStatic(field: Field, value: Long) {
		setter(field).invoke(value)
	}

	override fun getLong(any: Any, field: Field): Long = getter(field).invoke(any) as Long

	override fun getLongStatic(field: Field): Long = getter(field).invoke() as Long

	override fun setFloat(any: Any, field: Field, value: Float) {
		setter(field).invoke(any, value)
	}

	override fun setFloatStatic(field: Field, value: Float) {
		setter(field).invoke(value)
	}

	override fun getFloat(any: Any, field: Field): Float = getter(field).invoke(any) as Float

	override fun getFloatStatic(field: Field): Float = getter(field).invoke() as Float

	override fun setDouble(any: Any, field: Field, value: Double) {
		setter(field).invoke(any, value)
	}

	override fun setDoubleStatic(field: Field, value: Double) {
		setter(field).invoke(value)
	}

	override fun getDouble(any: Any, field: Field): Double = getter(field).invoke(any) as Double

	override fun getDoubleStatic(field: Field): Double = getter(field).invoke() as Double

	override fun setChar(any: Any, field: Field, value: Char) {
		setter(field).invoke(any, value)
	}

	override fun setCharStatic(field: Field, value: Char) {
		setter(field).invoke(value)
	}

	override fun getChar(any: Any, field: Field): Char = getter(field).invoke(any) as Char

	override fun getCharStatic(field: Field): Char = getter(field).invoke() as Char

	override fun setBoolean(any: Any, field: Field, value: Boolean) {
		setter(field).invoke(any, value)
	}

	override fun setBooleanStatic(field: Field, value: Boolean) {
		setter(field).invoke(value)
	}

	override fun getBoolean(any: Any, field: Field): Boolean = getter(field).invoke(any) as Boolean

	override fun getBooleanStatic(field: Field): Boolean = getter(field).invoke() as Boolean

	override fun setObject(any: Any, field: Field, value: Any?) {
		setter(field).invoke(any, value)
	}

	override fun setObjectStatic(field: Field, value: Any?) {
		setter(field).invoke(value)
	}

	override fun <T> getObject(any: Any, field: Field): T = getter(field).invoke(any) as T

	override fun <T> getObjectStatic(field: Field): T = getter(field).invoke() as T

	override fun set(any: Any, field: Field, value: Any?) {
		setter(field).invoke(any, value)
	}

	override fun setStatic(field: Field, value: Any?) {
		setter(field).invoke(value)
	}

	override fun <T> get(any: Any, field: Field): T = getter(field).invoke(any) as T

	override fun <T> getStatic(field: Field): T = getter(field).invoke() as T
}