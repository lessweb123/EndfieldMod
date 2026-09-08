package endfield.util.aspector.classes;

import kotlin.Pair;
import kotlin.collections.ArraysKt;
import kotlin.collections.CollectionsKt;
import kotlin.collections.MapsKt;
import kotlin.jvm.JvmClassMappingKt;
import kotlin.reflect.KClass;
import kotlin.reflect.KType;

import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ReflectClassAccessor implements ClassAccessor {
	List<ClassLoader> attachedClassLoader = new ArrayList<>();
	Map<ClassName, ClassDecl<?>> loadedDeclMap = new HashMap<>();

	public ReflectClassAccessor(ClassLoader... loaders) {
		if (loaders.length == 0) {
			attachedClassLoader.add(getClass().getClassLoader());
		} else {
			Collections.addAll(attachedClassLoader, loaders);
		}
	}

	public void attachClassLoader(ClassLoader loader) {
		attachedClassLoader.add(loader);
	}

	Class<?> loadClass(ClassName className) {
		return switch (className.descriptor()) {
			case "V" -> void.class;
			case "B" -> byte.class;
			case "S" -> short.class;
			case "I" -> int.class;
			case "J" -> long.class;
			case "F" -> float.class;
			case "D" -> double.class;
			case "C" -> char.class;
			case "Z" -> boolean.class;
			default -> {
				if (className.isArray()) {
					Class<?> componentType = loadClass(className.componentName());
					yield componentType.arrayType();
				}

				String name = className.name();

				for (ClassLoader loader : attachedClassLoader) {
					try {
						yield loader.loadClass(name);
					} catch (ClassNotFoundException ignored) {
					}
				}

				throw new RuntimeException(className.name());
			}
		};
	}

	@Override
	public byte[] getBytes(ClassName className) {
		Class<?> clazz = loadClass(className);
		ClassLoader loader = clazz.getClassLoader();

		if (loader == null) loader = ClassLoader.getSystemClassLoader();

		String path = clazz.getName().replace('.', '/') + ".class";

		try (InputStream stream = loader.getResourceAsStream(path)) {
			if (stream != null) {
				return stream.readAllBytes();
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		byte[] bytes = ClassAccessor.sharedClassByte.get(className);
		if (bytes != null) return bytes;

		throw new IllegalArgumentException("Class " + clazz + " have no bytecode found.");
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> ClassDecl<T> getClassDecl(ClassName className) {
		return (ClassDecl<T>) switch (className.descriptor()) {
			case "V" -> ClassAccessor.voidDecl;
			case "B" -> ClassAccessor.byteDecl;
			case "S" -> ClassAccessor.shortDecl;
			case "I" -> ClassAccessor.intDecl;
			case "J" -> ClassAccessor.longDecl;
			case "F" -> ClassAccessor.floatDecl;
			case "D" -> ClassAccessor.doubleDecl;
			case "C" -> ClassAccessor.charDecl;
			case "Z" -> ClassAccessor.booleanDecl;
			default -> loadedDeclMap.computeIfAbsent(className, name -> {
				if (name.isArray()) {
					return new ArrayClassDecl<>(loadClass(name.componentName()));
				}

				Class<?> clazz = loadClass(name);
				return new ReflectClassDecl<>(this, clazz);
			});
		};
	}

	static EAnnotation asEAnnotation(Annotation annotation) {
		Class<?> annoType = annotation.getClass().getInterfaces()[0];
		List<Pair<String, AnnotationValue<?, ?>>> pairs = CollectionsKt.map(ArraysKt.filter(annoType.getMethods(), it -> it.getParameters().length == 0
				&& !it.getName().equals("toString") && !it.getName().equals("hashCode") && !it.getName().equals("annotationType")), method -> {
			Object value;
			try {
				value = method.invoke(annotation);
			} catch (IllegalAccessException | InvocationTargetException e) {
				Object defaultValue = method.getDefaultValue();
				if (defaultValue != null) {
					value = defaultValue;
				} else {
					throw new IllegalStateException("Failed to invoke annotation method " + method.getName() + " and no default value found.");
				}
			}
			return new Pair<>(method.getName(), handleAnnotationValue(value));
		});

		return new EAnnotation(ClassName.byClass(annoType), MapsKt.toMap(pairs));
	}

	static AnnotationValue<?, ?> handleAnnotationValue(Object value) {
		if (value instanceof byte[] || value instanceof short[] || value instanceof int[] || value instanceof long[] ||
				value instanceof float[] || value instanceof double[] || value instanceof boolean[] || value instanceof char[] ||
				value instanceof String || value instanceof Byte || value instanceof Short || value instanceof Integer || value instanceof Long ||
				value instanceof Float || value instanceof Double || value instanceof Boolean || value instanceof Character) {
			return new Value<>(value);
		} else if (value instanceof Class<?> c) {
			return new TypeValue(ClassName.byClass(c));
		} else if (value instanceof Enum<?> e) {
			return new EnumValue<>(ClassName.byClass(value.getClass()), e.name());
		} else if (value instanceof Object[] a) {
			return new ArrayValue<>(ArraysKt.map(a, it -> handleAnnotationValue(it)));
		} else if (value instanceof Annotation a) {
			return new NestedAnnotationValue(asEAnnotation(a));
		} else {
			throw new IllegalArgumentException("Unsupported value type: " + value);
		}
	}

	static class ArrayClassDecl<T> extends ClassDecl<T> {
		public ArrayClassDecl(Class<?> clazz) {
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
			return List.of(new EField(
					this,
					"length",
					new AnnotatedType<>(ClassAccessor.intDecl, List.of()),
					Modifier.PUBLIC | Modifier.FINAL,
					null,
					List.of()
			));
		}

		@Override
		public List<EMethod> methods() {
			return List.of();
		}

		@Override
		public List<EConstructor<T>> constructors() {
			return List.of();
		}

		@Override
		public List<EAnnotation> annotations() {
			return List.of();
		}
	}

	static class ReflectClassDecl<T> extends ClassDecl<T> {
		ClassAccessor accessor;
		Class<T> clazz;

		ClassDecl<?> superClass;
		AnnotatedType<?> annotatedSuperClass;
		List<ClassDecl<?>> interfaces;
		List<AnnotatedType<?>> annotatedInterfaces;
		List<EField> fields;
		List<EMethod> methods;
		List<EConstructor<T>> constructors;
		List<EAnnotation> annotations;

		public ReflectClassDecl(ClassAccessor a, Class<T> c) {
			super(ClassName.byClass(c));

			accessor = a;
			clazz = c;
		}

		@Override
		public int flags() {
			return clazz.getModifiers();
		}

		@Override
		public ClassDecl<?> superClass() {
			if (superClass == null) {
				Class<?> superClazz = clazz.getSuperclass();
				if (superClazz != null) superClass = accessor.getClassDecl(ClassName.byClass(superClazz));
			}
			return superClass;
		}

		@Override
		public AnnotatedType<?> annotatedSuperClass() {
			if (annotatedSuperClass == null) {
				annotatedSuperClass = getSuperClassWithAnnotations(clazz);
			}
			return annotatedSuperClass;
		}

		@Override
		public List<ClassDecl<?>> interfaces() {
			if (interfaces == null) {
				interfaces = ArraysKt.map(clazz.getInterfaces(), it -> accessor.getClassDecl(ClassName.byClass(it)));
			}
			return interfaces;
		}

		@Override
		public List<AnnotatedType<?>> annotatedInterfaces() {
			if (annotatedInterfaces == null) {
				List<AnnotatedType<?>> supertypes = getSuperTypesWithAnnotations(clazz);
				annotatedInterfaces = CollectionsKt.filter(supertypes, it -> it.type().isInterface());
			}
			return annotatedInterfaces;
		}

		@Override
		public List<EField> fields() {
			if (fields == null) {
				fields = ArraysKt.map(clazz.getDeclaredFields(), field -> new EField(
						accessor.getClassDecl(ClassName.byClass(field.getDeclaringClass())),
						field.getName(),
						toAnnoType(field.getAnnotatedType()),
						field.getModifiers(),
						null,
						ArraysKt.map(field.getAnnotations(), annotation -> asEAnnotation(annotation))
				));
			}
			return fields;
		}

		@Override
		public List<EMethod> methods() {
			if (methods == null) {
				methods = ArraysKt.map(clazz.getDeclaredMethods(), method -> new EMethod(
						accessor.getClassDecl(ClassName.byClass(method.getDeclaringClass())),
						method.getName(),
						ArraysKt.map(method.getParameters(), parameter -> new EParameter(
								parameter.getName(),
								toAnnoType(parameter.getAnnotatedType()),
								ArraysKt.map(parameter.getAnnotations(), annotation -> asEAnnotation(annotation))
						)),
						toAnnoType(method.getAnnotatedReturnType()),
						method.getModifiers(),
						ArraysKt.map(method.getAnnotations(), annotation -> asEAnnotation(annotation))
				));
			}
			return methods;
		}

		@Override
		public List<EConstructor<T>> constructors() {
			if (constructors == null) {
				constructors = ArraysKt.map(clazz.getDeclaredConstructors(), constructor -> new EConstructor<>(
						accessor.getClassDecl(ClassName.byClass(constructor.getDeclaringClass())),
						ArraysKt.map(constructor.getParameters(), parameter -> new EParameter(
								parameter.getName(),
								toAnnoType(parameter.getAnnotatedType()),
								ArraysKt.map(parameter.getAnnotations(), annotation -> asEAnnotation(annotation))
						)),
						constructor.getModifiers(),
						ArraysKt.map(constructor.getAnnotations(), annotation -> asEAnnotation(annotation))
				));
			}
			return constructors;
		}

		@Override
		public List<EAnnotation> annotations() {
			if (annotations == null) {
				annotations = ArraysKt.map(clazz.getDeclaredAnnotations(), it -> asEAnnotation(it));
			}
			return annotations;
		}

		@SuppressWarnings("unchecked")
		<U> AnnotatedType<U> toAnnoType(java.lang.reflect.AnnotatedType at) {
			Type t = at.getType();

			Class<U> c;
			if (t instanceof ParameterizedType pt) {
				c = (Class<U>) pt.getRawType();
			} else if (t instanceof Class<?> cs) {
				c = (Class<U>) cs;
			} else {
				throw new UnsupportedOperationException("Unsupported type " + t.getClass().getName());
			}

			return new AnnotatedType<>(accessor.getClassDecl(ClassName.byClass(c)), ArraysKt.map(at.getAnnotations(), it -> asEAnnotation(it)));
		}

		AnnotatedType<?> getSuperClassWithAnnotations(Class<?> clazz) {
			Class<?> superClazz = clazz.getSuperclass();

			if (superClazz == null) return null;

			List<Annotation> list = new ArrayList<>();

			KType superClass = CollectionsKt.firstOrNull(JvmClassMappingKt.getKotlinClass(clazz).getSupertypes(),
					t -> !JvmClassMappingKt.getJavaClass((KClass<?>) t.getClassifier()).isInterface());
			if (superClass != null) {
				list.addAll(superClass.getAnnotations());
			}

			java.lang.reflect.AnnotatedType annotatedSuperclass = clazz.getAnnotatedSuperclass();
			if (annotatedSuperclass != null) {
				Collections.addAll(list, annotatedSuperclass.getAnnotations());
			}

			return new AnnotatedType<>(accessor.getClassDecl(ClassName.byClass(superClazz)), CollectionsKt.map(list, it -> asEAnnotation(it)));
		}

		List<AnnotatedType<?>> getSuperTypesWithAnnotations(Class<?> clazz) {
			Map<Class<?>, List<Annotation>> typeAnnotations = new HashMap<>();

			for (KType it : JvmClassMappingKt.getKotlinClass(clazz).getSupertypes()) {
				List<Annotation> annotations = it.getAnnotations();
				typeAnnotations.computeIfAbsent(JvmClassMappingKt.getJavaClass((KClass<?>) it.getClassifier()), c -> new ArrayList<>()).addAll(annotations);
			}

			List<java.lang.reflect.AnnotatedType> javaTypes = CollectionsKt.plus(CollectionsKt.listOfNotNull(clazz.getAnnotatedSuperclass()), clazz.getAnnotatedInterfaces());
			for (java.lang.reflect.AnnotatedType it : javaTypes) {
				Collections.addAll(typeAnnotations.computeIfAbsent((Class<?>) it.getType(), c -> new ArrayList<>()), it.getAnnotations());
			}

			return MapsKt.map(typeAnnotations, it -> {
				Class<?> c = it.getKey();
				List<EAnnotation> as = CollectionsKt.map(it.getValue(), a -> asEAnnotation(a));
				return new AnnotatedType<>(accessor.getClassDecl(ClassName.byClass(c)), as);
			});
		}
	}
}
