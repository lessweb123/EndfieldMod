package endfield.android

import endfield.util.CollectionObjectMap
import endfield.util.Invokers
import java.lang.invoke.MethodHandle
import java.lang.invoke.MethodHandles.Lookup

class AndroidInvokers : Invokers {
	override fun lookup(refc: Class<*>): Lookup = lookupMap.getOrPut(refc) { lookupConstructor.newInstance(refc, 15) }

	override fun findSetter(refc: Class<*>, name: String, type: Class<*>): MethodHandle = lookup(refc).unreflectSetter(refc.getDeclaredField(name).apply { isAccessible = true })

	override fun findStaticSetter(refc: Class<*>, name: String, type: Class<*>): MethodHandle = lookup(refc).unreflectSetter(refc.getDeclaredField(name).apply { isAccessible = true })

	companion object {
		@JvmField
		val lookupConstructor = Lookup::class.java.getDeclaredConstructor(Class::class.java, Integer.TYPE).apply { isAccessible = true }
		@JvmField
		val lookupMap = CollectionObjectMap<Class<*>, Lookup>(Class::class.java, Lookup::class.java)
	}
}
