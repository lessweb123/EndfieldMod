package endfield.util.aspector.classes;

import java.lang.annotation.Annotation;
import java.lang.reflect.Proxy;

public class NestedAnnotationValue extends AnnotationValue<Annotation, EAnnotation> {
	public final EAnnotation rawValue;
	Annotation value;

	public NestedAnnotationValue(EAnnotation anno) {
		rawValue = anno;
	}

	@Override
	public ClassName getType() {
		return ClassName.byClass(Annotation.class);
	}

	@Override
	public Annotation value() {
		if (value == null) {
			try {
				Class<?> annoType = Class.forName(rawValue.type.name());
				value = (Annotation) Proxy.newProxyInstance(annoType.getClassLoader(), new Class[]{annoType}, (obj, method, args) -> {
					AnnotationValue<?, ?> annoValue = rawValue.getValue(method.getName());

					if (annoValue != null) {
						return annoValue.value();
					}

					return method.invoke(obj, args);
				});
			} catch (ClassNotFoundException e) {
				throw new RuntimeException(e);
			}
		}

		return value;
	}

	@Override
	public EAnnotation rawValue() {
		return rawValue;
	}
}
