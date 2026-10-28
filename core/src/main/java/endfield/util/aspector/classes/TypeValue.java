package endfield.util.aspector.classes;

import static endfield.util.GetKt.sneakyThrow;

public class TypeValue extends AnnotationValue<Class<?>, ClassName> {
	public final ClassName className;

	public TypeValue(ClassName name) {
		className = name;
	}

	@Override
	public ClassName getType() {
		return ClassName.CLASS_NAME;
	}

	@Override
	public Class<?> value() {
		try {
			return Class.forName(className.name());
		} catch (ClassNotFoundException e) {
			throw sneakyThrow(e);
		}
	}

	@Override
	public ClassName rawValue() {
		return className;
	}
}
