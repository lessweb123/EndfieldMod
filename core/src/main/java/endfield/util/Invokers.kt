package endfield.util

import java.lang.invoke.MethodHandle
import java.lang.invoke.MethodHandles.Lookup
import java.lang.invoke.MethodType
import java.lang.reflect.Constructor
import java.lang.reflect.Field
import java.lang.reflect.Method

interface Invokers {
	fun lookup(refc: Class<*>): Lookup

	fun findVirtual(refc: Class<*>, name: String, type: MethodType): MethodHandle = lookup(refc).findVirtual(refc, name, type)

	fun findStatic(refc: Class<*>, name: String, type: MethodType): MethodHandle = lookup(refc).findStatic(refc, name, type)

	fun findConstructor(refc: Class<*>, type: MethodType): MethodHandle = lookup(refc).findConstructor(refc, type)

	fun findSpecial(refc: Class<*>, name: String, type: MethodType): MethodHandle = lookup(refc).findSpecial(refc, name, type, refc)

	fun findGetter(refc: Class<*>, name: String, type: Class<*>): MethodHandle = lookup(refc).findGetter(refc, name, type)

	fun findStaticGetter(refc: Class<*>, name: String, type: Class<*>): MethodHandle = lookup(refc).findStaticGetter(refc, name, type)

	fun findSetter(refc: Class<*>, name: String, type: Class<*>): MethodHandle = lookup(refc).findSetter(refc, name, type)

	fun findStaticSetter(refc: Class<*>, name: String, type: Class<*>): MethodHandle = lookup(refc).findStaticSetter(refc, name, type)

	fun unreflect(method: Method): MethodHandle = lookup(method.declaringClass).unreflect(method)

	fun unreflectSpecial(method: Method): MethodHandle = lookup(method.declaringClass).unreflectSpecial(method, method.declaringClass)

	fun unreflectConstructor(constructor: Constructor<*>): MethodHandle = lookup(constructor.declaringClass).unreflectConstructor(constructor)

	fun unreflectGetter(field: Field): MethodHandle = lookup(field.declaringClass).unreflectGetter(field)

	fun unreflectSetter(field: Field): MethodHandle = lookup(field.declaringClass).unreflectSetter(field)
}
