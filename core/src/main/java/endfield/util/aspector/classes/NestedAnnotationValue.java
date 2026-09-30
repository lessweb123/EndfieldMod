package endfield.util.aspector.classes;

import java.lang.annotation.Annotation;
import java.lang.reflect.Proxy;

import static endfield.util.GetKt.sneakyThrow;

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

					return annoValue == null ? method.invoke(obj, args) : annoValue.value();
				});
			} catch (ClassNotFoundException e) {
				throw sneakyThrow(e);
			}
		}

		return value;
	}

	@Override
	public EAnnotation rawValue() {
		return rawValue;
	}
}
