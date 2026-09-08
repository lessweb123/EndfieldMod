package endfield.util.aspector.classes;

import kotlin.collections.CollectionsKt;

import java.util.List;

public class EConstructor<T> extends ClassElement {
	public final List<EParameter> parameters;

	MethodSignature signature;

	public EConstructor(ClassDecl<T> decl, List<EParameter> pars, int flags, List<EAnnotation> annos) {
		super(decl, "<init>", flags, annos);

		parameters = pars;
	}

	public MethodSignature signature() {
		if (signature == null) {
			signature = new MethodSignature("<init>", CollectionsKt.map(parameterTypes(), it -> it.name), ClassName.V);
		}

		return signature;
	}

	public List<ClassDecl<?>> parameterTypes() {
		return CollectionsKt.map(parameters, it -> it.annotatedType().type());
	}

	public List<AnnotatedType<?>> annotatedParameterTypes() {
		return CollectionsKt.map(parameters, it -> it.annotatedType());
	}
}
