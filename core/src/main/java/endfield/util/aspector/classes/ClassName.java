package endfield.util.aspector.classes;

import endfield.util.CollectionObjectMap;
import endfield.util.Strings2;
import kotlin.jvm.JvmClassMappingKt;
import kotlin.reflect.KClass;
import kotlin.text.StringsKt;
import org.objectweb.asm.Type;

public class ClassName {
	static final CollectionObjectMap<String, ClassName> nameMap = new CollectionObjectMap<>(String.class, ClassName.class);

	public static final ClassName V = new ClassName("V");
	public static final ClassName Z = new ClassName("Z");
	public static final ClassName C = new ClassName("C");
	public static final ClassName B = new ClassName("B");
	public static final ClassName S = new ClassName("S");
	public static final ClassName I = new ClassName("I");
	public static final ClassName J = new ClassName("J");
	public static final ClassName F = new ClassName("F");
	public static final ClassName D = new ClassName("D");

	public static final ClassName OBJECT = byClass(Object.class);
	public static final ClassName BOOLEAN = byClass(Boolean.class);
	public static final ClassName CHARACTER = byClass(Character.class);
	public static final ClassName BYTE = byClass(Byte.class);
	public static final ClassName SHORT = byClass(Short.class);
	public static final ClassName INTEGER = byClass(Integer.class);
	public static final ClassName LONG = byClass(Long.class);
	public static final ClassName FLOAT = byClass(Float.class);
	public static final ClassName DOUBLE = byClass(Double.class);
	public static final ClassName NOTHING = byClass(Void.class);
	public static final ClassName CLASS = byClass(Class.class);
	public static final ClassName STRING = byClass(String.class);
	public static final ClassName ENUM = byClass(Enum.class);

	public static final ClassName CLASS_NAME = byClass(ClassName.class);

	public final String descriptor;

	static {
		nameMap.put("V", V);
		nameMap.put("Z", Z);
		nameMap.put("C", C);
		nameMap.put("B", B);
		nameMap.put("S", S);
		nameMap.put("I", I);
		nameMap.put("J", J);
		nameMap.put("F", F);
		nameMap.put("D", D);
	}

	ClassName(String des) {
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
		return byDescriptor(Type.getDescriptor(clazz));
	}

	public static ClassName byDescriptor(String descriptor) {
		return nameMap.computeIfAbsent(descriptor, ClassName::new);
	}

	public static ClassName byInternalName(String internalName) {
		return byDescriptor(internalToDesc(internalName));
	}

	public static ClassName byName(String name) {
		return byDescriptor(internalToDesc(name.replace(".", "/")));
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
		return byDescriptor(Strings2.substringAfter(descriptor, "["));
	}

	public ClassName arrayName() {
		return byDescriptor("[" + descriptor);
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
		return this == obj || obj instanceof ClassName n && descriptor.equals(n.descriptor);
	}
}
