package endfield.util.aspector.accesses;

import endfield.util.aspector.PackageAccessor;
import endfield.util.aspector.classes.ClassAccessor;
import endfield.util.aspector.classes.ClassDecl;
import endfield.util.aspector.classes.ClassElement;
import endfield.util.aspector.classes.ClassName;
import endfield.util.aspector.classes.EConstructor;
import endfield.util.aspector.classes.EMethod;
import kotlin.collections.CollectionsKt;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

public abstract class PackageAccessHandler {
	protected ClassAccessor classAccessor;

	public PackageAccessHandler(ClassAccessor accessor) {
		classAccessor = accessor;
	}

	public String genPackageAccessClassName(Class<?> accessTarget) {
		return accessTarget.getName() + "$PackageAccess";
	}

	@SuppressWarnings("unchecked")
	public <T> Class<T> getPackageAccessClass(Class<T> accessTarget) {
		if (accessTarget.getAnnotation(PackageAccessor.class) != null) return accessTarget;

		checkAccessible(accessTarget);

		ClassName name = ClassName.byName(genPackageAccessClassName(accessTarget));

		try {
			ClassLoader targetLoader = accessTarget.getClassLoader();
			return (Class<T>) (targetLoader != null ? targetLoader.loadClass(name.name()) : Class.forName(name.name()));
		} catch (ClassNotFoundException e) {
			ClassName targetName = ClassName.byClass(accessTarget);
			ClassDecl<?> targetDecl = classAccessor.getClassDecl(targetName);

			AccessBuilder builder = new AccessBuilder(name, targetName);

			for (EMethod method : CollectionsKt.filter(targetDecl.methods(), it -> (it.flags & (Modifier.PUBLIC | Modifier.PROTECTED | Modifier.PRIVATE | Modifier.STATIC | Modifier.FINAL)) == 0)) {
				builder.registerEnhanceMethod(method);
			}
			for (EConstructor<?> constructor : CollectionsKt.filter(targetDecl.constructors(), it -> (it.flags & (Modifier.PRIVATE | Modifier.FINAL)) == 0)) {
				builder.registerEnhanceConstructor(constructor);
			}

			byte[] bytecode = genPackageAccessClass(builder);

			return (Class<T>) loadClass(name, bytecode, accessTarget);
		}
	}

	protected <T> void checkAccessible(Class<T> accessTarget) {
		if (accessTarget.isPrimitive())
			throw new IllegalArgumentException("Cannot enhance a primitive type.");
		if (accessTarget.isInterface())
			throw new IllegalArgumentException("Cannot enhance an interface type: $accessTarget.");
		if (Modifier.isFinal(accessTarget.getModifiers()) || Modifier.isPrivate(accessTarget.getModifiers()))
			throw new IllegalArgumentException("Cannot enhance access class with modifiers final or private.");
	}

	protected abstract byte[] genPackageAccessClass(AccessBuilder builder);

	protected abstract Class<?> loadClass(ClassName className, byte[] bytecode, Class<?> accessTarget);

	public static class AccessBuilder {
		public ClassName className;
		public ClassName accessTarget;

		public List<ClassElement> enhanceElements = new ArrayList<>();

		public AccessBuilder(ClassName name, ClassName target) {
			className = name;
			accessTarget = target;
		}

		public void registerEnhanceMethod(EMethod method) {
			enhanceElements.add(method);
		}

		public void registerEnhanceConstructor(EConstructor<?> constructor) {
			enhanceElements.add(constructor);
		}
	}
}
