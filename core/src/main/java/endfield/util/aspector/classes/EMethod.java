package endfield.util.aspector.classes;

import java.util.ArrayList;
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
			List<ClassName> types = new ArrayList<>(parameters.size());
			for (EParameter parameter : parameters) types.add(parameter.annotatedType().type().name);

			signature = new MethodSignature(name, types, returnType().name);
		}

		return signature;
	}

	public List<ClassDecl<?>> parameterTypes() {
		List<ClassDecl<?>> types = new ArrayList<>(parameters.size());
		for (EParameter parameter : parameters) types.add(parameter.annotatedType().type());
		return types;
	}

	public ClassDecl<?> returnType() {
		return annotatedReturnType.type();
	}
}
