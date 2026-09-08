package endfield.util.aspector.classes;

import java.util.List;

public abstract class ClassElement {
	public final ClassDecl<?> declaring;
	public final String name;
	public final int flags;
	public final List<EAnnotation> annotations;

	public ClassElement(ClassDecl<?> decla, String eleName, int flag, List<EAnnotation> annos) {
		declaring = decla;
		name = eleName;
		flags = flag;
		annotations = annos;
	}

	public EAnnotation getAnnotation(ClassName annoTypeName) {
		for (EAnnotation annotation : annotations) {
			if (annotation.type.equals(annoTypeName)) return annotation;
		}
		return null;
	}
}
