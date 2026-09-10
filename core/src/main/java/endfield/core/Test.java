package endfield.core;

import arc.util.Log;
import endfield.util.aspector.AspectExtends;
import endfield.util.aspector.RuntimeAspector;
import endfield.util.aspector.RuntimeAspector.AspectDelegate.DeclDelegate;
import endfield.util.aspector.Shared;
import endfield.util.aspector.Stub;
import endfield.util.aspector.Using;
import endfield.util.aspector.classes.ASMClassAccessor;
import endfield.util.aspector.classes.BytecodeClassLoader;
import endfield.util.aspector.classes.ClassDecl;
import endfield.util.aspector.classes.ClassName;
import endfield.util.aspector.generate.ProxyAspectFactory;
import kotlin.NotImplementedError;
import org.jetbrains.annotations.TestOnly;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Random;

import static endfield.Vars2.platformImpl;

@TestOnly
public class Test extends @Stub Random {
	public @TestAnno(
			str = "test text",
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

			Package pack = RuntimeAspector.withMaker(it -> new ProxyAspectFactory(it), it -> platformImpl.packageAccessHandler(it), it -> {
				it.use(loader);

				DeclDelegate<?> aspectDecl = it.apply(it.open(ClassLoader.class), LoaderAspect.class);

				Aspect instance = (Aspect) aspectDecl.instance();
				return instance.definePackage(Object.class);
			});
		} catch (Throwable e) {
			Log.err(e);
		}
	}

	public static void test2() {
		try {
			ASMClassAccessor accessor = new ASMClassAccessor();

			ClassDecl<Test> classDecl = accessor.getClassDecl(ClassName.byClass(LoaderAspect.class));
			classDecl.annotatedSuperClass();
		} catch (Throwable e) {
			Log.err(e);
		}
	}

	@Target(ElementType.TYPE_USE)
	@Retention(RetentionPolicy.RUNTIME)
	public @interface TestAnno {
		String str();

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
