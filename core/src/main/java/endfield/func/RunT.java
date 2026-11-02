package endfield.func;

import arc.util.UnsafeRunnable;

/** A run that throws something. */
@FunctionalInterface
public interface RunT<T extends Throwable> extends UnsafeRunnable {
	@Override
	void run() throws T;
}
