package endfield.util.aspector.classes;

import java.lang.annotation.Annotation;
import java.lang.annotation.IncompleteAnnotationException;
import java.lang.reflect.Modifier;
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

	@SuppressWarnings("unchecked")
	@Override
	public Annotation value() {
		if (value == null) {
			try {
				Class<?> annoType = Class.forName(rawValue.type.name());
				value = (Annotation) Proxy.newProxyInstance(annoType.getClassLoader(), new Class[]{annoType}, (obj, method, args) -> {
					AnnotationValue<?, ?> annoValue = rawValue.getValue(method.getName());

					if (annoValue != null) return annoValue.value();

					if ((method.getModifiers() & Modifier.ABSTRACT) == 0) return method.invoke(obj, args);

					throw new IncompleteAnnotationException((Class<? extends Annotation>) method.getDeclaringClass(), method.getName());
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
