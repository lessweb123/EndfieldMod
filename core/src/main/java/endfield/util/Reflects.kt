package endfield.util

import java.lang.invoke.MethodHandle
import java.lang.invoke.MethodType
import java.lang.reflect.Constructor
import java.lang.reflect.Field
import java.lang.reflect.Method

fun findGetter(refc: Class<*>, name: String, type: Class<*>): MethodHandle = Reflects.lookup(refc).findGetter(refc, name, type)

fun findSetter(refc: Class<*>, name: String, type: Class<*>): MethodHandle = Reflects.lookup(refc).findSetter(refc, name, type)

fun findStaticGetter(refc: Class<*>, name: String, type: Class<*>): MethodHandle = Reflects.lookup(refc).findStaticGetter(refc, name, type)

fun findStaticSetter(refc: Class<*>, name: String, type: Class<*>): MethodHandle = Reflects.lookup(refc).findStaticSetter(refc, name, type)

fun findGetter(field: Field): MethodHandle = Reflects.lookup(field.declaringClass).unreflectGetter(field)

fun findSetter(field: Field): MethodHandle = Reflects.lookup(field.declaringClass).unreflectSetter(field)

fun findVirtual(refc: Class<*>, name: String, methodType: MethodType): MethodHandle = Reflects.lookup(refc).findVirtual(refc, name, methodType)

fun findStatic(refc: Class<*>, name: String, methodType: MethodType): MethodHandle = Reflects.lookup(refc).findStatic(refc, name, methodType)

fun findVirtual(method: Method): MethodHandle = Reflects.lookup(method.declaringClass).unreflect(method)

fun findConstructor(refc: Class<*>, methodType: MethodType): MethodHandle = Reflects.lookup(refc).findConstructor(refc, methodType)

fun findConstructor(constructor: Constructor<*>): MethodHandle = Reflects.lookup(constructor.declaringClass).unreflectConstructor(constructor)

fun findSpecial(refc: Class<*>, name: String, methodType: MethodType): MethodHandle = Reflects.lookup(refc).findSpecial(refc, name, methodType, refc)

fun findSpecial(method: Method): MethodHandle = Reflects.lookup(method.declaringClass).unreflectSpecial(method, method.declaringClass)
