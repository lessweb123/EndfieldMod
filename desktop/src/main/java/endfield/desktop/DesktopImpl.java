package endfield.desktop;

import arc.util.Log;
import endfield.core.EndFieldMod;
import endfield.util.CollectionObjectMap;
import endfield.util.ConstructorAccessor;
import endfield.util.MockAccessibleHelper;
import endfield.util.MockClassHelper;
import endfield.util.ReflectionFieldAccessHelper;
import endfield.util.ReflectionMethodInvokeHelper;
import endfield.util.FieldAccessor;
import endfield.util.MethodAccessor;
import endfield.util.PlatformImpl;
import endfield.util.Reflects;
import endfield.util.aspector.accesses.PackageAccessHandler;
import endfield.util.aspector.classes.ClassAccessor;
import endfield.util.handler.ObjectHandler;
import sun.reflect.ReflectionFactory;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.invoke.MethodType;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Objects;
import java.util.function.Function;

import static endfield.Vars2.accessibleHelper;
import static endfield.Vars2.classHelper;
import static endfield.Vars2.fieldAccessHelper;
import static endfield.Vars2.methodInvokeHelper;
import static endfield.desktop.Unsafer.unsafe;
import static endfield.util.GetKt.sneakyThrow;

public class DesktopImpl implements PlatformImpl {
	static Lookup lookup;
	static Constructor<Lookup> lookupCtor;

	static final CollectionObjectMap<Class<?>, Lookup> lookupMap;
	static final Function<Class<?>, Lookup> lookupBuilder;

	static {
		try {
			lookup = (Lookup) ReflectionFactory.getReflectionFactory()
					.newConstructorForSerialization(Lookup.class, lookupCtor = Lookup.class.getDeclaredConstructor(Class.class, Class.class, int.class))
					.newInstance(EndFieldMod.class, null, -1);

			Demodulator.openModules();
			Demodulator.ensureFieldOpen();

			classHelper = new DesktopClassHelper();
			fieldAccessHelper = new UnsafeFieldAccessHelper();
			methodInvokeHelper = new MethodHandleMethodInvokeHelper();
			accessibleHelper = new DesktopAccessibleHelper();
		} catch (Throwable e) {
			Log.err("It seems you platform is special. (But don't worry)", e);

			lookup = Reflects.publicLookup;

			classHelper = new MockClassHelper();
			fieldAccessHelper = new ReflectionFieldAccessHelper();
			methodInvokeHelper = new ReflectionMethodInvokeHelper();
			accessibleHelper = new MockAccessibleHelper() {
				@Override
				public void makeAccessible(AccessibleObject object) {
					object.trySetAccessible();
				}
			};
		}

		lookupMap = new CollectionObjectMap<>(Class.class, Lookup.class);
		lookupBuilder = clazz -> methodInvokeHelper.newInstance(lookupCtor, clazz, null, 95);
	}

	@Override
	public Lookup lookup(Class<?> clazz) {
		return lookupMap.computeIfAbsent(clazz, lookupBuilder);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T clone(T object) {
		try {
			// If the object implements the Cloneable interface, call Object.clone() directly, which is faster than copyField().
			if (object instanceof Cloneable) {
				return (T) CloneHelper.clone.invokeExact(object);
			}

			Class<?> type = object.getClass();

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
		return UnsafeFieldAccessor.getUnsafeFieldAccessor(field);
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

	@Override
	public <T> Class<T> ensureInitialized(Class<T> targetClass) {
		unsafe.ensureClassInitialized(targetClass);
		return targetClass;
	}

	static final class CloneHelper {
		static final MethodHandle clone;

		static {
			try {
				clone = lookup.findVirtual(Object.class, "clone", MethodType.methodType(Object.class));
			} catch (NoSuchMethodException | IllegalAccessException e) {
				throw sneakyThrow(e);
			}
		}

		private CloneHelper() {}
	}
}
