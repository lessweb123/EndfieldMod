package endfield.util.aspector.classes;

import kotlin.collections.ArraysKt;
import kotlin.jvm.JvmClassMappingKt;
import kotlin.reflect.KClass;
import kotlin.reflect.KType;

import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedType;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
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
					yield Array.newInstance(componentType, 0).getClass();
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

		Map<String, AnnotationValue<?, ?>> valueMap = new HashMap<>();

		for (Method method : annoType.getMethods())
			if (method.getParameters().length == 0 && !method.getName().equals("toString") && !method.getName().equals("hashCode") && !method.getName().equals("annotationType")) {
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
				valueMap.put(method.getName(), handleAnnotationValue(value));
			}

		return new EAnnotation(ClassName.byClass(annoType), valueMap);
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
			return List.of(new EField(
					this,
					"length",
					new EAnnotatedType<>(ClassAccessor.intDecl, List.of()),
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
		EAnnotatedType<?> annotatedSuperClass;
		List<ClassDecl<?>> interfaces;
		List<EAnnotatedType<?>> annotatedInterfaces;
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
		public EAnnotatedType<?> annotatedSuperClass() {
			if (annotatedSuperClass == null) {
				annotatedSuperClass = getSuperClassWithAnnotations(clazz);
			}
			return annotatedSuperClass;
		}

		@Override
		public List<ClassDecl<?>> interfaces() {
			if (interfaces == null) {
				interfaces = new ArrayList<>();

				for (Class<?> inter : clazz.getInterfaces()) {
					interfaces.add(accessor.getClassDecl(ClassName.byClass(inter)));
				}
			}
			return interfaces;
		}

		@Override
		public List<EAnnotatedType<?>> annotatedInterfaces() {
			if (annotatedInterfaces == null) {
				annotatedInterfaces = new ArrayList<>();

				List<EAnnotatedType<?>> supertypes = getSuperTypesWithAnnotations(clazz);
				for (EAnnotatedType<?> supertype : supertypes) {
					if (supertype.type().isInterface()) annotatedInterfaces.add(supertype);
				}
			}
			return annotatedInterfaces;
		}

		@Override
		public List<EField> fields() {
			if (fields == null) {
				fields = new ArrayList<>();

				for (Field field : clazz.getDeclaredFields()) {
					List<EAnnotation> annotations = new ArrayList<>();

					for (Annotation annotation : field.getAnnotations()) annotations.add(asEAnnotation(annotation));

					fields.add(new EField(
							accessor.getClassDecl(ClassName.byClass(field.getDeclaringClass())),
							field.getName(),
							toAnnoType(field.getAnnotatedType()),
							field.getModifiers(),
							null,
							annotations
					));
				}
			}
			return fields;
		}

		@Override
		public List<EMethod> methods() {
			if (methods == null) {
				methods = new ArrayList<>();

				for (Method method : clazz.getDeclaredMethods()) {
					List<EParameter> parameters = new ArrayList<>();

					for (Parameter parameter : method.getParameters()) {
						List<EAnnotation> annotations = new ArrayList<>();

						for (Annotation annotation : parameter.getAnnotations()) annotations.add(asEAnnotation(annotation));

						parameters.add(new EParameter(parameter.getName(), toAnnoType(parameter.getAnnotatedType()), annotations));
					}

					List<EAnnotation> annotations = new ArrayList<>();

					for (Annotation annotation : method.getAnnotations()) annotations.add(asEAnnotation(annotation));

					methods.add(new EMethod(
							accessor.getClassDecl(ClassName.byClass(method.getDeclaringClass())),
							method.getName(),
							parameters,
							toAnnoType(method.getAnnotatedReturnType()),
							method.getModifiers(),
							annotations
					));
				}
			}
			return methods;
		}

		@Override
		public List<EConstructor<T>> constructors() {
			if (constructors == null) {
				constructors = new ArrayList<>();

				for (Constructor<?> constructor : clazz.getDeclaredConstructors()) {
					List<EParameter> parameters = new ArrayList<>();

					for (Parameter parameter : constructor.getParameters()) {
						List<EAnnotation> annotations = new ArrayList<>();

						for (Annotation annotation : parameter.getAnnotations()) annotations.add(asEAnnotation(annotation));

						parameters.add(new EParameter(parameter.getName(), toAnnoType(parameter.getAnnotatedType()), annotations));
					}

					List<EAnnotation> annotations = new ArrayList<>();

					for (Annotation annotation : constructor.getAnnotations()) annotations.add(asEAnnotation(annotation));

					constructors.add(new EConstructor<>(
							accessor.getClassDecl(ClassName.byClass(constructor.getDeclaringClass())),
							parameters,
							constructor.getModifiers(),
							annotations
					));
				}
			}
			return constructors;
		}

		@Override
		public List<EAnnotation> annotations() {
			if (annotations == null) {
				annotations = new ArrayList<>();

				for (Annotation annotation : clazz.getDeclaredAnnotations()) annotations.add(asEAnnotation(annotation));
			}
			return annotations;
		}

		@SuppressWarnings("unchecked")
		<U> EAnnotatedType<U> toAnnoType(AnnotatedType at) {
			Type t = at.getType();

			Class<U> c;
			if (t instanceof ParameterizedType pt) {
				c = (Class<U>) pt.getRawType();
			} else if (t instanceof Class<?> cs) {
				c = (Class<U>) cs;
			} else {
				throw new UnsupportedOperationException("Unsupported type " + t.getClass().getName());
			}

			return new EAnnotatedType<>(accessor.getClassDecl(ClassName.byClass(c)), ArraysKt.map(at.getAnnotations(), it -> asEAnnotation(it)));
		}

		EAnnotatedType<?> getSuperClassWithAnnotations(Class<?> clazz) {
			Class<?> superClazz = clazz.getSuperclass();

			if (superClazz == null) return null;

			List<Annotation> list = new ArrayList<>();

			for (KType superClass : JvmClassMappingKt.getKotlinClass(clazz).getSupertypes()) {
				if (!JvmClassMappingKt.getJavaClass((KClass<?>) superClass.getClassifier()).isInterface()) {
					list.addAll(superClass.getAnnotations());
					break;
				}
			}

			AnnotatedType annotatedSuperclass = clazz.getAnnotatedSuperclass();
			if (annotatedSuperclass != null) {
				Collections.addAll(list, annotatedSuperclass.getAnnotations());
			}

			List<EAnnotation> annotations = new ArrayList<>();

			for (Annotation annotation : list) annotations.add(asEAnnotation(annotation));

			return new EAnnotatedType<>(accessor.getClassDecl(ClassName.byClass(superClazz)), annotations);
		}

		List<EAnnotatedType<?>> getSuperTypesWithAnnotations(Class<?> clazz) {
			Map<Class<?>, List<Annotation>> typeAnnotations = new HashMap<>();

			for (KType it : JvmClassMappingKt.getKotlinClass(clazz).getSupertypes()) {
				List<Annotation> annotations = it.getAnnotations();
				typeAnnotations.computeIfAbsent(JvmClassMappingKt.getJavaClass((KClass<?>) it.getClassifier()), c -> new ArrayList<>()).addAll(annotations);
			}

			List<AnnotatedType> javaTypes = new ArrayList<>();

			AnnotatedType superclass = clazz.getAnnotatedSuperclass();

			if (superclass != null) javaTypes.add(superclass);

			Collections.addAll(javaTypes, clazz.getAnnotatedInterfaces());

			for (AnnotatedType it : javaTypes) {
				Collections.addAll(typeAnnotations.computeIfAbsent((Class<?>) it.getType(), c -> new ArrayList<>()), it.getAnnotations());
			}

			List<EAnnotatedType<?>> result = new ArrayList<>();

			for (var it : typeAnnotations.entrySet()) {
				List<EAnnotation> annotations = new ArrayList<>();

				for (Annotation annotation : it.getValue()) annotations.add(asEAnnotation(annotation));

				result.add(new EAnnotatedType<>(accessor.getClassDecl(ClassName.byClass(it.getKey())), annotations));
			}

			return result;
		}
	}
}
