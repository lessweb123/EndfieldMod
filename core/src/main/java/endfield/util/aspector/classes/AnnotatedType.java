package endfield.util.aspector.classes;

import java.util.List;

public class AnnotatedType<T> {
	ClassDecl<T> type;
	List<EAnnotation> annotations;

	public AnnotatedType(ClassDecl<T> decl, List<EAnnotation> annos) {
		type = decl;
		annotations = annos;
	}

	public ClassDecl<T> type() {
		return type;
	}

	public List<EAnnotation> annotations() {
		return annotations;
	}

	public EAnnotation getAnnotation(ClassName annoTypeName) {
		for (EAnnotation anno : annotations) {
			if (anno.type.equals(annoTypeName)) return anno;
		}

		return null;
	}
}
