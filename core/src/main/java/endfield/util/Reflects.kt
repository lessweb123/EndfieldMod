package endfield.util

import endfield.Vars2.platformImpl
import java.lang.invoke.MethodHandle
import java.lang.invoke.MethodType
import java.lang.invoke.VarHandle

fun findSetter(clazz: Class<*>, name: String, type: Class<*>): MethodHandle =
	platformImpl.lookup(clazz).findSetter(clazz, name, type)

fun findGetter(clazz: Class<*>, name: String, type: Class<*>): MethodHandle =
	platformImpl.lookup(clazz).findGetter(clazz, name, type)

fun findStaticSetter(clazz: Class<*>, name: String, type: Class<*>): MethodHandle =
	platformImpl.lookup(clazz).findStaticSetter(clazz, name, type)

fun findStaticGetter(clazz: Class<*>, name: String, type: Class<*>): MethodHandle =
	platformImpl.lookup(clazz).findStaticGetter(clazz, name, type)

fun findVarHandle(clazz: Class<*>, name: String, type: Class<*>): VarHandle =
	platformImpl.lookup(clazz).findVarHandle(clazz, name, type)

fun findVirtual(clazz: Class<*>, name: String, returnType: Class<*>, parameterTypes: Array<Class<*>>): MethodHandle =
	platformImpl.lookup(clazz).findVirtual(clazz, name, MethodType.methodType(returnType, parameterTypes))

fun findStatic(clazz: Class<*>, name: String, returnType: Class<*>, parameterTypes: Array<Class<*>>): MethodHandle =
	platformImpl.lookup(clazz).findStatic(clazz, name, MethodType.methodType(returnType, parameterTypes))