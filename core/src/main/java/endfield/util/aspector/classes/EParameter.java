package endfield.util.aspector.classes;

import java.util.List;

public class EParameter {
	String name;
	AnnotatedType<?> annotatedType;
	List<EAnnotation> annotations;

	public EParameter(String pn, AnnotatedType<?> annoType, List<EAnnotation> annos) {
		name = pn;
		annotatedType = annoType;
		annotations = annos;
	}

	public String name() {
		return name;
	}

	public AnnotatedType<?> annotatedType() {
		return annotatedType;
	}

	public List<EAnnotation> annotations() {
		return annotations;
	}

	public EAnnotation getAnnotation(ClassName annoTypeName) {
		for (EAnnotation annotation : annotations) {
			if (annotation.type.equals(annoTypeName)) return annotation;
		}
		return null;
	}
}
