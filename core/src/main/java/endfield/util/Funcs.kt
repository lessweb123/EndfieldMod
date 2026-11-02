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
import endfield.func.BoolBoolf
import endfield.func.BoolBoolf2
import endfield.func.ByteBytef
import endfield.func.CharCharf
import endfield.func.CharCharf2
import endfield.func.Charc
import endfield.func.Charp
import endfield.func.DoubleDoublef
import endfield.func.DoubleDoublef2
import endfield.func.Doublec
import endfield.func.Doublep
import endfield.func.FloatFloatf2
import endfield.func.Floatc3
import endfield.func.Floatf2
import endfield.func.Floatt
import endfield.func.Floatt2
import endfield.func.Floatt3
import endfield.func.FuncT
import endfield.func.IntBoolf
import endfield.func.IntIntf2
import endfield.func.Intf2
import endfield.func.Intt
import endfield.func.Intt2
import endfield.func.LongLongf
import endfield.func.Longp
import endfield.func.ProvT
import endfield.func.RunT
import endfield.func.Shortc

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

operator fun BoolBoolf.invoke(p: Boolean): Boolean = get(p)
operator fun BoolBoolf2.invoke(p1: Boolean, p2: Boolean): Boolean = get(p1, p2)
operator fun ByteBytef.invoke(p: Byte): Byte = get(p)
operator fun Charc.invoke(p: Char) = get(p)
operator fun CharCharf.invoke(p: Char): Char = get(p)
operator fun CharCharf2.invoke(p1: Char, p2: Char): Char = get(p1, p2)
operator fun Charp.invoke(): Char = get()
operator fun Doublec.invoke(p: Double) = get(p)
operator fun DoubleDoublef.invoke(p: Double): Double = get(p)
operator fun DoubleDoublef2.invoke(p1: Double, p2: Double): Double = get(p1, p2)
operator fun Doublep.invoke(): Double = get()
operator fun Floatc3.invoke(x: Float, y: Float, z: Float) = get(x, y, z)
operator fun <P1, P2> Floatf2<P1, P2>.invoke(p1: P1, p2: P2): Float = get(p1, p2)
operator fun FloatFloatf2.invoke(p1: Float, p2: Float): Float = get(p1, p2)
operator fun <R> Floatt<R>.invoke(p: Float): R = get(p)
operator fun <R> Floatt2<R>.invoke(p1: Float, p2: Float): R = get(p1, p2)
operator fun <R> Floatt3<R>.invoke(p1: Float, p2: Float, p3: Float): R = get(p1, p2, p3)
operator fun <P, R, T : Throwable> FuncT<P, R, T>.invoke(p: P): R = get(p)
operator fun IntBoolf.invoke(p: Int): Boolean = get(p)
operator fun <P1, P2> Intf2<P1, P2>.invoke(p1: P1, p2: P2): Int = get(p1, p2)
operator fun IntIntf2.invoke(p1: Int, p2: Int): Int = get(p1, p2)
operator fun <R> Intt<R>.invoke(p1: Int): R = get(p1)
operator fun <R> Intt2<R>.invoke(p1: Int, p2: Int): R = get(p1, p2)
operator fun LongLongf.invoke(p: Long): Long = get(p)
operator fun Longp.invoke(): Long = get()
operator fun <R, T : Throwable> ProvT<R, T>.invoke(): R = get()
operator fun <T : Throwable> RunT<T>.invoke() = run()
operator fun Shortc.invoke(p: Short) = get(p)
