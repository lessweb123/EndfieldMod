@file:Suppress("UNCHECKED_CAST")

package endfield.util

import arc.func.Prov
import arc.struct.IntMap
import java.lang.invoke.MethodHandle
import java.lang.invoke.MethodType
import java.lang.reflect.Constructor
import java.lang.reflect.Field
import java.lang.reflect.Method

/**
 * Wrap the [MethodHandle], no need to forcefully handle thrown exceptions, automatically convert types.
 *
 * @since 1.0.10
 */
open class MethodHandleInvoker(@JvmField val handle: MethodHandle) : MethodInvoker {
	companion object {
		@JvmField
		internal val generics = IntMap<MethodType>()

		@JvmStatic
		fun unreflectGetter(field: Field): MethodHandleInvoker = find(HandleSupport.unreflectGetter(field))

		@JvmStatic
		fun unreflectSetter(field: Field): MethodHandleInvoker = find(HandleSupport.unreflectSetter(field.also { Reflections.setAccessible(field) }))

		@JvmStatic
		fun unreflect(method: Method): MethodHandleInvoker = find(HandleSupport.unreflect(method))

		@JvmStatic
		fun unreflectSpecial(method: Method): MethodHandleInvoker = find(HandleSupport.unreflectSpecial(method))

		@JvmStatic
		fun unreflectConstructor(constructor: Constructor<*>): MethodHandleInvoker = find(HandleSupport.unreflectConstructor(constructor.also { Reflections.setAccessible(it) }))

		@JvmStatic
		fun findVirtual(refc: Class<*>, name: String, methodType: MethodType): MethodHandleInvoker = find(HandleSupport.findVirtual(refc, name, methodType))

		@JvmStatic
		fun findSpecial(refc: Class<*>, name: String, methodType: MethodType): MethodHandleInvoker = find(HandleSupport.findSpecial(refc, name, methodType))

		@JvmStatic
		fun findStatic(refc: Class<*>, name: String, methodType: MethodType): MethodHandleInvoker = find(HandleSupport.findStatic(refc, name, methodType))

		@JvmStatic
		fun findConstructor(refc: Class<*>, methodType: MethodType): MethodHandleInvoker = find(HandleSupport.findConstructor(refc, methodType))

		@JvmStatic
		fun findGetter(refc: Class<*>, name: String, type: Class<*>): MethodHandleInvoker = find(HandleSupport.findGetter(refc, name, type))

		@JvmStatic
		fun findSetter(refc: Class<*>, name: String, type: Class<*>): MethodHandleInvoker = find(HandleSupport.findSetter(refc, name, type))

		@JvmStatic
		fun findStaticGetter(refc: Class<*>, name: String, type: Class<*>): MethodHandleInvoker = find(HandleSupport.findStaticGetter(refc, name, type))

		@JvmStatic
		fun findStaticSetter(refc: Class<*>, name: String, type: Class<*>): MethodHandleInvoker = find(HandleSupport.findStaticSetter(refc, name, type))

		@JvmStatic
		fun find(target: MethodHandle): MethodHandleInvoker {
			val paramCount = target.type().parameterCount()
			val genericType = generics.get(paramCount, Prov { MethodType.genericMethodType(paramCount) })

			return InternalMethodHandleInvoker(target.asType(genericType))
		}
	}

	override fun <T> invoke(): T = handle.invoke() as T

	override fun <T, P0> invoke(p0: P0): T = handle.invoke(p0) as T

	override fun <T, P0, P1> invoke(p0: P0, p1: P1): T = handle.invoke(p0, p1) as T

	override fun <T, P0, P1, P2> invoke(p0: P0, p1: P1, p2: P2): T = handle.invoke(p0, p1, p2) as T

	override fun <T, P0, P1, P2, P3> invoke(p0: P0, p1: P1, p2: P2, p3: P3): T =
		handle.invoke(p0, p1, p2, p3) as T

	override fun <T, P0, P1, P2, P3, P4>
			invoke(p0: P0, p1: P1, p2: P2, p3: P3, p4: P4): T =
		handle.invoke(p0, p1, p2, p3, p4) as T

	override fun <T, P0, P1, P2, P3, P4, P5>
			invoke(p0: P0, p1: P1, p2: P2, p3: P3, p4: P4, p5: P5): T =
		handle.invoke(p0, p1, p2, p3, p4, p5) as T

	override fun <T, P0, P1, P2, P3, P4, P5, P6>
			invoke(p0: P0, p1: P1, p2: P2, p3: P3, p4: P4, p5: P5, p6: P6): T =
		handle.invoke(p0, p1, p2, p3, p4, p5, p6) as T

	override fun <T, P0, P1, P2, P3, P4, P5, P6, P7>
			invoke(p0: P0, p1: P1, p2: P2, p3: P3, p4: P4, p5: P5, p6: P6, p7: P7): T =
		handle.invoke(p0, p1, p2, p3, p4, p5, p6, p7) as T

	override fun <T, P0, P1, P2, P3, P4, P5, P6, P7, P8>
			invoke(p0: P0, p1: P1, p2: P2, p3: P3, p4: P4, p5: P5, p6: P6, p7: P7, p8: P8): T =
		handle.invoke(p0, p1, p2, p3, p4, p5, p6, p7, p8) as T

	override fun <T, P0, P1, P2, P3, P4, P5, P6, P7, P8, P9>
			invoke(p0: P0, p1: P1, p2: P2, p3: P3, p4: P4, p5: P5, p6: P6, p7: P7, p8: P8, p9: P9): T =
		handle.invoke(p0, p1, p2, p3, p4, p5, p6, p7, p8, p9) as T

	override fun <T, P0, P1, P2, P3, P4, P5, P6, P7, P8, P9, P10>
			invoke(p0: P0, p1: P1, p2: P2, p3: P3, p4: P4, p5: P5, p6: P6, p7: P7, p8: P8, p9: P9, p10: P10): T =
		handle.invoke(p0, p1, p2, p3, p4, p5, p6, p7, p8, p9, p10) as T

	override fun <T, P0, P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11>
			invoke(p0: P0, p1: P1, p2: P2, p3: P3, p4: P4, p5: P5, p6: P6, p7: P7, p8: P8, p9: P9, p10: P10, p11: P11): T =
		handle.invoke(p0, p1, p2, p3, p4, p5, p6, p7, p8, p9, p10, p11) as T

	override fun <T, P0, P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, P12>
			invoke(p0: P0, p1: P1, p2: P2, p3: P3, p4: P4, p5: P5, p6: P6, p7: P7, p8: P8, p9: P9, p10: P10, p11: P11, p12: P12): T =
		handle.invoke(p0, p1, p2, p3, p4, p5, p6, p7, p8, p9, p10, p11, p12) as T

	override fun <T, P0, P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, P12, P13>
			invoke(p0: P0, p1: P1, p2: P2, p3: P3, p4: P4, p5: P5, p6: P6, p7: P7, p8: P8, p9: P9, p10: P10, p11: P11, p12: P12, p13: P13): T =
		handle.invoke(p0, p1, p2, p3, p4, p5, p6, p7, p8, p9, p10, p11, p12, p13) as T

	override fun <T, P0, P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, P12, P13, P14>
			invoke(p0: P0, p1: P1, p2: P2, p3: P3, p4: P4, p5: P5, p6: P6, p7: P7, p8: P8, p9: P9, p10: P10, p11: P11, p12: P12, p13: P13, p14: P14): T =
		handle.invoke(p0, p1, p2, p3, p4, p5, p6, p7, p8, p9, p10, p11, p12, p13, p14) as T

	override fun <T, P0, P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, P12, P13, P14, P15>
			invoke(p0: P0, p1: P1, p2: P2, p3: P3, p4: P4, p5: P5, p6: P6, p7: P7, p8: P8, p9: P9, p10: P10, p11: P11, p12: P12, p13: P13, p14: P14, p15: P15): T =
		handle.invoke(p0, p1, p2, p3, p4, p5, p6, p7, p8, p9, p10, p11, p12, p13, p14, p15) as T

	override fun <T, P0, P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, P12, P13, P14, P15, P16>
			invoke(p0: P0, p1: P1, p2: P2, p3: P3, p4: P4, p5: P5, p6: P6, p7: P7, p8: P8, p9: P9, p10: P10, p11: P11, p12: P12, p13: P13, p14: P14, p15: P15, p16: P16): T =
		handle.invoke(p0, p1, p2, p3, p4, p5, p6, p7, p8, p9, p10, p11, p12, p13, p14, p15, p16) as T

	override fun <T, P0, P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, P12, P13, P14, P15, P16, P17>
			invoke(p0: P0, p1: P1, p2: P2, p3: P3, p4: P4, p5: P5, p6: P6, p7: P7, p8: P8, p9: P9, p10: P10, p11: P11, p12: P12, p13: P13, p14: P14, p15: P15, p16: P16, p17: P17): T =
		handle.invoke(p0, p1, p2, p3, p4, p5, p6, p7, p8, p9, p10, p11, p12, p13, p14, p15, p16, p17) as T

	override fun <T, P0, P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, P12, P13, P14, P15, P16, P17, P18>
			invoke(p0: P0, p1: P1, p2: P2, p3: P3, p4: P4, p5: P5, p6: P6, p7: P7, p8: P8, p9: P9, p10: P10, p11: P11, p12: P12, p13: P13, p14: P14, p15: P15, p16: P16, p17: P17, p18: P18): T =
		handle.invoke(p0, p1, p2, p3, p4, p5, p6, p7, p8, p9, p10, p11, p12, p13, p14, p15, p16, p17, p18) as T

	override fun <T, P0, P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, P12, P13, P14, P15, P16, P17, P18, P19>
			invoke(p0: P0, p1: P1, p2: P2, p3: P3, p4: P4, p5: P5, p6: P6, p7: P7, p8: P8, p9: P9, p10: P10, p11: P11, p12: P12, p13: P13, p14: P14, p15: P15, p16: P16, p17: P17, p18: P18, p19: P19): T =
		handle.invoke(p0, p1, p2, p3, p4, p5, p6, p7, p8, p9, p10, p11, p12, p13, p14, p15, p16, p17, p18, p19) as T

	override fun <T, P0, P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, P12, P13, P14, P15, P16, P17, P18, P19, P20>
			invoke(p0: P0, p1: P1, p2: P2, p3: P3, p4: P4, p5: P5, p6: P6, p7: P7, p8: P8, p9: P9, p10: P10, p11: P11, p12: P12, p13: P13, p14: P14, p15: P15, p16: P16, p17: P17, p18: P18, p19: P19, p20: P20): T =
		handle.invoke(p0, p1, p2, p3, p4, p5, p6, p7, p8, p9, p10, p11, p12, p13, p14, p15, p16, p17, p18, p19, p20) as T

	override fun <T, P0, P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, P12, P13, P14, P15, P16, P17, P18, P19, P20, P21>
			invoke(p0: P0, p1: P1, p2: P2, p3: P3, p4: P4, p5: P5, p6: P6, p7: P7, p8: P8, p9: P9, p10: P10, p11: P11, p12: P12, p13: P13, p14: P14, p15: P15, p16: P16, p17: P17, p18: P18, p19: P19, p20: P20, p21: P21): T =
		handle.invoke(p0, p1, p2, p3, p4, p5, p6, p7, p8, p9, p10, p11, p12, p13, p14, p15, p16, p17, p18, p19, p20, p21) as T

	override fun <T, P0, P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, P12, P13, P14, P15, P16, P17, P18, P19, P20, P21, P22>
			invoke(p0: P0, p1: P1, p2: P2, p3: P3, p4: P4, p5: P5, p6: P6, p7: P7, p8: P8, p9: P9, p10: P10, p11: P11, p12: P12, p13: P13, p14: P14, p15: P15, p16: P16, p17: P17, p18: P18, p19: P19, p20: P20, p21: P21, p22: P22): T =
		handle.invoke(p0, p1, p2, p3, p4, p5, p6, p7, p8, p9, p10, p11, p12, p13, p14, p15, p16, p17, p18, p19, p20, p21, p22) as T
}

internal class InternalMethodHandleInvoker(method: MethodHandle) : MethodHandleInvoker(method) {
	override fun <T> invoke(): T = handle.invokeExact() as T

	override fun <T, P0> invoke(p0: P0): T = handle.invokeExact(p0) as T

	override fun <T, P0, P1> invoke(p0: P0, p1: P1): T = handle.invokeExact(p0, p1) as T

	override fun <T, P0, P1, P2> invoke(p0: P0, p1: P1, p2: P2): T = handle.invokeExact(p0, p1, p2) as T

	override fun <T, P0, P1, P2, P3> invoke(p0: P0, p1: P1, p2: P2, p3: P3): T =
		handle.invokeExact(p0, p1, p2, p3) as T

	override fun <T, P0, P1, P2, P3, P4>
			invoke(p0: P0, p1: P1, p2: P2, p3: P3, p4: P4): T =
		handle.invokeExact(p0, p1, p2, p3, p4) as T

	override fun <T, P0, P1, P2, P3, P4, P5>
			invoke(p0: P0, p1: P1, p2: P2, p3: P3, p4: P4, p5: P5): T =
		handle.invokeExact(p0, p1, p2, p3, p4, p5) as T

	override fun <T, P0, P1, P2, P3, P4, P5, P6>
			invoke(p0: P0, p1: P1, p2: P2, p3: P3, p4: P4, p5: P5, p6: P6): T =
		handle.invokeExact(p0, p1, p2, p3, p4, p5, p6) as T

	override fun <T, P0, P1, P2, P3, P4, P5, P6, P7>
			invoke(p0: P0, p1: P1, p2: P2, p3: P3, p4: P4, p5: P5, p6: P6, p7: P7): T =
		handle.invokeExact(p0, p1, p2, p3, p4, p5, p6, p7) as T

	override fun <T, P0, P1, P2, P3, P4, P5, P6, P7, P8>
			invoke(p0: P0, p1: P1, p2: P2, p3: P3, p4: P4, p5: P5, p6: P6, p7: P7, p8: P8): T =
		handle.invokeExact(p0, p1, p2, p3, p4, p5, p6, p7, p8) as T

	override fun <T, P0, P1, P2, P3, P4, P5, P6, P7, P8, P9>
			invoke(p0: P0, p1: P1, p2: P2, p3: P3, p4: P4, p5: P5, p6: P6, p7: P7, p8: P8, p9: P9): T =
		handle.invokeExact(p0, p1, p2, p3, p4, p5, p6, p7, p8, p9) as T

	override fun <T, P0, P1, P2, P3, P4, P5, P6, P7, P8, P9, P10>
			invoke(p0: P0, p1: P1, p2: P2, p3: P3, p4: P4, p5: P5, p6: P6, p7: P7, p8: P8, p9: P9, p10: P10): T =
		handle.invokeExact(p0, p1, p2, p3, p4, p5, p6, p7, p8, p9, p10) as T

	override fun <T, P0, P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11>
			invoke(p0: P0, p1: P1, p2: P2, p3: P3, p4: P4, p5: P5, p6: P6, p7: P7, p8: P8, p9: P9, p10: P10, p11: P11): T =
		handle.invokeExact(p0, p1, p2, p3, p4, p5, p6, p7, p8, p9, p10, p11) as T

	override fun <T, P0, P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, P12>
			invoke(p0: P0, p1: P1, p2: P2, p3: P3, p4: P4, p5: P5, p6: P6, p7: P7, p8: P8, p9: P9, p10: P10, p11: P11, p12: P12): T =
		handle.invokeExact(p0, p1, p2, p3, p4, p5, p6, p7, p8, p9, p10, p11, p12) as T

	override fun <T, P0, P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, P12, P13>
			invoke(p0: P0, p1: P1, p2: P2, p3: P3, p4: P4, p5: P5, p6: P6, p7: P7, p8: P8, p9: P9, p10: P10, p11: P11, p12: P12, p13: P13): T =
		handle.invokeExact(p0, p1, p2, p3, p4, p5, p6, p7, p8, p9, p10, p11, p12, p13) as T

	override fun <T, P0, P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, P12, P13, P14>
			invoke(p0: P0, p1: P1, p2: P2, p3: P3, p4: P4, p5: P5, p6: P6, p7: P7, p8: P8, p9: P9, p10: P10, p11: P11, p12: P12, p13: P13, p14: P14): T =
		handle.invokeExact(p0, p1, p2, p3, p4, p5, p6, p7, p8, p9, p10, p11, p12, p13, p14) as T

	override fun <T, P0, P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, P12, P13, P14, P15>
			invoke(p0: P0, p1: P1, p2: P2, p3: P3, p4: P4, p5: P5, p6: P6, p7: P7, p8: P8, p9: P9, p10: P10, p11: P11, p12: P12, p13: P13, p14: P14, p15: P15): T =
		handle.invokeExact(p0, p1, p2, p3, p4, p5, p6, p7, p8, p9, p10, p11, p12, p13, p14, p15) as T

	override fun <T, P0, P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, P12, P13, P14, P15, P16>
			invoke(p0: P0, p1: P1, p2: P2, p3: P3, p4: P4, p5: P5, p6: P6, p7: P7, p8: P8, p9: P9, p10: P10, p11: P11, p12: P12, p13: P13, p14: P14, p15: P15, p16: P16): T =
		handle.invokeExact(p0, p1, p2, p3, p4, p5, p6, p7, p8, p9, p10, p11, p12, p13, p14, p15, p16) as T

	override fun <T, P0, P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, P12, P13, P14, P15, P16, P17>
			invoke(p0: P0, p1: P1, p2: P2, p3: P3, p4: P4, p5: P5, p6: P6, p7: P7, p8: P8, p9: P9, p10: P10, p11: P11, p12: P12, p13: P13, p14: P14, p15: P15, p16: P16, p17: P17): T =
		handle.invokeExact(p0, p1, p2, p3, p4, p5, p6, p7, p8, p9, p10, p11, p12, p13, p14, p15, p16, p17) as T

	override fun <T, P0, P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, P12, P13, P14, P15, P16, P17, P18>
			invoke(p0: P0, p1: P1, p2: P2, p3: P3, p4: P4, p5: P5, p6: P6, p7: P7, p8: P8, p9: P9, p10: P10, p11: P11, p12: P12, p13: P13, p14: P14, p15: P15, p16: P16, p17: P17, p18: P18): T =
		handle.invokeExact(p0, p1, p2, p3, p4, p5, p6, p7, p8, p9, p10, p11, p12, p13, p14, p15, p16, p17, p18) as T

	override fun <T, P0, P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, P12, P13, P14, P15, P16, P17, P18, P19>
			invoke(p0: P0, p1: P1, p2: P2, p3: P3, p4: P4, p5: P5, p6: P6, p7: P7, p8: P8, p9: P9, p10: P10, p11: P11, p12: P12, p13: P13, p14: P14, p15: P15, p16: P16, p17: P17, p18: P18, p19: P19): T =
		handle.invokeExact(p0, p1, p2, p3, p4, p5, p6, p7, p8, p9, p10, p11, p12, p13, p14, p15, p16, p17, p18, p19) as T

	override fun <T, P0, P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, P12, P13, P14, P15, P16, P17, P18, P19, P20>
			invoke(p0: P0, p1: P1, p2: P2, p3: P3, p4: P4, p5: P5, p6: P6, p7: P7, p8: P8, p9: P9, p10: P10, p11: P11, p12: P12, p13: P13, p14: P14, p15: P15, p16: P16, p17: P17, p18: P18, p19: P19, p20: P20): T =
		handle.invokeExact(p0, p1, p2, p3, p4, p5, p6, p7, p8, p9, p10, p11, p12, p13, p14, p15, p16, p17, p18, p19, p20) as T

	override fun <T, P0, P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, P12, P13, P14, P15, P16, P17, P18, P19, P20, P21>
			invoke(p0: P0, p1: P1, p2: P2, p3: P3, p4: P4, p5: P5, p6: P6, p7: P7, p8: P8, p9: P9, p10: P10, p11: P11, p12: P12, p13: P13, p14: P14, p15: P15, p16: P16, p17: P17, p18: P18, p19: P19, p20: P20, p21: P21): T =
		handle.invokeExact(p0, p1, p2, p3, p4, p5, p6, p7, p8, p9, p10, p11, p12, p13, p14, p15, p16, p17, p18, p19, p20, p21) as T

	override fun <T, P0, P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, P12, P13, P14, P15, P16, P17, P18, P19, P20, P21, P22>
			invoke(p0: P0, p1: P1, p2: P2, p3: P3, p4: P4, p5: P5, p6: P6, p7: P7, p8: P8, p9: P9, p10: P10, p11: P11, p12: P12, p13: P13, p14: P14, p15: P15, p16: P16, p17: P17, p18: P18, p19: P19, p20: P20, p21: P21, p22: P22): T =
		handle.invokeExact(p0, p1, p2, p3, p4, p5, p6, p7, p8, p9, p10, p11, p12, p13, p14, p15, p16, p17, p18, p19, p20, p21, p22) as T
}