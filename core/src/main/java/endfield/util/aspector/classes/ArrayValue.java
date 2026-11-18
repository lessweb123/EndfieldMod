package endfield.util.aspector.classes;

import kotlin.collections.CollectionsKt;

import java.util.ArrayList;
import java.util.List;

public class ArrayValue<V, T extends AnnotationValue<V, ?>> extends AnnotationValue<List<V>, List<T>> {
	public final List<T> values;

	public ArrayValue(List<T> vals) {
		values = vals;
	}

	@Override
	public ClassName getType() {
		return ClassName.CLASS_NAME.arrayName();
	}

	@Override
	public List<V> value() {
		List<V> value = new ArrayList<>(values.size());
		for (T val : values) value.add(val.value());

		return value;
	}

	@Override
	public List<T> rawValue() {
		return values;
	}
}
