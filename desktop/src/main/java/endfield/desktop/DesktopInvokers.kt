package endfield.desktop

import endfield.desktop.DesktopImpl.lookup
import endfield.util.CollectionObjectMap
import endfield.util.Invokers
import java.lang.invoke.MethodHandle
import java.lang.invoke.MethodHandles.Lookup
import java.lang.invoke.MethodType
import java.lang.reflect.Constructor
import java.lang.reflect.Field
import java.lang.reflect.Method
import java.lang.reflect.Modifier

class DesktopInvokers : Invokers {
	override fun lookup(refc: Class<*>): Lookup = lookupMap.getOrPut(refc) { lookupCtor.invokeExact(refc, null as Class<*>?, 95) as Lookup }

	override fun findVirtual(refc: Class<*>, name: String, type: MethodType): MethodHandle = lookup.findVirtual(refc, name, type)

	override fun findStatic(refc: Class<*>, name: String, type: MethodType): MethodHandle = lookup.findStatic(refc, name, type)

	override fun findConstructor(refc: Class<*>, type: MethodType): MethodHandle = lookup.findConstructor(refc, type)

	override fun findSpecial(refc: Class<*>, name: String, type: MethodType): MethodHandle = lookup.findSpecial(refc, name, type, refc)

	override fun findGetter(refc: Class<*>, name: String, type: Class<*>): MethodHandle = lookup.findGetter(refc, name, type)

	override fun findStaticGetter(refc: Class<*>, name: String, type: Class<*>): MethodHandle = lookup.findStaticGetter(refc, name, type)

	override fun findSetter(refc: Class<*>, name: String, type: Class<*>): MethodHandle = lookup.findSetter(refc, name, type)

	override fun findStaticSetter(refc: Class<*>, name: String, type: Class<*>): MethodHandle = lookup.findStaticSetter(refc, name, type)

	override fun unreflect(method: Method): MethodHandle = lookup.unreflect(method)

	override fun unreflectSpecial(method: Method): MethodHandle = lookup.unreflectSpecial(method, method.declaringClass)

	override fun unreflectConstructor(constructor: Constructor<*>): MethodHandle = lookup.unreflectConstructor(constructor)

	override fun unreflectGetter(field: Field): MethodHandle = lookup.unreflectGetter(field)

	override fun unreflectSetter(field: Field): MethodHandle {
		val modifiers = field.modifiers
		if ((modifiers and Modifier.FINAL) == 0) return lookup.unreflectSetter(field)
		if ((modifiers and Modifier.STATIC) != 0) {
			return lookup.findStaticSetter(field.declaringClass, field.name, field.type)
		} else {
			return lookup.findSetter(field.declaringClass, field.name, field.type)
		}
	}

	companion object {
		@JvmField
		val lookupCtor: MethodHandle = lookup.findConstructor(Lookup::class.java, MethodType.methodType(Void.TYPE, arrayOf(Class::class.java, Class::class.java, Integer.TYPE)))

		@JvmField
		val lookupMap: CollectionObjectMap<Class<*>, Lookup> = CollectionObjectMap<Class<*>, Lookup>(Class::class.java, Lookup::class.java)
	}
}
