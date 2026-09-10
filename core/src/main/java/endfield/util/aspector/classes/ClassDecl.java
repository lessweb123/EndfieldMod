package endfield.util.aspector.classes;

import java.lang.reflect.Modifier;
import java.util.List;

public abstract class ClassDecl<T> {
	public static final int BRIDGE = 0x00000040;
	public static final int VARARGS = 0x00000080;
	public static final int SYNTHETIC = 0x00001000;
	public static final int ANNOTATION = 0x00002000;
	public static final int ENUM = 0x00004000;
	public static final int MANDATED = 0x00008000;

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
		if ((flags() & ENUM) == 0) return false;

		ClassDecl<?> superClass = superClass();
		return superClass != null && superClass.name.equals(ClassName.jEnum);
	}
}
