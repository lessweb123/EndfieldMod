package endfield.util.aspector.classes;

import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public interface ClassAccessor {
	Map<ClassName, byte[]> sharedClassByte = new HashMap<>();

	ClassDecl<Void> voidDecl = new PrimitiveClassDecl<>(void.class);
	ClassDecl<Byte> byteDecl = new PrimitiveClassDecl<>(byte.class);
	ClassDecl<Short> shortDecl = new PrimitiveClassDecl<>(short.class);
	ClassDecl<Integer> intDecl = new PrimitiveClassDecl<>(int.class);
	ClassDecl<Long> longDecl = new PrimitiveClassDecl<>(long.class);
	ClassDecl<Float> floatDecl = new PrimitiveClassDecl<>(float.class);
	ClassDecl<Double> doubleDecl = new PrimitiveClassDecl<>(double.class);
	ClassDecl<Character> charDecl = new PrimitiveClassDecl<>(char.class);
	ClassDecl<Boolean> booleanDecl = new PrimitiveClassDecl<>(boolean.class);

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
		public EAnnotatedType<?> annotatedSuperClass() {
			return null;
		}

		@Override
		public List<ClassDecl<?>> interfaces() {
			return List.of();
		}

		@Override
		public List<EAnnotatedType<?>> annotatedInterfaces() {
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
