package endfield.util.aspector.classes;

import java.util.List;

public class EField extends ClassElement {
	public final EAnnotatedType<?> annotatedType;
	public final Object constant;

	public EField(ClassDecl<?> declaring, String name, EAnnotatedType<?> annoType, int flags, Object cons, List<EAnnotation> annotations) {
		super(declaring, name, flags, annotations);

		annotatedType = annoType;
		constant = cons;
	}

	public ClassDecl<?> type() {
		return annotatedType.type();
	}
}
