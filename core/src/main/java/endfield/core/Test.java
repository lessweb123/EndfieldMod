package endfield.core;

import arc.util.Log;
import endfield.util.HandleSupport;
import endfield.util.MethodHandleInvoker;
import endfield.util.MethodInvoker;
import endfield.util.SpecialMethodAccessor;
import endfield.util.aspector.AspectExtends;
import endfield.util.aspector.RuntimeAspector;
import endfield.util.aspector.RuntimeAspector.AspectDelegate.DeclDelegate;
import endfield.util.aspector.Shared;
import endfield.util.aspector.Stub;
import endfield.util.aspector.Using;
import endfield.util.aspector.accesses.PackageAccessHandler;
import endfield.util.aspector.classes.ASMClassAccessor;
import endfield.util.aspector.classes.BytecodeClassLoader;
import endfield.util.aspector.classes.ClassDecl;
import endfield.util.aspector.classes.ClassName;
import endfield.util.aspector.generate.ProxyAspectFactory;
import endfield.util.handler.ClassHandler;
import endfield.util.handler.EnumHandler;
import endfield.util.handler.MethodHandler;
import endfield.util.handler.ObjectHandler;
import kotlin.NotImplementedError;
import org.jetbrains.annotations.TestOnly;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.invoke.MethodType;
import java.lang.invoke.VarHandle.AccessMode;
import java.util.Arrays;
import java.util.Objects;
import java.util.Random;

/** Call the test on the console. */
@TestOnly
public class Test extends @Stub Random {
	public static final boolean debug = false;
	public static final String sstr = "";

	public final String str = "";

	public @TestAnno(
			text = "test text",
			type = Aspect.class,
			arrayTypes = {Aspect.class, AccessStub.class},
			arrayEnum = {Using.AFTER_RETURN, Using.OVERRIDE},
			intArray = {12, 25, 74, 1}
	) String test(String a) {
		return a;
	}

	public static void test1() {
		try {
			BytecodeClassLoader loader = new BytecodeClassLoader(RuntimeAspector.class.getClassLoader());

			Package pack = RuntimeAspector.withMaker(ProxyAspectFactory::new, PackageAccessHandler::packageAccessHandler, it -> {
				it.use(loader);

				DeclDelegate<?> aspectDecl = it.apply(it.open(ClassLoader.class), LoaderAspect.class);

				Aspect instance = (Aspect) aspectDecl.instance();
				return instance.definePackage(Object.class);
			});

			Log.info(pack);
		} catch (Throwable e) {
			Log.err(e);
		}
	}

	public static void test2() {
		try {
			ASMClassAccessor accessor = new ASMClassAccessor();

			ClassDecl<Test> classDecl = accessor.getClassDecl(ClassName.byClass(LoaderAspect.class));

			classDecl.superClass();
			classDecl.annotatedSuperClass();
			classDecl.interfaces();
			classDecl.annotatedInterfaces();
			classDecl.fields();
			classDecl.methods();
			classDecl.constructors();
			classDecl.annotations();
		} catch (Throwable e) {
			Log.err(e);
		}
	}

	public static void test3() {
		try {
			SpecialMethodAccessor accessor = new SpecialMethodAccessor(A.class.getDeclaredMethod("print"));
			accessor.invoke(new B());
			accessor.invoke(new C());
		} catch (Throwable e) {
			Log.err(e);
		}
	}

	public static void test4() {
		try {
			// Before using Unsafe to read and write fields, it is necessary to ensure that the class is initialized.
			EnumHandler<TestEnum> handler = EnumHandler.of(ClassHandler.ensureInitialized(TestEnum.class));

			handler.addEnumItemTail("D");
			Log.info(Arrays.toString(TestEnum.values()));
		} catch (Throwable e) {
			Log.err(e);
		}
	}

	public static void test5() {
		try {
			MethodInvoker i1 = MethodHandleInvoker.findStatic(Test.class, "print", MethodType.methodType(void.class));
			MethodInvoker i2 = MethodHandleInvoker.findVirtual(B.class, "print", MethodType.methodType(void.class));
			MethodInvoker i3 = MethodHandleInvoker.findSpecial(A.class, "print", MethodType.methodType(void.class));

			i1.invoke();
			i2.invoke(new B());
			i3.invoke(new C());
		} catch (Throwable e) {
			Log.err(e);
		}
	}

	public static void test6() {
		try {
			TestData source = new TestData(325, 799.9f, "arknights"), target = new TestData(-1, -1, "none");
			ObjectHandler.copyField(source, target);
			Log.info(target);
		} catch (Throwable e) {
			Log.err(e);
		}
	}

	public static void test7() {
		try {
			Log.info(HandleSupport.findStaticVarHandle(Test.class, "sstr", String.class).isAccessModeSupported(AccessMode.SET));
		} catch (Throwable e) {
			Log.err(e);
		}
	}

	public static void test8() {
		try {
			Class<?>[] matchParamTypes = {boolean.class, char.class, byte.class, short.class, int.class, long.class, float.class, double.class};
			MethodHandler.invokeStatic(Test.class, "print");
			Log.info(MethodHandler.<String>invokeStatic(Test.class, "match", false, '\u0000', (byte) 1, (short) 1, 1, 1l, 1f, 1d));
			Log.info(MethodHandler.<String>invokeStaticTyped(Test.class, "match", matchParamTypes, false, '\u0000', (byte) 1, (short) 1, 1, 1l, 1f, 1d));
		} catch (Exception e) {
			Log.err(e);
		}
	}

	public static void print() {
		Log.info("test print");
	}

	public static String match(boolean bool, char c, byte b, short s, int i, long l, float f, double d) {
		StringBuilder builder = new StringBuilder();
		return builder.append(bool).append(c).append(b).append(s).append(i).append(l).append(f).append(d).toString();
	}

	public static class A {
		void print() {
			Log.info("a");
		}
	}

	public static class B extends A {
		@Override
		void print() {
			Log.info("b");
		}
	}

	public static class C extends B {
		@Override
		void print() {
			Log.info("c");
		}
	}

	public enum TestEnum implements Cloneable {
		A, B, C
	}

	public static class TestData {
		public int intValue;
		public float floatValue;
		public String stringValue;

		public TestData(int i, float f, String s) {
			intValue = i;
			floatValue = f;
			stringValue = s;
		}

		public TestData() {}

		@Override
		public String toString() {
			return "TestData{intValue=" + intValue + ", floatValue=" + floatValue + ", stringValue=" + stringValue + "}";
		}

		@Override
		public boolean equals(Object obj) {
			return obj == this || obj instanceof TestData d && intValue == d.intValue && floatValue == d.floatValue && Objects.equals(stringValue, d.stringValue);
		}

		@Override
		public int hashCode() {
			return Objects.hash(intValue, floatValue, stringValue);
		}
	}

	@Target(ElementType.TYPE_USE)
	@Retention(RetentionPolicy.RUNTIME)
	public @interface TestAnno {
		String text();

		Class<?> type();

		Class<?>[] arrayTypes();

		Using[] arrayEnum();

		int[] intArray();
	}

	public interface Aspect {
		Package definePackage(Class<?> c);
	}

	public interface AccessStub {
		default Package definePackage(Class<?> c) {
			throw new NotImplementedError();
		}
	}

	public static class Base1 extends @Stub ClassLoader implements @Stub AccessStub, Aspect {
		@Shared
		public int called = 5;

		@Override
		public Package definePackage(Class<?> c) {
			Log.info("definePackage with base1: " + c);
			Log.info("called: " + called++);
			return AccessStub.super.definePackage(c);
		}
	}

	public static class Base2 extends @Stub ClassLoader implements @Stub AccessStub, Aspect {
		@Shared
		public int called = 5;

		@Override
		public Package definePackage(Class<?> c) {
			Log.info("definePackage with base2: " + c);
			Log.info("called: " + called++);
			return AccessStub.super.definePackage(c);
		}
	}

	public interface B1 {
		default Package definePackage(Class<?> c) {
			throw new NotImplementedError();
		}
	}

	public interface B2 {
		default Package definePackage(Class<?> c) {
			throw new NotImplementedError();
		}
	}

	@AspectExtends(extend = {Base1.class, Base2.class})
	public static class LoaderAspect extends @Stub ClassLoader implements @Stub AccessStub, @Stub(attacheTo = Base1.class) B1, @Stub(attacheTo = Base2.class) B2 {
		@Shared
		public int called = 5;

		@Override
		public Package definePackage(Class<?> c) {
			Log.info("Private method");

			Package pack = B1.super.definePackage(c);
			return B2.super.definePackage(c);
		}
	}
}
