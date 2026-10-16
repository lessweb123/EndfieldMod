package endfield.android;

import arc.util.Log;
import endfield.util.FieldAccessor;
import endfield.util.PlatformImpl;
import endfield.util.ReflectionMethodInvokeHelper;
import libcore.io.Memory;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Objects;

import static endfield.Vars2.accessibleHelper;
import static endfield.Vars2.classHelper;
import static endfield.Vars2.fieldAccessHelper;
import static endfield.Vars2.imageHandle;
import static endfield.Vars2.invokers;
import static endfield.Vars2.methodInvokeHelper;
import static endfield.android.Unsafer.unsafe;
import static endfield.util.GetKt.sneakyThrow;

@SuppressWarnings("removal")
public class AndroidImpl implements PlatformImpl {
	static {
		try {
			HiddenApi.setup();
		} catch (Throwable e) {
			Log.err(e);
		}
	}

	public AndroidImpl() {
		init();
	}

	void init() {
		invokers = new AndroidInvokers();
		accessibleHelper = new AndroidAccessibleHelper();
		classHelper = new AndroidClassHelper();
		fieldAccessHelper = new UnsafeFieldAccessHelper();
		methodInvokeHelper = new ReflectionMethodInvokeHelper();

		imageHandle = new AndroidImageHandle();
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T clone(T object) {
		Class<?> type = object.getClass();

		if (type == Class.class || type == Field.class || type == Method.class || type == Constructor.class) return object;

		try {
			return (T) Companion.cloneMethod.invoke(object);
		} catch (IllegalAccessException | InvocationTargetException e) {
			throw sneakyThrow(e);
		}
	}

	@Override
	public FieldAccessor fieldAccessor(Field field) {
		return UnsafeFieldAccessor.getUnsafeFieldAccessor(field);
	}

	@Override
	public void put(long srcAddress, long destAddress, long bytes) {
		unsafe.copyMemory(srcAddress, destAddress, bytes);
	}

	@Override
	public void put(Object src, int srcOffset, Object dst, int dstOffset, long bytes) {
		Objects.requireNonNull(src);
		Objects.requireNonNull(dst);

		Memory.memmove(dst, dstOffset, src, srcOffset, bytes);
	}

	@Override
	public int arrayBaseOffset(Class<?> arrayClass) {
		return unsafe.arrayBaseOffset(arrayClass);
	}

	@Override
	public int arrayIndexScale(Class<?> arrayClass) {
		return unsafe.arrayIndexScale(arrayClass);
	}

	public static final class Companion {
		static final Method cloneMethod;

		static {
			try {
				cloneMethod = Object.class.getDeclaredMethod("internalClone");
				cloneMethod.setAccessible(true);
			} catch (Throwable e) {
				throw sneakyThrow(e);
			}
		}

		private Companion() {}
	}
}
