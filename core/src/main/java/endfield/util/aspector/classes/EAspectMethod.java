package endfield.util.aspector.classes;

import endfield.util.aspector.Using;

import java.util.List;

public class EAspectMethod extends EMethod {
	public Using using;

	public EAspectMethod(ClassDecl<?> declaring, String methodName, List<EParameter> parameters, AnnotatedType<?> returnType, int flag, Using u, List<EAnnotation> annotations) {
		super(declaring, methodName, parameters, returnType, flag, annotations);
		using = u;
	}
}
