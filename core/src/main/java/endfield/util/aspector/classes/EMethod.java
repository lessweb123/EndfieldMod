package endfield.util.aspector.classes;

import kotlin.collections.CollectionsKt;

import java.util.List;

public class EMethod extends ClassElement {
	public final List<EParameter> parameters;
	public final EAnnotatedType<?> annotatedReturnType;

	MethodSignature signature;

	public EMethod(ClassDecl<?> declaring, String name, List<EParameter> pars, EAnnotatedType<?> annoReturnType, int accessFlag, List<EAnnotation> annotations) {
		super(declaring, name, accessFlag, annotations);

		parameters = pars;
		annotatedReturnType = annoReturnType;
	}

	public MethodSignature signature() {
		if (signature == null) {
			signature = new MethodSignature(name, CollectionsKt.map(parameterTypes(), it -> it.name), returnType().name);
		}

		return signature;
	}

	public List<ClassDecl<?>> parameterTypes() {
		return CollectionsKt.map(parameters, it -> it.annotatedType().type());
	}

	public ClassDecl<?> returnType() {
		return annotatedReturnType.type();
	}
}
