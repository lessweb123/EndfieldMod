package endfield.util.aspector.classes;

import java.util.ArrayList;
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
			List<ClassName> types = new ArrayList<>();
			for (EParameter parameter : parameters) types.add(parameter.annotatedType().type().name);

			signature = new MethodSignature("<init>", types, ClassName.V);
		}

		return signature;
	}

	public List<ClassDecl<?>> parameterTypes() {
		List<ClassDecl<?>> types = new ArrayList<>(parameters.size());
		for (EParameter parameter : parameters) types.add(parameter.annotatedType().type());
		return types;
	}

	public List<EAnnotatedType<?>> annotatedParameterTypes() {
		List<EAnnotatedType<?>> types = new ArrayList<>(parameters.size());
		for (EParameter parameter : parameters) types.add(parameter.annotatedType());
		return types;
	}
}
