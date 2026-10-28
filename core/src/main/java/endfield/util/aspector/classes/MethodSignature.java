package endfield.util.aspector.classes;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

public class MethodSignature {
	static final Pattern descriptorMatcher = Pattern.compile("^\\(([BCDFIJSZ]|\\[+[BCDFIJSZ]|\\[*L[^;]+;)*\\)([BCDFIJSZ]|\\[+[BCDFIJSZ]|V|\\[*L[^;]+;)$");

	public final String methodName;
	public final List<ClassName> paramTypes;
	public final ClassName returnType;

	int hash;

	public MethodSignature(String metName, List<ClassName> parTypes, ClassName retType) {
		methodName = metName;
		paramTypes = parTypes;
		returnType = retType;
	}

	public static MethodSignature parse(String methodName, String descriptor) {
		if (!descriptorMatcher.matcher(descriptor).matches())
			throw new IllegalArgumentException("Invalid signature string: " + descriptor);
		if (methodName.contains("(") || methodName.contains(")") || methodName.contains(";"))
			throw new IllegalArgumentException("Invalid method name: " + methodName);

		List<ClassName> paramTypes = new ArrayList<>();
		ClassName returnType = null;

		int index = 0;

		while (index < descriptor.length()) {
			char c = descriptor.charAt(index);
			switch (c) {
				case '(' -> index++;
				case ')' -> {
					index++;

					char c2 = descriptor.charAt(index);
					ClassName res = switch (c2) {
						case 'B' -> ClassName.B;
						case 'S' -> ClassName.S;
						case 'I' -> ClassName.I;
						case 'J' -> ClassName.J;
						case 'F' -> ClassName.F;
						case 'D' -> ClassName.D;
						case 'C' -> ClassName.C;
						case 'Z' -> ClassName.Z;
						case 'V' -> ClassName.V;
						case 'L' -> {
							int nextSemicolon = descriptor.indexOf(';', index);
							if (nextSemicolon == -1)
								throw new IllegalArgumentException("Invalid signature string: " + descriptor);

							ClassName className = ClassName.byDescriptor(descriptor.substring(index, nextSemicolon + 1));
							index = nextSemicolon;
							yield className;
						}
						default -> throw new IllegalArgumentException("Invalid signature string: " + c2);
					};

					index++;
					returnType = res;
				}
				default -> {
					char c2 = descriptor.charAt(index);
					ClassName res = switch (c2) {
						case 'B' -> ClassName.B;
						case 'S' -> ClassName.S;
						case 'I' -> ClassName.I;
						case 'J' -> ClassName.J;
						case 'F' -> ClassName.F;
						case 'D' -> ClassName.D;
						case 'C' -> ClassName.C;
						case 'Z' -> ClassName.Z;
						case 'V' -> ClassName.V;
						case 'L' -> {
							var nextSemicolon = descriptor.indexOf(';', index);
							if (nextSemicolon == -1)
								throw new IllegalArgumentException("Invalid signature string: " + descriptor);

							var className = ClassName.byDescriptor(descriptor.substring(index, nextSemicolon + 1));
							index = nextSemicolon;
							yield className;
						}
						default -> throw new IllegalArgumentException("Invalid signature string: " + c2);
					};

					index++;
					paramTypes.add(res);
				}
			}
		}

		if (returnType == null)
			throw new IllegalArgumentException("Invalid signature string: " + descriptor);

		return new MethodSignature(methodName, paramTypes, returnType);
	}

	public boolean match(MethodSignature other) {
		return other == this || methodName.equals(other.methodName) && paramTypes.equals(other.paramTypes);
	}

	@Override
	public boolean equals(Object obj) {
		return this == obj || obj instanceof MethodSignature that &&
				methodName.equals(that.methodName) &&
				paramTypes.equals(that.paramTypes) &&
				returnType.equals(that.returnType);
	}

	@Override
	public int hashCode() {
		int h = hash;

		if (h == 0) {
			h = hash = Objects.hash(methodName, paramTypes, returnType);
		}

		return h;
	}

	public String jvmDescriptor() {
		StringBuilder builder = new StringBuilder();

		for (ClassName type : paramTypes) {
			builder.append(type.descriptor());
		}

		return "(" + builder + ")" + returnType.descriptor();
	}
}
