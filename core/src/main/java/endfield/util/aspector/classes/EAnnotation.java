package endfield.util.aspector.classes;

import java.util.Map;

public class EAnnotation {
	public ClassName type;
	Map<String, AnnotationValue<?, ?>> values;

	public EAnnotation(ClassName className, Map<String, AnnotationValue<?, ?>> valueMap) {
		type = className;
		values = valueMap;
	}

	@SuppressWarnings("unchecked")
	public <T extends AnnotationValue<?, ?>> T getValue(String name) {
		return (T) values.get(name);
	}
}