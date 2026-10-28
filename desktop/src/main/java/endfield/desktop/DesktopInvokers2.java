package endfield.desktop;

import arc.util.Log;
import endfield.core.Test;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.invoke.VarHandle;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import static endfield.desktop.DesktopImpl.lookup;

public class DesktopInvokers2 extends DesktopInvokers {
	static Method resolveOrFail;
	static Constructor<?> fieldMemberName;

	static Method makeFieldHandle;
	static Method makeRefc;

	@SuppressWarnings("unused")
	@Override
	public VarHandle findVarHandle(Class<?> recv, String name, Class<?> type) {
		try {
			Object getField = resolveOrFail.invoke(lookup, (byte) 1, recv, name, type);
			Object putField = resolveOrFail.invoke(lookup, (byte) 3, recv, name, type);
			return (VarHandle) makeFieldHandle.invoke(null, getField, recv, true);
		} catch (Exception e) {
			Log.err(e);
			return super.findVarHandle(recv, name, type);
		}
	}

	@SuppressWarnings("unused")
	@Override
	public VarHandle findStaticVarHandle(Class<?> recv, String name, Class<?> type) {
		try {
			Object getField = resolveOrFail.invoke(lookup, (byte) 2, recv, name, type);
			Object putField = resolveOrFail.invoke(lookup, (byte) 4, recv, name, type);
			return (VarHandle) makeFieldHandle.invoke(null, getField, recv, true);
		} catch (Exception e) {
			Log.err(e);
			return super.findVarHandle(recv, name, type);
		}
	}

	@Override
	public MethodHandle unreflectGetter(Field field) {
		try {
			Object putField = fieldMemberName.newInstance(field, false);
			return (MethodHandle) makeRefc.invoke(null, field.getDeclaringClass(), putField);
		} catch (Exception e) {
			Log.err(e);
			return super.unreflectGetter(field);
		}
	}

	@Override
	public MethodHandle unreflectSetter(Field field) {
		try {
			Object putField = fieldMemberName.newInstance(field, true);
			return (MethodHandle) makeRefc.invoke(null, field.getDeclaringClass(), putField);
		} catch (Exception e) {
			Log.err(e);
			return super.unreflectSetter(field);
		}
	}

	@SuppressWarnings("unused")
	@Override
	public VarHandle unreflectVarHandle(Field field) {
		try {
			Object getField = fieldMemberName.newInstance(field, false);
			Object putField = fieldMemberName.newInstance(field, true);
			return (VarHandle) makeFieldHandle.invoke(null, getField, field.getDeclaringClass(), true);
		} catch (Exception e) {
			Log.err(e);
			return super.unreflectVarHandle(field);
		}
	}

	static boolean init() {
		try {
			Class<?> memberNameClass = Class.forName("java.lang.invoke.MemberName", false, null);
			Class<?> varHandlesClass = Class.forName("java.lang.invoke.VarHandles", false, null);
			Class<?> directMethodHandleClass = Class.forName("java.lang.invoke.DirectMethodHandle", false, null);

			resolveOrFail = Lookup.class.getDeclaredMethod("resolveOrFail", byte.class, Class.class, String.class, Class.class);
			resolveOrFail.setAccessible(true);
			fieldMemberName = memberNameClass.getDeclaredConstructor(Field.class, boolean.class);
			fieldMemberName.setAccessible(true);
			makeFieldHandle = varHandlesClass.getDeclaredMethod("makeFieldHandle", memberNameClass, Class.class, boolean.class);
			makeFieldHandle.setAccessible(true);
			makeRefc = directMethodHandleClass.getDeclaredMethod("make", Class.class, memberNameClass);
			makeRefc.setAccessible(true);

			return true;
		} catch (Throwable e) {
			Log.err(e);
			return false;
		}
	}
}
