package endfield.util.aspector.classes;

public class TypeValue extends AnnotationValue<Class<?>, ClassName> {
	public final ClassName className;

	public TypeValue(ClassName name) {
		className = name;
	}

	@Override
	public ClassName getType() {
		return ClassName.jClassName;
	}

	@Override
	public Class<?> value() {
		try {
			return Class.forName(className.name());
		} catch (ClassNotFoundException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public ClassName rawValue() {
		return className;
	}
}
