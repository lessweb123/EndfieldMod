package endfield.core

import arc.func.Func
import arc.util.Log
import endfield.util.invoke
import endfield.util.atomic.AtomicCharArray

object Test1 : Cloneable {
	@JvmStatic
	fun test() {
		val func = Func<CharArray, AtomicCharArray> { AtomicCharArray(it) }
		val array = func(CharArray(Character.SIZE))
		val e0 = array[0]
		array[0] = 'a'
		val len = array.length

		Log.info(array.toString())
	}

	fun copy(): Test1 = super.clone() as Test1
}