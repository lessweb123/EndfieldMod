package endfield.desktop;

import endfield.core.EndFieldMod;
import endfield.util.ConstructorAccessor;
import endfield.util.FieldAccessor;
import endfield.util.MethodAccessor;
import endfield.util.MethodHandleConstructorAccessor;
import endfield.util.MethodHandleInvoker;
import endfield.util.MethodHandleStaticMethodAccessor;
import endfield.util.MethodHandleVirtualMethodAccessor;
import endfield.util.MethodInvoker;
import endfield.util.PlatformImpl;
import endfield.util.aspector.accesses.PackageAccessHandler;
import endfield.util.aspector.classes.ClassAccessor;
import endfield.util.handler.ObjectHandler;
import sun.reflect.ReflectionFactory;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.invoke.MethodType;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Objects;

import static endfield.Vars2.accessibleHelper;
import static endfield.Vars2.classHelper;
import static endfield.Vars2.fieldAccessHelper;
import static endfield.Vars2.imageHandle;
import static endfield.Vars2.invokers;
import static endfield.Vars2.methodInvokeHelper;
import static endfield.desktop.Unsafer.unsafe;
import static endfield.util.GetKt.sneakyThrow;

public class DesktopImpl implements PlatformImpl {
	static final Lookup lookup;

	static final MethodHandle cloneMethod;

	static {
		try {
			lookup = (Lookup) ReflectionFactory.getReflectionFactory()
					.newConstructorForSerialization(Lookup.class, Lookup.class.getDeclaredConstructor(Class.class, Class.class, int.class))
					.newInstance(EndFieldMod.class, null, -1);

			cloneMethod = lookup.findSpecial(Object.class, "clone", MethodType.methodType(Object.class), Object.class);

			Demodulator.openModules();
			Demodulator.ensureFieldOpen();
		} catch (Throwable e) {
			throw sneakyThrow(e);
		}
	}

	public DesktopImpl() {
		init();
	}

	void init() {
		invokers = DesktopInvokers2.init() ? new DesktopInvokers2() : new DesktopInvokers();
		classHelper = new DesktopClassHelper();
		fieldAccessHelper = new UnsafeFieldAccessHelper();
		methodInvokeHelper = new MethodHandleMethodInvokeHelper();
		accessibleHelper = new DesktopAccessibleHelper();

		imageHandle = new DesktopImageHandle();
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T clone(T object) {
		try {
			// If the object implements the Cloneable interface, call Object.clone() directly, which is faster than copyField().
			if (object instanceof Cloneable) {
				return (T) cloneMethod.invokeExact(object);
			}

			Class<?> type = object.getClass();
			// These types cannot be cloned
			if (type == Class.class || type == Field.class || type == Method.class || type == Constructor.class) return object;

			T result = (T) unsafe.allocateInstance(type);
			// The performance overhead may be high, but there is currently no other way.
			ObjectHandler.copyField(object, result);
			return result;
		} catch (Throwable e) {
			throw sneakyThrow(e);
		}
	}

	@Override
	public FieldAccessor fieldAccessor(Field field) {
		return (field.getModifiers() & Modifier.FINAL) == 0 ?
				VarHandleFieldAccessor.getVarHandleFieldAccessor(field) :
				UnsafeFieldAccessor.getUnsafeFieldAccessor(field);
	}

	@Override
	public MethodAccessor methodAccessor(Method method) {
		return (method.getModifiers() & Modifier.STATIC) != 0 ?
				new MethodHandleStaticMethodAccessor(method) :
				new MethodHandleVirtualMethodAccessor(method);
	}

	@Override
	public <T> ConstructorAccessor<T> constructorAccessor(Constructor<T> constructor) {
		return new MethodHandleConstructorAccessor<>(constructor);
	}

	@Override
	public MethodInvoker methodInvoker(Method method) {
		return MethodHandleInvoker.unreflect(method);
	}

	@Override
	public MethodInvoker methodInvoker(Constructor<?> constructor) {
		return MethodHandleInvoker.unreflectConstructor(constructor);
	}

	@Override
	public PackageAccessHandler packageAccessHandler(ClassAccessor accessor) {
		return new UnsafePackageAccessHandler(accessor);
	}

	@Override
	public void put(long srcAddress, long destAddress, long bytes) {
		unsafe.copyMemory(srcAddress, destAddress, bytes);
	}

	@Override
	public void put(Object src, int srcOffset, Object dst, int dstOffset, long bytes) {
		Objects.requireNonNull(src);
		Objects.requireNonNull(dst);

		unsafe.copyMemory(src, srcOffset, dst, dstOffset, bytes);
	}

	@Override
	public int arrayBaseOffset(Class<?> arrayClass) {
		return (int) unsafe.arrayBaseOffset(arrayClass);
	}

	@Override
	public int arrayIndexScale(Class<?> arrayClass) {
		return unsafe.arrayIndexScale(arrayClass);
	}
}
