package endfield.android;

import arc.util.Log;
import org.jetbrains.annotations.TestOnly;

import java.lang.invoke.VarHandle;
import java.lang.invoke.VarHandle.AccessMode;
import java.lang.reflect.Method;

import static endfield.android.AndroidInvokers2.alignedAccessModesBitMask;

@TestOnly
public class Test3 {
	public static final String sj = "";

	public final String j = "";

	public static void test() {
		try {
			AndroidInvokers2 invokers = new AndroidInvokers2();

			Test3 helper = new Test3();

			VarHandle sj = invokers.findStaticVarHandle(Test3.class, "sj", String.class);
			VarHandle j = invokers.findVarHandle(Test3.class, "j", String.class);

			Log.info(sj.isAccessModeSupported(AccessMode.SET));
			Log.info(j.isAccessModeSupported(AccessMode.SET));
		} catch (Exception e) {
			Log.err(e);
		}
	}

	public static void test2() {
		try {
			Method alignedMask = VarHandle.class.getDeclaredMethod("alignedAccessModesBitMask", Class.class, boolean.class);
			alignedMask.setAccessible(true);

			for (Class<?> type : new Class<?>[]{boolean.class, char.class, byte.class, short.class, int.class, long.class, float.class, double.class, Object.class, String.class, Class.class}) {
				Log.info("alignedAccessModesBitMask: @ - @ @", type, Integer.toHexString((int) alignedMask.invoke(null, type, false)), Integer.toHexString(alignedAccessModesBitMask(type, false)));
			}
		} catch (Exception e) {
			Log.err(e);
		}
	}
}
