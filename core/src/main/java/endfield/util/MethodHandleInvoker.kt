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
open class MethodHandleInvoker(val method: MethodHandle) : MethodInvoker {
	companion object {
		@JvmField
		val generics = IntMap<MethodType>()

		@JvmStatic
		fun unreflectGetter(field: Field): MethodHandleInvoker {
			Reflects.setAccessible(field)
			return create(Reflects.lookup(field.declaringClass).unreflectGetter(field))
		}

		@JvmStatic
		fun unreflectSetter(field: Field): MethodHandleInvoker {
			Reflects.setAccessible(field)
			return create(Reflects.lookup(field.declaringClass).unreflectSetter(field))
		}

		@JvmStatic
		fun unreflect(method: Method): MethodHandleInvoker {
			Reflects.setAccessible(method)
			return create(Reflects.lookup(method.declaringClass).unreflect(method))
		}

		@JvmStatic
		fun unreflect(constructor: Constructor<*>): MethodHandleInvoker {
			Reflects.setAccessible(constructor)
			return create(Reflects.lookup(constructor.declaringClass).unreflectConstructor(constructor))
		}

		@JvmStatic
		fun findVirtual(declaring: Class<*>, name: String, methodType: MethodType): MethodHandleInvoker {
			return create(Reflects.lookup(declaring).findVirtual(declaring, name, methodType))
		}

		@JvmStatic
		fun findSpecial(declaring: Class<*>, name: String, methodType: MethodType): MethodHandleInvoker {
			return create(Reflects.lookup(declaring).findSpecial(declaring, name, methodType, declaring))
		}

		@JvmStatic
		fun findStatic(declaring: Class<*>, name: String, methodType: MethodType): MethodHandleInvoker {
			return create(Reflects.lookup(declaring).findStatic(declaring, name, methodType))
		}

		@JvmStatic
		fun findConstructor(declaring: Class<*>, methodType: MethodType): MethodHandleInvoker {
			return create(Reflects.lookup(declaring).findConstructor(declaring, methodType))
		}

		@JvmStatic
		fun findGetter(declaring: Class<*>, name: String, type: Class<*>): MethodHandleInvoker {
			return create(Reflects.lookup(declaring).findGetter(declaring, name, type))
		}

		@JvmStatic
		fun findSetter(declaring: Class<*>, name: String, type: Class<*>): MethodHandleInvoker {
			return create(Reflects.lookup(declaring).findSetter(declaring, name, type))
		}

		@JvmStatic
		fun findStaticGetter(declaring: Class<*>, name: String, type: Class<*>): MethodHandleInvoker {
			return create(Reflects.lookup(declaring).findStaticGetter(declaring, name, type))
		}

		@JvmStatic
		fun findStaticSetter(declaring: Class<*>, name: String, type: Class<*>): MethodHandleInvoker {
			return create(Reflects.lookup(declaring).findStaticSetter(declaring, name, type))
		}

		@JvmStatic
		fun create(target: MethodHandle): MethodHandleInvoker {
			val paramCount = target.type().parameterCount()
			val genericType = generics.get(paramCount, Prov { MethodType.genericMethodType(paramCount) })

			return MethodHandleExactInvoker(target.asType(genericType))
		}
	}

	var hash = 0

	override fun <T> invoke(): T = method.invoke() as T

	override fun <T, P0> invoke(p0: P0): T = method.invoke(p0) as T

	override fun <T, P0, P1> invoke(p0: P0, p1: P1): T = method.invoke(p0, p1) as T

	override fun <T, P0, P1, P2> invoke(p0: P0, p1: P1, p2: P2): T = method.invoke(p0, p1, p2) as T

	override fun <T, P0, P1, P2, P3> invoke(p0: P0, p1: P1, p2: P2, p3: P3): T =
		method.invoke(p0, p1, p2, p3) as T

	override fun <T, P0, P1, P2, P3, P4>
			invoke(p0: P0, p1: P1, p2: P2, p3: P3, p4: P4): T =
		method.invoke(p0, p1, p2, p3, p4) as T

	override fun <T, P0, P1, P2, P3, P4, P5>
			invoke(p0: P0, p1: P1, p2: P2, p3: P3, p4: P4, p5: P5): T =
		method.invoke(p0, p1, p2, p3, p4, p5) as T

	override fun <T, P0, P1, P2, P3, P4, P5, P6>
			invoke(p0: P0, p1: P1, p2: P2, p3: P3, p4: P4, p5: P5, p6: P6): T =
		method.invoke(p0, p1, p2, p3, p4, p5, p6) as T

	override fun <T, P0, P1, P2, P3, P4, P5, P6, P7>
			invoke(p0: P0, p1: P1, p2: P2, p3: P3, p4: P4, p5: P5, p6: P6, p7: P7): T =
		method.invoke(p0, p1, p2, p3, p4, p5, p6, p7) as T

	override fun <T, P0, P1, P2, P3, P4, P5, P6, P7, P8>
			invoke(p0: P0, p1: P1, p2: P2, p3: P3, p4: P4, p5: P5, p6: P6, p7: P7, p8: P8): T =
		method.invoke(p0, p1, p2, p3, p4, p5, p6, p7, p8) as T

	override fun <T, P0, P1, P2, P3, P4, P5, P6, P7, P8, P9>
			invoke(p0: P0, p1: P1, p2: P2, p3: P3, p4: P4, p5: P5, p6: P6, p7: P7, p8: P8, p9: P9): T =
		method.invoke(p0, p1, p2, p3, p4, p5, p6, p7, p8, p9) as T

	override fun <T, P0, P1, P2, P3, P4, P5, P6, P7, P8, P9, P10>
			invoke(p0: P0, p1: P1, p2: P2, p3: P3, p4: P4, p5: P5, p6: P6, p7: P7, p8: P8, p9: P9, p10: P10): T =
		method.invoke(p0, p1, p2, p3, p4, p5, p6, p7, p8, p9, p10) as T

	override fun <T, P0, P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11>
			invoke(p0: P0, p1: P1, p2: P2, p3: P3, p4: P4, p5: P5, p6: P6, p7: P7, p8: P8, p9: P9, p10: P10, p11: P11): T =
		method.invoke(p0, p1, p2, p3, p4, p5, p6, p7, p8, p9, p10, p11) as T

	override fun <T, P0, P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, P12>
			invoke(p0: P0, p1: P1, p2: P2, p3: P3, p4: P4, p5: P5, p6: P6, p7: P7, p8: P8, p9: P9, p10: P10, p11: P11, p12: P12): T =
		method.invoke(p0, p1, p2, p3, p4, p5, p6, p7, p8, p9, p10, p11, p12) as T

	override fun <T, P0, P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, P12, P13>
			invoke(p0: P0, p1: P1, p2: P2, p3: P3, p4: P4, p5: P5, p6: P6, p7: P7, p8: P8, p9: P9, p10: P10, p11: P11, p12: P12, p13: P13): T =
		method.invoke(p0, p1, p2, p3, p4, p5, p6, p7, p8, p9, p10, p11, p12, p13) as T

	override fun <T, P0, P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, P12, P13, P14>
			invoke(p0: P0, p1: P1, p2: P2, p3: P3, p4: P4, p5: P5, p6: P6, p7: P7, p8: P8, p9: P9, p10: P10, p11: P11, p12: P12, p13: P13, p14: P14): T =
		method.invoke(p0, p1, p2, p3, p4, p5, p6, p7, p8, p9, p10, p11, p12, p13, p14) as T

	override fun <T, P0, P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, P12, P13, P14, P15>
			invoke(p0: P0, p1: P1, p2: P2, p3: P3, p4: P4, p5: P5, p6: P6, p7: P7, p8: P8, p9: P9, p10: P10, p11: P11, p12: P12, p13: P13, p14: P14, p15: P15): T =
		method.invoke(p0, p1, p2, p3, p4, p5, p6, p7, p8, p9, p10, p11, p12, p13, p14, p15) as T

	override fun <T, P0, P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, P12, P13, P14, P15, P16>
			invoke(p0: P0, p1: P1, p2: P2, p3: P3, p4: P4, p5: P5, p6: P6, p7: P7, p8: P8, p9: P9, p10: P10, p11: P11, p12: P12, p13: P13, p14: P14, p15: P15, p16: P16): T =
		method.invoke(p0, p1, p2, p3, p4, p5, p6, p7, p8, p9, p10, p11, p12, p13, p14, p15, p16) as T

	override fun <T, P0, P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, P12, P13, P14, P15, P16, P17>
			invoke(p0: P0, p1: P1, p2: P2, p3: P3, p4: P4, p5: P5, p6: P6, p7: P7, p8: P8, p9: P9, p10: P10, p11: P11, p12: P12, p13: P13, p14: P14, p15: P15, p16: P16, p17: P17): T =
		method.invoke(p0, p1, p2, p3, p4, p5, p6, p7, p8, p9, p10, p11, p12, p13, p14, p15, p16, p17) as T

	override fun <T, P0, P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, P12, P13, P14, P15, P16, P17, P18>
			invoke(p0: P0, p1: P1, p2: P2, p3: P3, p4: P4, p5: P5, p6: P6, p7: P7, p8: P8, p9: P9, p10: P10, p11: P11, p12: P12, p13: P13, p14: P14, p15: P15, p16: P16, p17: P17, p18: P18): T =
		method.invoke(p0, p1, p2, p3, p4, p5, p6, p7, p8, p9, p10, p11, p12, p13, p14, p15, p16, p17, p18) as T

	override fun <T, P0, P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, P12, P13, P14, P15, P16, P17, P18, P19>
			invoke(p0: P0, p1: P1, p2: P2, p3: P3, p4: P4, p5: P5, p6: P6, p7: P7, p8: P8, p9: P9, p10: P10, p11: P11, p12: P12, p13: P13, p14: P14, p15: P15, p16: P16, p17: P17, p18: P18, p19: P19): T =
		method.invoke(p0, p1, p2, p3, p4, p5, p6, p7, p8, p9, p10, p11, p12, p13, p14, p15, p16, p17, p18, p19) as T

	override fun <T, P0, P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, P12, P13, P14, P15, P16, P17, P18, P19, P20>
			invoke(p0: P0, p1: P1, p2: P2, p3: P3, p4: P4, p5: P5, p6: P6, p7: P7, p8: P8, p9: P9, p10: P10, p11: P11, p12: P12, p13: P13, p14: P14, p15: P15, p16: P16, p17: P17, p18: P18, p19: P19, p20: P20): T =
		method.invoke(p0, p1, p2, p3, p4, p5, p6, p7, p8, p9, p10, p11, p12, p13, p14, p15, p16, p17, p18, p19, p20) as T

	override fun <T, P0, P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, P12, P13, P14, P15, P16, P17, P18, P19, P20, P21>
			invoke(p0: P0, p1: P1, p2: P2, p3: P3, p4: P4, p5: P5, p6: P6, p7: P7, p8: P8, p9: P9, p10: P10, p11: P11, p12: P12, p13: P13, p14: P14, p15: P15, p16: P16, p17: P17, p18: P18, p19: P19, p20: P20, p21: P21): T =
		method.invoke(p0, p1, p2, p3, p4, p5, p6, p7, p8, p9, p10, p11, p12, p13, p14, p15, p16, p17, p18, p19, p20, p21) as T

	override fun <T, P0, P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, P12, P13, P14, P15, P16, P17, P18, P19, P20, P21, P22>
			invoke(p0: P0, p1: P1, p2: P2, p3: P3, p4: P4, p5: P5, p6: P6, p7: P7, p8: P8, p9: P9, p10: P10, p11: P11, p12: P12, p13: P13, p14: P14, p15: P15, p16: P16, p17: P17, p18: P18, p19: P19, p20: P20, p21: P21, p22: P22): T =
		method.invoke(p0, p1, p2, p3, p4, p5, p6, p7, p8, p9, p10, p11, p12, p13, p14, p15, p16, p17, p18, p19, p20, p21, p22) as T
}

internal class MethodHandleExactInvoker(method: MethodHandle) : MethodHandleInvoker(method) {
	override fun <T> invoke(): T = method.invokeExact() as T

	override fun <T, P0> invoke(p0: P0): T = method.invokeExact(p0) as T

	override fun <T, P0, P1> invoke(p0: P0, p1: P1): T = method.invokeExact(p0, p1) as T

	override fun <T, P0, P1, P2> invoke(p0: P0, p1: P1, p2: P2): T = method.invokeExact(p0, p1, p2) as T

	override fun <T, P0, P1, P2, P3> invoke(p0: P0, p1: P1, p2: P2, p3: P3): T =
		method.invokeExact(p0, p1, p2, p3) as T

	override fun <T, P0, P1, P2, P3, P4>
			invoke(p0: P0, p1: P1, p2: P2, p3: P3, p4: P4): T =
		method.invokeExact(p0, p1, p2, p3, p4) as T

	override fun <T, P0, P1, P2, P3, P4, P5>
			invoke(p0: P0, p1: P1, p2: P2, p3: P3, p4: P4, p5: P5): T =
		method.invokeExact(p0, p1, p2, p3, p4, p5) as T

	override fun <T, P0, P1, P2, P3, P4, P5, P6>
			invoke(p0: P0, p1: P1, p2: P2, p3: P3, p4: P4, p5: P5, p6: P6): T =
		method.invokeExact(p0, p1, p2, p3, p4, p5, p6) as T

	override fun <T, P0, P1, P2, P3, P4, P5, P6, P7>
			invoke(p0: P0, p1: P1, p2: P2, p3: P3, p4: P4, p5: P5, p6: P6, p7: P7): T =
		method.invokeExact(p0, p1, p2, p3, p4, p5, p6, p7) as T

	override fun <T, P0, P1, P2, P3, P4, P5, P6, P7, P8>
			invoke(p0: P0, p1: P1, p2: P2, p3: P3, p4: P4, p5: P5, p6: P6, p7: P7, p8: P8): T =
		method.invokeExact(p0, p1, p2, p3, p4, p5, p6, p7, p8) as T

	override fun <T, P0, P1, P2, P3, P4, P5, P6, P7, P8, P9>
			invoke(p0: P0, p1: P1, p2: P2, p3: P3, p4: P4, p5: P5, p6: P6, p7: P7, p8: P8, p9: P9): T =
		method.invokeExact(p0, p1, p2, p3, p4, p5, p6, p7, p8, p9) as T

	override fun <T, P0, P1, P2, P3, P4, P5, P6, P7, P8, P9, P10>
			invoke(p0: P0, p1: P1, p2: P2, p3: P3, p4: P4, p5: P5, p6: P6, p7: P7, p8: P8, p9: P9, p10: P10): T =
		method.invokeExact(p0, p1, p2, p3, p4, p5, p6, p7, p8, p9, p10) as T

	override fun <T, P0, P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11>
			invoke(p0: P0, p1: P1, p2: P2, p3: P3, p4: P4, p5: P5, p6: P6, p7: P7, p8: P8, p9: P9, p10: P10, p11: P11): T =
		method.invokeExact(p0, p1, p2, p3, p4, p5, p6, p7, p8, p9, p10, p11) as T

	override fun <T, P0, P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, P12>
			invoke(p0: P0, p1: P1, p2: P2, p3: P3, p4: P4, p5: P5, p6: P6, p7: P7, p8: P8, p9: P9, p10: P10, p11: P11, p12: P12): T =
		method.invokeExact(p0, p1, p2, p3, p4, p5, p6, p7, p8, p9, p10, p11, p12) as T

	override fun <T, P0, P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, P12, P13>
			invoke(p0: P0, p1: P1, p2: P2, p3: P3, p4: P4, p5: P5, p6: P6, p7: P7, p8: P8, p9: P9, p10: P10, p11: P11, p12: P12, p13: P13): T =
		method.invokeExact(p0, p1, p2, p3, p4, p5, p6, p7, p8, p9, p10, p11, p12, p13) as T

	override fun <T, P0, P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, P12, P13, P14>
			invoke(p0: P0, p1: P1, p2: P2, p3: P3, p4: P4, p5: P5, p6: P6, p7: P7, p8: P8, p9: P9, p10: P10, p11: P11, p12: P12, p13: P13, p14: P14): T =
		method.invokeExact(p0, p1, p2, p3, p4, p5, p6, p7, p8, p9, p10, p11, p12, p13, p14) as T

	override fun <T, P0, P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, P12, P13, P14, P15>
			invoke(p0: P0, p1: P1, p2: P2, p3: P3, p4: P4, p5: P5, p6: P6, p7: P7, p8: P8, p9: P9, p10: P10, p11: P11, p12: P12, p13: P13, p14: P14, p15: P15): T =
		method.invokeExact(p0, p1, p2, p3, p4, p5, p6, p7, p8, p9, p10, p11, p12, p13, p14, p15) as T

	override fun <T, P0, P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, P12, P13, P14, P15, P16>
			invoke(p0: P0, p1: P1, p2: P2, p3: P3, p4: P4, p5: P5, p6: P6, p7: P7, p8: P8, p9: P9, p10: P10, p11: P11, p12: P12, p13: P13, p14: P14, p15: P15, p16: P16): T =
		method.invokeExact(p0, p1, p2, p3, p4, p5, p6, p7, p8, p9, p10, p11, p12, p13, p14, p15, p16) as T

	override fun <T, P0, P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, P12, P13, P14, P15, P16, P17>
			invoke(p0: P0, p1: P1, p2: P2, p3: P3, p4: P4, p5: P5, p6: P6, p7: P7, p8: P8, p9: P9, p10: P10, p11: P11, p12: P12, p13: P13, p14: P14, p15: P15, p16: P16, p17: P17): T =
		method.invokeExact(p0, p1, p2, p3, p4, p5, p6, p7, p8, p9, p10, p11, p12, p13, p14, p15, p16, p17) as T

	override fun <T, P0, P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, P12, P13, P14, P15, P16, P17, P18>
			invoke(p0: P0, p1: P1, p2: P2, p3: P3, p4: P4, p5: P5, p6: P6, p7: P7, p8: P8, p9: P9, p10: P10, p11: P11, p12: P12, p13: P13, p14: P14, p15: P15, p16: P16, p17: P17, p18: P18): T =
		method.invokeExact(p0, p1, p2, p3, p4, p5, p6, p7, p8, p9, p10, p11, p12, p13, p14, p15, p16, p17, p18) as T

	override fun <T, P0, P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, P12, P13, P14, P15, P16, P17, P18, P19>
			invoke(p0: P0, p1: P1, p2: P2, p3: P3, p4: P4, p5: P5, p6: P6, p7: P7, p8: P8, p9: P9, p10: P10, p11: P11, p12: P12, p13: P13, p14: P14, p15: P15, p16: P16, p17: P17, p18: P18, p19: P19): T =
		method.invokeExact(p0, p1, p2, p3, p4, p5, p6, p7, p8, p9, p10, p11, p12, p13, p14, p15, p16, p17, p18, p19) as T

	override fun <T, P0, P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, P12, P13, P14, P15, P16, P17, P18, P19, P20>
			invoke(p0: P0, p1: P1, p2: P2, p3: P3, p4: P4, p5: P5, p6: P6, p7: P7, p8: P8, p9: P9, p10: P10, p11: P11, p12: P12, p13: P13, p14: P14, p15: P15, p16: P16, p17: P17, p18: P18, p19: P19, p20: P20): T =
		method.invokeExact(p0, p1, p2, p3, p4, p5, p6, p7, p8, p9, p10, p11, p12, p13, p14, p15, p16, p17, p18, p19, p20) as T

	override fun <T, P0, P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, P12, P13, P14, P15, P16, P17, P18, P19, P20, P21>
			invoke(p0: P0, p1: P1, p2: P2, p3: P3, p4: P4, p5: P5, p6: P6, p7: P7, p8: P8, p9: P9, p10: P10, p11: P11, p12: P12, p13: P13, p14: P14, p15: P15, p16: P16, p17: P17, p18: P18, p19: P19, p20: P20, p21: P21): T =
		method.invokeExact(p0, p1, p2, p3, p4, p5, p6, p7, p8, p9, p10, p11, p12, p13, p14, p15, p16, p17, p18, p19, p20, p21) as T

	override fun <T, P0, P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, P12, P13, P14, P15, P16, P17, P18, P19, P20, P21, P22>
			invoke(p0: P0, p1: P1, p2: P2, p3: P3, p4: P4, p5: P5, p6: P6, p7: P7, p8: P8, p9: P9, p10: P10, p11: P11, p12: P12, p13: P13, p14: P14, p15: P15, p16: P16, p17: P17, p18: P18, p19: P19, p20: P20, p21: P21, p22: P22): T =
		method.invokeExact(p0, p1, p2, p3, p4, p5, p6, p7, p8, p9, p10, p11, p12, p13, p14, p15, p16, p17, p18, p19, p20, p21, p22) as T
}