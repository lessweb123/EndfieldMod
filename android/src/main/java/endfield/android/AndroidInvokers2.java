package endfield.android;

import arc.util.Log;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.invoke.MethodType;
import java.lang.invoke.VarHandle;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import static endfield.android.Unsafer.unsafe;

@SuppressWarnings("removal")
public class AndroidInvokers2 extends AndroidInvokers {
	public static Method findFieldOfType;

	public static Method fieldVarHandle;
	public static Method staticFieldVarHandle;

	public static Constructor<?> addressMethodHandleImpl;
	public static Constructor<?> fieldMethodHandleImpl;

	public static long varHandleMaskOffset;

	@Override
	public MethodHandle findGetter(Class<?> refc, String name, Class<?> type) {
		try {
			Lookup lookup = lookup(refc);
			Field field = (Field) findFieldOfType.invoke(lookup, refc, name, type);
			return (MethodHandle) fieldMethodHandleImpl.newInstance(field, 8, MethodType.methodType(type, refc));
		} catch (Exception e) {
			Log.err(e);
			return super.findGetter(refc, name, type);
		}
	}

	@Override
	public MethodHandle findSetter(Class<?> refc, String name, Class<?> type) {
		try {
			Lookup lookup = lookup(refc);
			Field field = (Field) findFieldOfType.invoke(lookup, refc, name, type);
			return (MethodHandle) fieldMethodHandleImpl.newInstance(field, 9, MethodType.methodType(void.class, refc, type));
		} catch (Exception e) {
			Log.err(e);
			return super.findSetter(refc, name, type);
		}
	}

	@Override
	public MethodHandle findStaticGetter(Class<?> refc, String name, Class<?> type) {
		try {
			Lookup lookup = lookup(refc);
			Field field = (Field) findFieldOfType.invoke(lookup, refc, name, type);
			return (MethodHandle) fieldMethodHandleImpl.newInstance(field, 10, MethodType.methodType(type));
		} catch (Exception e) {
			Log.err(e);
			return super.findStaticGetter(refc, name, type);
		}
	}

	@Override
	public MethodHandle findStaticSetter(Class<?> refc, String name, Class<?> type) {
		try {
			Lookup lookup = lookup(refc);
			Field field = (Field) findFieldOfType.invoke(lookup, refc, name, type);
			return (MethodHandle) fieldMethodHandleImpl.newInstance(field, 11, MethodType.methodType(void.class, type));
		} catch (Exception e) {
			Log.err(e);
			return super.findStaticSetter(refc, name, type);
		}
	}

	@Override
	public VarHandle findVarHandle(Class<?> recv, String name, Class<?> type) {
		try {
			Lookup lookup = lookup(recv);
			Field field = (Field) findFieldOfType.invoke(lookup, recv, name, type);

			if ((field.getModifiers() & Modifier.STATIC) != 0) throw new IllegalArgumentException("field is static");

			VarHandle varHandle = (VarHandle) fieldVarHandle.invoke(null, field);

			if ((field.getModifiers() & Modifier.FINAL) != 0) unsafe.putInt(varHandle, varHandleMaskOffset, alignedAccessModesBitMask(field.getType(), false));

			return varHandle;
		} catch (Exception e) {
			Log.err(e);
			return super.findVarHandle(recv, name, type);
		}
	}

	@Override
	public VarHandle findStaticVarHandle(Class<?> recv, String name, Class<?> type) {
		try {
			Lookup lookup = lookup(recv);
			Field field = (Field) findFieldOfType.invoke(lookup, recv, name, type);

			if ((field.getModifiers() & Modifier.STATIC) == 0) throw new IllegalArgumentException("field is not static");

			VarHandle varHandle = (VarHandle) staticFieldVarHandle.invoke(null, field);

			if ((field.getModifiers() & Modifier.FINAL) != 0) unsafe.putInt(varHandle, varHandleMaskOffset, alignedAccessModesBitMask(field.getType(), false));

			return varHandle;
		} catch (Exception e) {
			Log.err(e);
			return super.findStaticVarHandle(recv, name, type);
		}
	}

	@Override
	public MethodHandle unreflectGetter(Field field) {
		try {
			if ((field.getModifiers() & Modifier.STATIC) != 0) {
				return (MethodHandle) fieldMethodHandleImpl.newInstance(field, 10, MethodType.methodType(field.getType()));
			} else {
				return (MethodHandle) fieldMethodHandleImpl.newInstance(field, 8, MethodType.methodType(field.getType(), field.getDeclaringClass()));
			}
		} catch (Exception e) {
			Log.err(e);
			return super.unreflectGetter(field);
		}
	}

	@Override
	public MethodHandle unreflectSetter(Field field) {
		try {
			if ((field.getModifiers() & Modifier.STATIC) != 0) {
				return (MethodHandle) fieldMethodHandleImpl.newInstance(field, 11, MethodType.methodType(void.class, field.getType()));
			} else {
				return (MethodHandle) fieldMethodHandleImpl.newInstance(field, 9, MethodType.methodType(void.class, field.getDeclaringClass(), field.getType()));
			}
		} catch (Exception e) {
			Log.err(e);
			return super.unreflectSetter(field);
		}
	}

	@Override
	public VarHandle unreflectVarHandle(Field field) {
		try {
			VarHandle varHandle = (VarHandle) ((field.getModifiers() & Modifier.STATIC) != 0 ? staticFieldVarHandle : fieldVarHandle).invoke(null, field);

			if ((field.getModifiers() & Modifier.FINAL) != 0) unsafe.putInt(varHandle, varHandleMaskOffset, alignedAccessModesBitMask(field.getType(), false));

			return varHandle;
		} catch (Exception e) {
			Log.err(e);
			return super.unreflectVarHandle(field);
		}
	}

	static boolean init() {
		try {
			findFieldOfType = Lookup.class.getDeclaredMethod("findFieldOfType", Class.class, String.class, Class.class);
			findFieldOfType.setAccessible(true);

			Class<?> methodHandleImpl = Class.forName("java.lang.invoke.MethodHandleImpl", false, Object.class.getClassLoader());
			Class<?> fieldVarHandleClass = Class.forName("java.lang.invoke.FieldVarHandle", false, Object.class.getClassLoader());
			Class<?> staticFieldVarHandleClass = Class.forName("java.lang.invoke.StaticFieldVarHandle", false, Object.class.getClassLoader());

			addressMethodHandleImpl = methodHandleImpl.getDeclaredConstructor(long.class, int.class, MethodType.class);
			addressMethodHandleImpl.setAccessible(true);
			fieldMethodHandleImpl = methodHandleImpl.getDeclaredConstructor(Field.class, int.class, MethodType.class);
			fieldMethodHandleImpl.setAccessible(true);

			fieldVarHandle = fieldVarHandleClass.getDeclaredMethod("create", Field.class);
			fieldVarHandle.setAccessible(true);
			staticFieldVarHandle = staticFieldVarHandleClass.getDeclaredMethod("create", Field.class);
			staticFieldVarHandle.setAccessible(true);

			varHandleMaskOffset = unsafe.objectFieldOffset(VarHandle.class.getDeclaredField("accessModesBitMask"));

			return true;
		} catch (Exception e) {
			Log.err(e);
			return false;
		}
	}

	public static void test() {
		try {
			AndroidInvokers2 invokers = new AndroidInvokers2();

			Helper helper = new Helper();

			VarHandle sj = invokers.findStaticVarHandle(Helper.class, "sj", String.class);
			VarHandle j = invokers.findVarHandle(Helper.class, "j", String.class);

			sj.set("sj");
			j.set(helper, "j");

			Log.info((String) sj.get());
			Log.info((String) j.get(helper));
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

	public static class Helper implements Cloneable {
		public static final String sj = "";

		public final String j = "";
	}
}
