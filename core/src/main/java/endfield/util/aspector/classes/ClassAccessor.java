package endfield.util.aspector.classes;

import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public interface ClassAccessor {
	Map<ClassName, byte[]> sharedClassByte = new HashMap<>();

	ClassDecl<Void> voidDecl = new PrimitiveClassDecl<>(Void.TYPE);
	ClassDecl<Byte> byteDecl = new PrimitiveClassDecl<>(Byte.class);
	ClassDecl<Short> shortDecl = new PrimitiveClassDecl<>(Short.class);
	ClassDecl<Integer> intDecl = new PrimitiveClassDecl<>(Integer.class);
	ClassDecl<Long> longDecl = new PrimitiveClassDecl<>(Long.class);
	ClassDecl<Float> floatDecl = new PrimitiveClassDecl<>(Float.class);
	ClassDecl<Double> doubleDecl = new PrimitiveClassDecl<>(Double.class);
	ClassDecl<Character> charDecl = new PrimitiveClassDecl<>(Character.class);
	ClassDecl<Boolean> booleanDecl = new PrimitiveClassDecl<>(Boolean.class);

	<T> ClassDecl<T> getClassDecl(ClassName className);

	byte[] getBytes(ClassName className);

	class PrimitiveClassDecl<T> extends ClassDecl<T> {
		PrimitiveClassDecl(Class<T> clazz) {
			super(ClassName.byClass(clazz));
		}

		@Override
		public int flags() {
			return Modifier.PUBLIC | Modifier.FINAL;
		}

		@Override
		public ClassDecl<?> superClass() {
			return null;
		}

		@Override
		public AnnotatedType<?> annotatedSuperClass() {
			return null;
		}

		@Override
		public List<ClassDecl<?>> interfaces() {
			return List.of();
		}

		@Override
		public List<AnnotatedType<?>> annotatedInterfaces() {
			return List.of();
		}

		@Override
		public List<EField> fields() {
			return List.of();
		}

		@Override
		public List<EConstructor<T>> constructors() {
			return List.of();
		}

		@Override
		public List<EMethod> methods() {
			return List.of();
		}

		@Override
		public List<EAnnotation> annotations() {
			return List.of();
		}
	}
}
