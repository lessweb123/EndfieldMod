package endfield.android;

import arc.util.Log;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.invoke.VarHandle;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class AndroidInvokers2 extends AndroidInvokers {
	public static Method findFieldOfType;
	public static Method findAccessor;

	@Override
	public MethodHandle findGetter(Class<?> refc, String name, Class<?> type) {
		try {
			Lookup lookup = lookup(refc);
			Field field = (Field) findFieldOfType.invoke(lookup, refc, name, type);
			return (MethodHandle) findAccessor.invoke(lookup, field, refc, type, 8, false);
		} catch (Exception e) {
			return super.findGetter(refc, name, type);
		}
	}

	@Override
	public MethodHandle findSetter(Class<?> refc, String name, Class<?> type) {
		try {
			Lookup lookup = lookup(refc);
			Field field = (Field) findFieldOfType.invoke(lookup, refc, name, type);
			return (MethodHandle) findAccessor.invoke(lookup, field, refc, type, 9, false);
		} catch (Exception e) {
			return super.findSetter(refc, name, type);
		}
	}

	@Override
	public MethodHandle findStaticGetter(Class<?> refc, String name, Class<?> type) {
		try {
			Lookup lookup = lookup(refc);
			Field field = (Field) findFieldOfType.invoke(lookup, refc, name, type);
			return (MethodHandle) findAccessor.invoke(lookup, field, refc, type, 10, false);
		} catch (Exception e) {
			return super.findStaticGetter(refc, name, type);
		}
	}

	@Override
	public MethodHandle findStaticSetter(Class<?> refc, String name, Class<?> type) {
		try {
			Lookup lookup = lookup(refc);
			Field field = (Field) findFieldOfType.invoke(lookup, refc, name, type);
			return (MethodHandle) findAccessor.invoke(lookup, field, refc, type, 11, false);
		} catch (Exception e) {
			return super.findStaticSetter(refc, name, type);
		}
	}

	public static boolean init() {
		try {
			findFieldOfType = Lookup.class.getDeclaredMethod("findFieldOfType", Class.class, String.class, Class.class);
			findFieldOfType.setAccessible(true);
			findAccessor = Lookup.class.getDeclaredMethod("findAccessor", Field.class, Class.class, Class.class, int.class, boolean.class);
			findAccessor.setAccessible(true);

			HiddenApi2.replaceMethod(
					VarHandle.class.getDeclaredMethod("alignedAccessModesBitMask", Class.class, boolean.class),
					AndroidInvokers2.class.getDeclaredMethod("alignedAccessModesBitMask", Class.class, boolean.class)
			);

			return true;
		} catch (NoSuchMethodException e) {
			Log.err(e);
			return false;
		}
	}

	public static void test() {
		try {
			Method method = VarHandle.class.getDeclaredMethod("alignedAccessModesBitMask", Class.class, boolean.class);
			method.setAccessible(true);

			Log.info(Integer.toHexString((int) method.invoke(null, Object.class, false)));
			Log.info(Integer.toHexString(alignedAccessModesBitMask(Object.class, false)));
			Log.info(Integer.toHexString((int) method.invoke(null, Object.class, true)));
			Log.info(Integer.toHexString(alignedAccessModesBitMask(Object.class, true)));
		} catch (Exception e) {
			Log.err(e);
		}
	}

	public static int alignedAccessModesBitMask(Class<?> varType, boolean isFinal) {
		int bitMask = 2147483647;

		if (varType != byte.class && varType != short.class && varType != char.class &&
				varType != int.class && varType != long.class
				&& varType != float.class && varType != double.class) {
			bitMask &= ~3670016;
		}

		if (varType != boolean.class && varType != byte.class && varType != short.class &&
				varType != char.class && varType != int.class && varType != long.class) {
			bitMask &= ~2143289344;
		}
		return bitMask;
	}
}
