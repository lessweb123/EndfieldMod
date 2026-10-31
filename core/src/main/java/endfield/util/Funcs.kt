package endfield.util

import arc.func.Boolc
import arc.func.Boolf
import arc.func.Boolf2
import arc.func.Boolf3
import arc.func.Cons
import arc.func.Cons2
import arc.func.Cons3
import arc.func.Cons4
import arc.func.ConsT
import arc.func.FloatFloatf
import arc.func.Floatc
import arc.func.Floatc2
import arc.func.Floatc4
import arc.func.Floatf
import arc.func.Floatp
import arc.func.Func
import arc.func.Func2
import arc.func.Func3
import arc.func.IntIntf
import arc.func.Intc
import arc.func.Intc2
import arc.func.Intc4
import arc.func.Intf
import arc.func.Intp
import arc.func.Longf
import arc.func.Prov

operator fun Boolc.invoke(p: Boolean) = get(p)
operator fun <T> Boolf<T>.invoke(p: T): Boolean = get(p)
operator fun <P1, P2> Boolf2<P1, P2>.invoke(p1: P1, p2: P2): Boolean = get(p1, p2)
operator fun <P1, P2, P3> Boolf3<P1, P2, P3>.invoke(p1: P1, p2: P2, p3: P3): Boolean = get(p1, p2, p3)
operator fun <P> Cons<P>.invoke(p: P) = get(p)
operator fun <P1, P2> Cons2<P1, P2>.invoke(p1: P1, p2: P2) = get(p1, p2)
operator fun <P1, P2, P3> Cons3<P1, P2, P3>.invoke(p1: P1, p2: P2, p3: P3) = get(p1, p2, p3)
operator fun <P1, P2, P3, P4> Cons4<P1, P2, P3, P4>.invoke(p1: P1, p2: P2, p3: P3, p4: P4) = get(p1, p2, p3, p4)
operator fun <P, T : Throwable> ConsT<P, T>.invoke(p: P) = get(p)
operator fun Floatc.invoke(p: Float) = get(p)
operator fun Floatc2.invoke(x: Float, y: Float) = get(x, y)
operator fun Floatc4.invoke(x: Float, y: Float, x2: Float, y2: Float) = get(x, y, x2, y2)
operator fun <T> Floatf<T>.invoke(p: T): Float = get(p)
operator fun FloatFloatf.invoke(p: Float): Float = get(p)
operator fun Floatp.invoke(): Float = get()
operator fun <P, R> Func<P, R>.invoke(p: P): R = get(p)
operator fun <P1, P2, R> Func2<P1, P2, R>.invoke(p1: P1, p2: P2): R = get(p1, p2)
operator fun <P1, P2, P3, R> Func3<P1, P2, P3, R>.invoke(p1: P1, p2: P2, p3: P3): R = get(p1, p2, p3)
operator fun Intc.invoke(p: Int) = get(p)
operator fun Intc2.invoke(x: Int, y: Int) = get(x, y)
operator fun Intc4.invoke(x: Int, y: Int, x2: Int, y2: Int) = get(x, y, x2, y2)
operator fun <T> Intf<T>.invoke(p: T): Int = get(p)
operator fun IntIntf.invoke(p: Int): Int = get(p)
operator fun Intp.invoke(): Int = get()
operator fun <T> Longf<T>.invoke(p: T): Long = get(p)
operator fun <R> Prov<R>.invoke(): R = get()