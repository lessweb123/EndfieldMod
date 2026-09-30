package endfield.util.aspector.classes;

import endfield.util.Strings2;
import kotlin.jvm.JvmClassMappingKt;
import kotlin.reflect.KClass;
import kotlin.text.StringsKt;
import org.objectweb.asm.Type;

public class ClassName {
	public static final ClassName V = new ClassName("V");
	public static final ClassName Z = new ClassName("Z");
	public static final ClassName C = new ClassName("C");
	public static final ClassName B = new ClassName("B");
	public static final ClassName S = new ClassName("S");
	public static final ClassName I = new ClassName("I");
	public static final ClassName J = new ClassName("J");
	public static final ClassName F = new ClassName("F");
	public static final ClassName D = new ClassName("D");

	public static final ClassName jObject = byClass(Object.class);
	public static final ClassName jNothing = byClass(void.class);
	public static final ClassName jClass = byClass(Class.class);
	public static final ClassName jString = byClass(String.class);
	public static final ClassName jEnum = byClass(Enum.class);
	public static final ClassName jClassName = byClass(ClassName.class);

	final String descriptor;

	public ClassName(String des) {
		descriptor = des;
	}

	public static String descToInternal(String signatureName) {
		return switch (signatureName.charAt(0)) {
			case 'V' -> "void";
			case 'Z' -> "boolean";
			case 'C' -> "char";
			case 'B' -> "byte";
			case 'S' -> "short";
			case 'I' -> "int";
			case 'J' -> "long";
			case 'F' -> "float";
			case 'D' -> "double";
			case 'L' -> StringsKt.trimEnd(signatureName.substring(1), ';');
			case '[' -> descToInternal(signatureName.substring(1));
			default -> throw new IllegalArgumentException("Illegal class name: " + signatureName);
		};
	}

	public static String internalToDesc(String internalName) {
		return switch (internalName) {
			case "void" -> "V";
			case "boolean" -> "Z";
			case "char" -> "C";
			case "byte" -> "B";
			case "short" -> "S";
			case "int" -> "I";
			case "long" -> "J";
			case "float" -> "F";
			case "double" -> "D";
			default -> internalName.startsWith("[") ? internalName : "L" + internalName + ";";
		};
	}

	public static ClassName byClass(KClass<?> clazz) {
		return byClass(JvmClassMappingKt.getJavaClass(clazz));
	}

	public static ClassName byClass(Class<?> clazz) {
		return new ClassName(Type.getDescriptor(clazz));
	}

	public static ClassName byDescriptor(String descriptor) {
		return new ClassName(descriptor);
	}

	public static ClassName byInternalName(String internalName) {
		return new ClassName(internalToDesc(internalName));
	}

	public static ClassName byName(String name) {
		return new ClassName(internalToDesc(name.replace(".", "/")));
	}

	public String descriptor() {
		return descriptor;
	}

	public String internalName() {
		return descToInternal(descriptor);
	}

	public String name() {
		return descToInternal(descriptor).replace("/", ".");
	}

	public String simpleName() {
		return Strings2.substringAfterLast(name(), ".");
	}

	public String packageName() {
		return Strings2.substringBeforeLast(name(), ".");
	}

	public boolean isPrimitive() {
		return descriptor.length() == 1;
	}

	public boolean isArray() {
		return descriptor.startsWith("[");
	}

	public ClassName componentName() {
		return new ClassName(Strings2.substringAfter(descriptor, "["));
	}

	public ClassName arrayName() {
		return new ClassName("[" + descriptor);
	}

	@Override
	public String toString() {
		return name();
	}

	@Override
	public int hashCode() {
		return descriptor.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (!(obj instanceof ClassName cn)) return false;

		return descriptor.equals(cn.descriptor);
	}
}
