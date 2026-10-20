package endfield.android;

import arc.util.Log;
import endfield.android.util.Methods;

import java.lang.reflect.Method;

import static endfield.android.Unsafer.unsafe;

@SuppressWarnings("removal")
public final class HiddenApi2 {
	public static long artMethodSize;

	public static long getArtMethodSize() {
		if (artMethodSize == 0) {
			try {
				artMethodSize = Methods.getArtMethod(NeverCall.class.getDeclaredMethod("b")) - Methods.getArtMethod(NeverCall.class.getDeclaredMethod("a"));
			} catch (NoSuchMethodException e) {
				throw new RuntimeException(e);
			}
		}
		return artMethodSize;
	}

	private HiddenApi2() {}

	public static void replaceMethod(Method targetMethod, Method hookMethod) {
		long targetArtMethodPtr = Methods.getArtMethod(targetMethod);
		long hookArtMethodPtr = Methods.getArtMethod(hookMethod);

		long entryPointOffset = getArtMethodSize() - 8;

		long hookEntryPoint = unsafe.getLong(hookArtMethodPtr + entryPointOffset);
		unsafe.putLong(targetArtMethodPtr + entryPointOffset, hookEntryPoint);
	}

	public static void test() {
		try {
			replaceMethod(R1.class.getDeclaredMethod("load"), R2.class.getDeclaredMethod("load"));

			R1.load();
			R2.load();
		} catch (Exception e) {
			Log.err(e);
		}
	}

	public static class NeverCall {
		private static int s;
		private static int t;
		private int i;
		private int j;

		private static void a() {}

		private static void b() {}
	}

	public static class R1 {
		public static void load() {
			Log.info("AA");
		}
	}

	public static class R2 {
		public static void load() {
			Log.info("BB");
		}
	}
}
