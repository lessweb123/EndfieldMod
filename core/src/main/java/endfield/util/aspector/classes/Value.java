package endfield.util.aspector.classes;

public class Value<T> extends AnnotationValue<T, T> {
	public final T value;

	public Value(T val) {
		value = val;
	}

	@Override
	public ClassName getType() {
		return ClassName.jClassName;
	}

	@Override
	public T value() {
		return value;
	}

	@Override
	public T rawValue() {
		return value;
	}
}
