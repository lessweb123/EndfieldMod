package endfield.util.aspector.classes;

import endfield.util.Reflections;

import java.lang.reflect.Modifier;
import java.util.List;

public abstract class ClassDecl<T> {
	public final ClassName name;

	public ClassDecl(ClassName className) {
		name = className;
	}

	public abstract int flags();

	public abstract ClassDecl<?> superClass();

	public abstract EAnnotatedType<?> annotatedSuperClass();

	public abstract List<ClassDecl<?>> interfaces();

	public abstract List<EAnnotatedType<?>> annotatedInterfaces();

	public abstract List<EField> fields();

	public abstract List<EConstructor<T>> constructors();

	public abstract List<EMethod> methods();

	public abstract List<EAnnotation> annotations();

	public EAnnotation getAnnotation(ClassName annoTypeName) {
		for (EAnnotation annotation : annotations()) {
			if (annotation.type.equals(annoTypeName)) return annotation;
		}
		return null;
	}

	public boolean isPublic() {
		return Modifier.isPublic(flags());
	}

	public boolean isProtected() {
		return Modifier.isProtected(flags());
	}

	public boolean isPrivate() {
		return Modifier.isPrivate(flags());
	}

	public boolean isInterface() {
		return Modifier.isInterface(flags());
	}

	public boolean isAbstract() {
		return Modifier.isAbstract(flags());
	}

	public boolean isFinal() {
		return Modifier.isFinal(flags());
	}

	public boolean isPrimitive() {
		return name.isPrimitive();
	}

	public boolean isArray() {
		return name.isArray();
	}

	public boolean isEnum() {
		if ((flags() & Reflections.ENUM) == 0) return false;

		ClassDecl<?> superClass = superClass();
		return superClass != null && superClass.name.equals(ClassName.ENUM);
	}
}
