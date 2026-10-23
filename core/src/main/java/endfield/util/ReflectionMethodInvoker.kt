@file:Suppress("UNCHECKED_CAST")

package endfield.util

import java.lang.invoke.WrongMethodTypeException
import java.lang.reflect.Constructor
import java.lang.reflect.Method

abstract class ReflectionMethodInvoker(@JvmField val method: Method) : MethodInvoker

/**
 * Wrap the [Method] or [Constructor], no need to forcefully handle thrown exceptions, automatically convert types.
 *
 * @since 1.0.10
 */
class ReflectionVirtualMethodInvoker(method: Method) : ReflectionMethodInvoker(method) {
	override fun <T> invoke(): T {
		// The virtual method must have an object parameter
		throw WrongMethodTypeException("wrong number of arguments")
	}

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

class ReflectionStaticMethodInvoker(method: Method) : ReflectionMethodInvoker(method) {
	override fun <T> invoke(): T = method.invoke(null) as T

	override fun <T, P0> invoke(p0: P0): T = method.invoke(null, p0) as T

	override fun <T, P0, P1> invoke(p0: P0, p1: P1): T = method.invoke(null, p0, p1) as T

	override fun <T, P0, P1, P2> invoke(p0: P0, p1: P1, p2: P2): T = method.invoke(null, p0, p1, p2) as T

	override fun <T, P0, P1, P2, P3> invoke(p0: P0, p1: P1, p2: P2, p3: P3): T =
		method.invoke(null, p0, p1, p2, p3) as T

	override fun <T, P0, P1, P2, P3, P4>
			invoke(p0: P0, p1: P1, p2: P2, p3: P3, p4: P4): T =
		method.invoke(null, p0, p1, p2, p3, p4) as T

	override fun <T, P0, P1, P2, P3, P4, P5>
			invoke(p0: P0, p1: P1, p2: P2, p3: P3, p4: P4, p5: P5): T =
		method.invoke(null, p0, p1, p2, p3, p4, p5) as T

	override fun <T, P0, P1, P2, P3, P4, P5, P6>
			invoke(p0: P0, p1: P1, p2: P2, p3: P3, p4: P4, p5: P5, p6: P6): T =
		method.invoke(null, p0, p1, p2, p3, p4, p5, p6) as T

	override fun <T, P0, P1, P2, P3, P4, P5, P6, P7>
			invoke(p0: P0, p1: P1, p2: P2, p3: P3, p4: P4, p5: P5, p6: P6, p7: P7): T =
		method.invoke(null, p0, p1, p2, p3, p4, p5, p6, p7) as T

	override fun <T, P0, P1, P2, P3, P4, P5, P6, P7, P8>
			invoke(p0: P0, p1: P1, p2: P2, p3: P3, p4: P4, p5: P5, p6: P6, p7: P7, p8: P8): T =
		method.invoke(null, p0, p1, p2, p3, p4, p5, p6, p7, p8) as T

	override fun <T, P0, P1, P2, P3, P4, P5, P6, P7, P8, P9>
			invoke(p0: P0, p1: P1, p2: P2, p3: P3, p4: P4, p5: P5, p6: P6, p7: P7, p8: P8, p9: P9): T =
		method.invoke(null, p0, p1, p2, p3, p4, p5, p6, p7, p8, p9) as T

	override fun <T, P0, P1, P2, P3, P4, P5, P6, P7, P8, P9, P10>
			invoke(p0: P0, p1: P1, p2: P2, p3: P3, p4: P4, p5: P5, p6: P6, p7: P7, p8: P8, p9: P9, p10: P10): T =
		method.invoke(null, p0, p1, p2, p3, p4, p5, p6, p7, p8, p9, p10) as T

	override fun <T, P0, P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11>
			invoke(p0: P0, p1: P1, p2: P2, p3: P3, p4: P4, p5: P5, p6: P6, p7: P7, p8: P8, p9: P9, p10: P10, p11: P11): T =
		method.invoke(null, p0, p1, p2, p3, p4, p5, p6, p7, p8, p9, p10, p11) as T

	override fun <T, P0, P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, P12>
			invoke(p0: P0, p1: P1, p2: P2, p3: P3, p4: P4, p5: P5, p6: P6, p7: P7, p8: P8, p9: P9, p10: P10, p11: P11, p12: P12): T =
		method.invoke(null, p0, p1, p2, p3, p4, p5, p6, p7, p8, p9, p10, p11, p12) as T

	override fun <T, P0, P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, P12, P13>
			invoke(p0: P0, p1: P1, p2: P2, p3: P3, p4: P4, p5: P5, p6: P6, p7: P7, p8: P8, p9: P9, p10: P10, p11: P11, p12: P12, p13: P13): T =
		method.invoke(null, p0, p1, p2, p3, p4, p5, p6, p7, p8, p9, p10, p11, p12, p13) as T

	override fun <T, P0, P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, P12, P13, P14>
			invoke(p0: P0, p1: P1, p2: P2, p3: P3, p4: P4, p5: P5, p6: P6, p7: P7, p8: P8, p9: P9, p10: P10, p11: P11, p12: P12, p13: P13, p14: P14): T =
		method.invoke(null, p0, p1, p2, p3, p4, p5, p6, p7, p8, p9, p10, p11, p12, p13, p14) as T

	override fun <T, P0, P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, P12, P13, P14, P15>
			invoke(p0: P0, p1: P1, p2: P2, p3: P3, p4: P4, p5: P5, p6: P6, p7: P7, p8: P8, p9: P9, p10: P10, p11: P11, p12: P12, p13: P13, p14: P14, p15: P15): T =
		method.invoke(null, p0, p1, p2, p3, p4, p5, p6, p7, p8, p9, p10, p11, p12, p13, p14, p15) as T

	override fun <T, P0, P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, P12, P13, P14, P15, P16>
			invoke(p0: P0, p1: P1, p2: P2, p3: P3, p4: P4, p5: P5, p6: P6, p7: P7, p8: P8, p9: P9, p10: P10, p11: P11, p12: P12, p13: P13, p14: P14, p15: P15, p16: P16): T =
		method.invoke(null, p0, p1, p2, p3, p4, p5, p6, p7, p8, p9, p10, p11, p12, p13, p14, p15, p16) as T

	override fun <T, P0, P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, P12, P13, P14, P15, P16, P17>
			invoke(p0: P0, p1: P1, p2: P2, p3: P3, p4: P4, p5: P5, p6: P6, p7: P7, p8: P8, p9: P9, p10: P10, p11: P11, p12: P12, p13: P13, p14: P14, p15: P15, p16: P16, p17: P17): T =
		method.invoke(null, p0, p1, p2, p3, p4, p5, p6, p7, p8, p9, p10, p11, p12, p13, p14, p15, p16, p17) as T

	override fun <T, P0, P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, P12, P13, P14, P15, P16, P17, P18>
			invoke(p0: P0, p1: P1, p2: P2, p3: P3, p4: P4, p5: P5, p6: P6, p7: P7, p8: P8, p9: P9, p10: P10, p11: P11, p12: P12, p13: P13, p14: P14, p15: P15, p16: P16, p17: P17, p18: P18): T =
		method.invoke(null, p0, p1, p2, p3, p4, p5, p6, p7, p8, p9, p10, p11, p12, p13, p14, p15, p16, p17, p18) as T

	override fun <T, P0, P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, P12, P13, P14, P15, P16, P17, P18, P19>
			invoke(p0: P0, p1: P1, p2: P2, p3: P3, p4: P4, p5: P5, p6: P6, p7: P7, p8: P8, p9: P9, p10: P10, p11: P11, p12: P12, p13: P13, p14: P14, p15: P15, p16: P16, p17: P17, p18: P18, p19: P19): T =
		method.invoke(null, p0, p1, p2, p3, p4, p5, p6, p7, p8, p9, p10, p11, p12, p13, p14, p15, p16, p17, p18, p19) as T

	override fun <T, P0, P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, P12, P13, P14, P15, P16, P17, P18, P19, P20>
			invoke(p0: P0, p1: P1, p2: P2, p3: P3, p4: P4, p5: P5, p6: P6, p7: P7, p8: P8, p9: P9, p10: P10, p11: P11, p12: P12, p13: P13, p14: P14, p15: P15, p16: P16, p17: P17, p18: P18, p19: P19, p20: P20): T =
		method.invoke(null, p0, p1, p2, p3, p4, p5, p6, p7, p8, p9, p10, p11, p12, p13, p14, p15, p16, p17, p18, p19, p20) as T

	override fun <T, P0, P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, P12, P13, P14, P15, P16, P17, P18, P19, P20, P21>
			invoke(p0: P0, p1: P1, p2: P2, p3: P3, p4: P4, p5: P5, p6: P6, p7: P7, p8: P8, p9: P9, p10: P10, p11: P11, p12: P12, p13: P13, p14: P14, p15: P15, p16: P16, p17: P17, p18: P18, p19: P19, p20: P20, p21: P21): T =
		method.invoke(null, p0, p1, p2, p3, p4, p5, p6, p7, p8, p9, p10, p11, p12, p13, p14, p15, p16, p17, p18, p19, p20, p21) as T

	override fun <T, P0, P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, P12, P13, P14, P15, P16, P17, P18, P19, P20, P21, P22>
			invoke(p0: P0, p1: P1, p2: P2, p3: P3, p4: P4, p5: P5, p6: P6, p7: P7, p8: P8, p9: P9, p10: P10, p11: P11, p12: P12, p13: P13, p14: P14, p15: P15, p16: P16, p17: P17, p18: P18, p19: P19, p20: P20, p21: P21, p22: P22): T =
		method.invoke(null, p0, p1, p2, p3, p4, p5, p6, p7, p8, p9, p10, p11, p12, p13, p14, p15, p16, p17, p18, p19, p20, p21, p22) as T
}

open class ReflectionConstructorInvoker(@JvmField val constructor: Constructor<*>) : MethodInvoker {
	override fun <T> invoke(): T = constructor.newInstance() as T

	override fun <T, P0> invoke(p0: P0): T = constructor.newInstance(p0) as T

	override fun <T, P0, P1> invoke(p0: P0, p1: P1): T = constructor.newInstance(p0, p1) as T

	override fun <T, P0, P1, P2> invoke(p0: P0, p1: P1, p2: P2): T = constructor.newInstance(p0, p1, p2) as T

	override fun <T, P0, P1, P2, P3> invoke(p0: P0, p1: P1, p2: P2, p3: P3): T =
		constructor.newInstance(p0, p1, p2, p3) as T

	override fun <T, P0, P1, P2, P3, P4>
			invoke(p0: P0, p1: P1, p2: P2, p3: P3, p4: P4): T =
		constructor.newInstance(p0, p1, p2, p3, p4) as T

	override fun <T, P0, P1, P2, P3, P4, P5>
			invoke(p0: P0, p1: P1, p2: P2, p3: P3, p4: P4, p5: P5): T =
		constructor.newInstance(p0, p1, p2, p3, p4, p5) as T

	override fun <T, P0, P1, P2, P3, P4, P5, P6>
			invoke(p0: P0, p1: P1, p2: P2, p3: P3, p4: P4, p5: P5, p6: P6): T =
		constructor.newInstance(p0, p1, p2, p3, p4, p5, p6) as T

	override fun <T, P0, P1, P2, P3, P4, P5, P6, P7>
			invoke(p0: P0, p1: P1, p2: P2, p3: P3, p4: P4, p5: P5, p6: P6, p7: P7): T =
		constructor.newInstance(p0, p1, p2, p3, p4, p5, p6, p7) as T

	override fun <T, P0, P1, P2, P3, P4, P5, P6, P7, P8>
			invoke(p0: P0, p1: P1, p2: P2, p3: P3, p4: P4, p5: P5, p6: P6, p7: P7, p8: P8): T =
		constructor.newInstance(p0, p1, p2, p3, p4, p5, p6, p7, p8) as T

	override fun <T, P0, P1, P2, P3, P4, P5, P6, P7, P8, P9>
			invoke(p0: P0, p1: P1, p2: P2, p3: P3, p4: P4, p5: P5, p6: P6, p7: P7, p8: P8, p9: P9): T =
		constructor.newInstance(p0, p1, p2, p3, p4, p5, p6, p7, p8, p9) as T

	override fun <T, P0, P1, P2, P3, P4, P5, P6, P7, P8, P9, P10>
			invoke(p0: P0, p1: P1, p2: P2, p3: P3, p4: P4, p5: P5, p6: P6, p7: P7, p8: P8, p9: P9, p10: P10): T =
		constructor.newInstance(p0, p1, p2, p3, p4, p5, p6, p7, p8, p9, p10) as T

	override fun <T, P0, P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11>
			invoke(p0: P0, p1: P1, p2: P2, p3: P3, p4: P4, p5: P5, p6: P6, p7: P7, p8: P8, p9: P9, p10: P10, p11: P11): T =
		constructor.newInstance(p0, p1, p2, p3, p4, p5, p6, p7, p8, p9, p10, p11) as T

	override fun <T, P0, P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, P12>
			invoke(p0: P0, p1: P1, p2: P2, p3: P3, p4: P4, p5: P5, p6: P6, p7: P7, p8: P8, p9: P9, p10: P10, p11: P11, p12: P12): T =
		constructor.newInstance(p0, p1, p2, p3, p4, p5, p6, p7, p8, p9, p10, p11, p12) as T

	override fun <T, P0, P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, P12, P13>
			invoke(p0: P0, p1: P1, p2: P2, p3: P3, p4: P4, p5: P5, p6: P6, p7: P7, p8: P8, p9: P9, p10: P10, p11: P11, p12: P12, p13: P13): T =
		constructor.newInstance(p0, p1, p2, p3, p4, p5, p6, p7, p8, p9, p10, p11, p12, p13) as T

	override fun <T, P0, P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, P12, P13, P14>
			invoke(p0: P0, p1: P1, p2: P2, p3: P3, p4: P4, p5: P5, p6: P6, p7: P7, p8: P8, p9: P9, p10: P10, p11: P11, p12: P12, p13: P13, p14: P14): T =
		constructor.newInstance(p0, p1, p2, p3, p4, p5, p6, p7, p8, p9, p10, p11, p12, p13, p14) as T

	override fun <T, P0, P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, P12, P13, P14, P15>
			invoke(p0: P0, p1: P1, p2: P2, p3: P3, p4: P4, p5: P5, p6: P6, p7: P7, p8: P8, p9: P9, p10: P10, p11: P11, p12: P12, p13: P13, p14: P14, p15: P15): T =
		constructor.newInstance(p0, p1, p2, p3, p4, p5, p6, p7, p8, p9, p10, p11, p12, p13, p14, p15) as T

	override fun <T, P0, P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, P12, P13, P14, P15, P16>
			invoke(p0: P0, p1: P1, p2: P2, p3: P3, p4: P4, p5: P5, p6: P6, p7: P7, p8: P8, p9: P9, p10: P10, p11: P11, p12: P12, p13: P13, p14: P14, p15: P15, p16: P16): T =
		constructor.newInstance(p0, p1, p2, p3, p4, p5, p6, p7, p8, p9, p10, p11, p12, p13, p14, p15, p16) as T

	override fun <T, P0, P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, P12, P13, P14, P15, P16, P17>
			invoke(p0: P0, p1: P1, p2: P2, p3: P3, p4: P4, p5: P5, p6: P6, p7: P7, p8: P8, p9: P9, p10: P10, p11: P11, p12: P12, p13: P13, p14: P14, p15: P15, p16: P16, p17: P17): T =
		constructor.newInstance(p0, p1, p2, p3, p4, p5, p6, p7, p8, p9, p10, p11, p12, p13, p14, p15, p16, p17) as T

	override fun <T, P0, P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, P12, P13, P14, P15, P16, P17, P18>
			invoke(p0: P0, p1: P1, p2: P2, p3: P3, p4: P4, p5: P5, p6: P6, p7: P7, p8: P8, p9: P9, p10: P10, p11: P11, p12: P12, p13: P13, p14: P14, p15: P15, p16: P16, p17: P17, p18: P18): T =
		constructor.newInstance(p0, p1, p2, p3, p4, p5, p6, p7, p8, p9, p10, p11, p12, p13, p14, p15, p16, p17, p18) as T

	override fun <T, P0, P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, P12, P13, P14, P15, P16, P17, P18, P19>
			invoke(p0: P0, p1: P1, p2: P2, p3: P3, p4: P4, p5: P5, p6: P6, p7: P7, p8: P8, p9: P9, p10: P10, p11: P11, p12: P12, p13: P13, p14: P14, p15: P15, p16: P16, p17: P17, p18: P18, p19: P19): T =
		constructor.newInstance(p0, p1, p2, p3, p4, p5, p6, p7, p8, p9, p10, p11, p12, p13, p14, p15, p16, p17, p18, p19) as T

	override fun <T, P0, P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, P12, P13, P14, P15, P16, P17, P18, P19, P20>
			invoke(p0: P0, p1: P1, p2: P2, p3: P3, p4: P4, p5: P5, p6: P6, p7: P7, p8: P8, p9: P9, p10: P10, p11: P11, p12: P12, p13: P13, p14: P14, p15: P15, p16: P16, p17: P17, p18: P18, p19: P19, p20: P20): T =
		constructor.newInstance(p0, p1, p2, p3, p4, p5, p6, p7, p8, p9, p10, p11, p12, p13, p14, p15, p16, p17, p18, p19, p20) as T

	override fun <T, P0, P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, P12, P13, P14, P15, P16, P17, P18, P19, P20, P21>
			invoke(p0: P0, p1: P1, p2: P2, p3: P3, p4: P4, p5: P5, p6: P6, p7: P7, p8: P8, p9: P9, p10: P10, p11: P11, p12: P12, p13: P13, p14: P14, p15: P15, p16: P16, p17: P17, p18: P18, p19: P19, p20: P20, p21: P21): T =
		constructor.newInstance(p0, p1, p2, p3, p4, p5, p6, p7, p8, p9, p10, p11, p12, p13, p14, p15, p16, p17, p18, p19, p20, p21) as T

	override fun <T, P0, P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, P12, P13, P14, P15, P16, P17, P18, P19, P20, P21, P22>
			invoke(p0: P0, p1: P1, p2: P2, p3: P3, p4: P4, p5: P5, p6: P6, p7: P7, p8: P8, p9: P9, p10: P10, p11: P11, p12: P12, p13: P13, p14: P14, p15: P15, p16: P16, p17: P17, p18: P18, p19: P19, p20: P20, p21: P21, p22: P22): T =
		constructor.newInstance(p0, p1, p2, p3, p4, p5, p6, p7, p8, p9, p10, p11, p12, p13, p14, p15, p16, p17, p18, p19, p20, p21, p22) as T
}
