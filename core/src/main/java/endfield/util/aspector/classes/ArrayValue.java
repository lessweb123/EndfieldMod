package endfield.util.aspector.classes;

import kotlin.collections.CollectionsKt;

import java.util.List;

public class ArrayValue<V, T extends AnnotationValue<V, ?>> extends AnnotationValue<List<V>, List<T>> {
	public final List<T> values;

	public ArrayValue(List<T> vals) {
		values = vals;
	}

	@Override
	public ClassName getType() {
		return ClassName.jClassName.arrayName();
	}

	@Override
	public List<V> value() {
		return CollectionsKt.map(values, it -> it.value());
	}

	@Override
	public List<T> rawValue() {
		return values;
	}
}
