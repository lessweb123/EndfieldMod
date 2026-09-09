package endfield.util.aspector;

import arc.func.Func;
import endfield.util.aspector.accesses.PackageAccessHandler;
import endfield.util.aspector.classes.BytecodeClassLoader;
import endfield.util.aspector.classes.BytecodeLoader;
import endfield.util.aspector.classes.ClassAccessor;
import endfield.util.aspector.classes.ClassDecl;
import endfield.util.aspector.classes.ClassName;
import endfield.util.aspector.classes.ReflectClassAccessor;
import endfield.util.aspector.generate.AspectFactory;
import endfield.util.handler.MethodHandler;
import kotlin.jvm.JvmClassMappingKt;
import kotlin.reflect.KClass;
import org.jetbrains.annotations.Nullable;

public final class RuntimeAspector {
	private RuntimeAspector() {}

	public static AspectDelegate withMaker(Func<ClassAccessor, AspectFactory> makerFactory, @Nullable Func<ClassAccessor, PackageAccessHandler> accessorFactory, ClassLoader... loaderPaths) {
		ReflectClassAccessor accessor = new ReflectClassAccessor(loaderPaths);
		Aspector aspector = new Aspector(makerFactory.get(accessor));
		PackageAccessHandler packageAccessor = accessorFactory == null ? null : accessorFactory.get(accessor);

		return new AspectDelegate(accessor, aspector, packageAccessor);
	}

	public static <T> T withMaker(Func<ClassAccessor, AspectFactory> makerFactory, @Nullable Func<ClassAccessor, PackageAccessHandler> accessorFactory, Func<AspectDelegate, T> scope, ClassLoader... loaderPaths) {
		return scope.get(withMaker(makerFactory, accessorFactory, loaderPaths));
	}

	public static class AspectDelegate {
		ClassAccessor accessor;
		Aspector aspector;
		@Nullable PackageAccessHandler packageAccessor;

		BytecodeLoader aspectLoader = new BytecodeClassLoader(getClass().getClassLoader());

		public AspectDelegate(ClassAccessor acce, Aspector aspe, @Nullable PackageAccessHandler packAcce) {
			accessor = acce;
			aspector = aspe;
			packageAccessor = packAcce;
		}

		public void use(BytecodeLoader loader) {
			aspectLoader = loader;
		}

		public <T> KClass<T> open(KClass<T> target) {
			return JvmClassMappingKt.getKotlinClass(open(JvmClassMappingKt.getJavaClass(target)));
		}

		public <T> Class<T> open(Class<T> target) {
			if (packageAccessor == null)
				throw new UnsupportedOperationException("Open package access must provide a PackageAccessHandler");

			return packageAccessor.getPackageAccessClass(target);
		}

		public <T> DeclDelegate<T> apply(Class<T> target, Class<?>... aspectDecl) {
			ClassDecl<?>[] aspectDecls = new ClassDecl<?>[aspectDecl.length];

			for (int i = 0; i < aspectDecl.length; i++) {
				aspectDecls[i] = accessor.getClassDecl(ClassName.byClass(aspectDecl[i]));
			}

			return new DeclDelegate<>(aspector.applyAspect(accessor.getClassDecl(ClassName.byClass(target)), aspectDecls));
		}

		public class DeclDelegate<T> {
			AspectDecl<T> decl;
			Class<T> aspectClass;

			public DeclDelegate(AspectDecl<T> aspectDecl) {
				decl = aspectDecl;
			}

			public Class<T> aspectClass() {
				load();
				return aspectClass;
			}

			public ClassName className() {
				return decl.getClassName();
			}

			public byte[] bytecode() {
				return decl.getBytecode();
			}

			public void load() {
				load(aspectLoader);
			}

			public void load(BytecodeLoader loader) {
				if (aspectClass == null) {
					aspectClass = decl.load(loader);
				}
			}

			public T instance(Object... args) {
				return MethodHandler.newInstanceDefault(aspectClass(), args);
			}
		}
	}
}
