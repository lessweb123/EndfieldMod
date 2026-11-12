package endfield.async

import arc.Core
import java.util.concurrent.Future
import java.util.concurrent.Semaphore
import java.util.concurrent.atomic.AtomicReference

fun <T> get(future: Future<out T>): T = future.get()

fun postWait(runSync: () -> Unit) {
	val flag = Semaphore(0)
	Core.app.post {
		try {
			runSync()
		} finally {
			flag.release()
		}
	}

	flag.acquire()
}

fun <T> postWait(runSync: () -> T): T {
	val flag = Semaphore(0)
	val out = AtomicReference<T>()
	Core.app.post {
		try {
			out.set(runSync())
		} finally {
			flag.release()
		}
	}

	flag.acquire()

	return out.get()
}
