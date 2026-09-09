package endfield.util.aspector.classes;

import endfield.util.Collections2;
import endfield.util.Constant;
import endfield.util.IntMap2;
import kotlin.Metadata;
import kotlin.collections.CollectionsKt;
import kotlin.collections.MapsKt;
import kotlin.io.FilesKt;
import kotlin.metadata.KmAnnotation;
import kotlin.metadata.KmAnnotationArgument;
import kotlin.metadata.KmClass;
import kotlin.metadata.KmClassifier;
import kotlin.metadata.KmFunction;
import kotlin.metadata.KmProperty;
import kotlin.metadata.KmType;
import kotlin.metadata.jvm.JvmExtensionsKt;
import kotlin.metadata.jvm.JvmFieldSignature;
import kotlin.metadata.jvm.JvmMethodSignature;
import kotlin.metadata.jvm.KotlinClassMetadata;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.TypeReference;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TypeAnnotationNode;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.jar.JarFile;
import java.util.zip.ZipFile;

public class ASMClassAccessor implements ClassAccessor {
	ClassLoader loaderPath;
	@Nullable File filePath;

	Map<ClassName, ClassDecl<?>> loadedDeclMap;

	public ASMClassAccessor(Object... paths) {
		for (Object path : paths) {
			if (path instanceof ClassLoader loader) {
				loaderPath = loader;
			} else if (path instanceof File file) {
				filePath = file;
			}
		}

		if (loaderPath == null) loaderPath = getClass().getClassLoader();

		loadedDeclMap = new HashMap<>();
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
					ClassDecl<T> componentType = getClassDecl(name.componentName());
					return new ArrayClassDecl<>(componentType);
				}

				byte[] bytecode = getBytes(name);
				return new BytecodeClassDecl<>(name, this, bytecode);
			});
		};
	}

	@Override
	public byte[] getBytes(ClassName className) {
		String path = className.internalName() + ".class";

		InputStream stream = loaderPath.getResourceAsStream(path);

		if (stream == null && filePath != null) {
			if (filePath.isDirectory()) {
				File target = new File(filePath, path);

				if (target.exists()) {
					try {
						stream = new FileInputStream(target);
					} catch (FileNotFoundException e) {
						throw new RuntimeException(e);
					}
				}
			} else {
				String extension = FilesKt.getExtension(filePath);

				if (extension.equals(".jar") || extension.equals(".zip")) {
					try (ZipFile zip = extension.equals(".jar") ? new JarFile(filePath.getAbsolutePath()) : new ZipFile(filePath.getAbsolutePath())) {
						stream = zip.getInputStream(zip.getEntry(path));
					} catch (IOException e) {
						throw new RuntimeException(e);
					}
				}
			}
		}
		if (stream == null) throw new RuntimeException(className + " not found in any paths");

		try (InputStream inputStream = stream) {
			return inputStream.readAllBytes();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	static class ArrayClassDecl<T> extends ClassDecl<T> {
		public ArrayClassDecl(ClassDecl<T> classDecl) {
			super(classDecl.name);
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
			return List.of(new EField(this,
					"length",
					new AnnotatedType<>(ClassAccessor.intDecl, List.of()),
					Modifier.PUBLIC | Modifier.FINAL,
					null,
					List.of()));
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

	static class BytecodeClassDecl<T> extends ClassDecl<T> {
		static ClassName metadataT = ClassName.byClass(Metadata.class);

		ClassAccessor accessor;
		byte[] bytecode;

		boolean initialized;

		int flags;
		ClassDecl<?> superClass;
		AnnotatedType<?> annotatedSuperClass;
		List<ClassDecl<?>> interfaces;
		List<AnnotatedType<?>> annotatedInterfaces;
		List<EAnnotation> annotations;
		List<EField> fields;
		List<EMethod> methods;
		List<EConstructor<T>> constructors;

		public BytecodeClassDecl(ClassName name, ClassAccessor access, byte[] code) {
			super(name);

			accessor = access;
			bytecode = code;
		}

		@Override
		public int flags() {
			return flags;
		}

		@Override
		public ClassDecl<?> superClass() {
			initialize();
			return superClass;
		}

		@Override
		public AnnotatedType<?> annotatedSuperClass() {
			initialize();
			return annotatedSuperClass;
		}

		@Override
		public List<ClassDecl<?>> interfaces() {
			initialize();
			return interfaces;
		}

		@Override
		public List<AnnotatedType<?>> annotatedInterfaces() {
			initialize();
			return annotatedInterfaces;
		}

		@Override
		public List<EAnnotation> annotations() {
			initialize();
			return annotations;
		}

		@Override
		public List<EField> fields() {
			initialize();
			return fields;
		}

		@Override
		public List<EMethod> methods() {
			initialize();
			return methods;
		}

		@Override
		public List<EConstructor<T>> constructors() {
			initialize();
			return constructors;
		}

		void initialize() {
			if (initialized) return;

			fields = new ArrayList<>();
			methods = new ArrayList<>();
			constructors = new ArrayList<>();

			ClassReader classReader = new ClassReader(bytecode);
			ClassNode classRoot = new ClassNode(Opcodes.ASM9);
			classReader.accept(classRoot, ClassReader.SKIP_CODE | ClassReader.SKIP_FRAMES);

			annotations = handleAnnotations(Collections2.plus(classRoot.visibleAnnotations, classRoot.invisibleAnnotations));
			EAnnotation ktMetadata = CollectionsKt.firstOrNull(annotations, it -> it.type.equals(metadataT));
			Metadata metadata = ktMetadata == null ? null : new Metadata() {
				@Override
				public Class<? extends Annotation> annotationType() {
					return Metadata.class;
				}

				@Override
				public int xi() {
					return 0;
				}

				@Override
				public String pn() {
					Value<String> value = ktMetadata.getValue("pn");
					return value == null ? "" : value.value;
				}

				@Override
				public String xs() {
					Value<String> value = ktMetadata.getValue("xs");
					return value == null ? "" : value.value;
				}

				@Override
				public String[] d2() {
					ArrayValue<String, Value<String>> value = ktMetadata.getValue("d2");
					return value == null ? Constant.EMPTY_STRING : value.value().toArray(Constant.EMPTY_STRING);
				}

				@Override
				public String[] d1() {
					ArrayValue<String, Value<String>> value = ktMetadata.getValue("d1");
					return value == null ? Constant.EMPTY_STRING : value.value().toArray(Constant.EMPTY_STRING);
				}

				@Override
				public int[] bv() {
					return new int[]{1, 0, 3};
				}

				@Override
				public int[] mv() {
					Value<int[]> value = ktMetadata.getValue("mv");
					return value == null ? new int[0] : value.value;
				}

				@Override
				public int k() {
					Value<Integer> value = ktMetadata.getValue("k");
					return value == null ? 1 : value.value;
				}
			};
			KmClass kmClass = null;
			if (metadata != null) {
				KotlinClassMetadata.Class md = (KotlinClassMetadata.Class) KotlinClassMetadata.readLenient(metadata);
				kmClass = md.getKmClass();
			}

			IntMap2<List<EAnnotation>> typeRefAnnoMap = handleTypeAnnotations(Collections2.plus(classRoot.visibleTypeAnnotations, classRoot.invisibleTypeAnnotations));
			if (kmClass != null) {
				for (KmType kmType : kmClass.getSupertypes()) {
					ClassName className;
					KmClassifier cf = kmType.classifier;
					if (cf instanceof KmClassifier.Class c) {
						className = ClassName.byName(c.getName());
					} else {
						className = ClassName.V;
					}

					int index = className.internalName().equals(classRoot.superName) ? -1 : CollectionsKt.indexOfFirst(classRoot.interfaces, it -> it.equals(className.internalName()));

					for (KmAnnotation kmAnnotation : JvmExtensionsKt.getAnnotations(kmType)) {
						EAnnotation annotation = handleKmAnnotation(kmAnnotation);
						typeRefAnnoMap.get(TypeReference.newSuperTypeReference(index).getValue(), () -> new ArrayList<>()).add(annotation);
					}
				}
			}

			flags = classRoot.access;
			superClass = classRoot.superName == null ? accessor.getClassDecl(ClassName.jObject) : accessor.getClassDecl(ClassName.byInternalName(classRoot.superName));
			annotatedSuperClass = new AnnotatedType<>(
					superClass,
					typeRefAnnoMap.get(TypeReference.newSuperTypeReference(-1).getValue(), () -> new ArrayList<>())
			);
			interfaces = CollectionsKt.map(CollectionsKt.toList(classRoot.interfaces), it -> accessor.getClassDecl(ClassName.byInternalName(it)));
			annotatedInterfaces = CollectionsKt.mapIndexed(interfaces, (i, t) -> new AnnotatedType<>(
					t,
					typeRefAnnoMap.get(TypeReference.newSuperTypeReference(i).getValue(), () -> new ArrayList<>())
			));

			Map<String, IntMap2<List<EAnnotation>>> kmFieldAnnoRef = new HashMap<>();
			Map<MethodSignature, IntMap2<List<EAnnotation>>> kmMethodAnnoRef = new HashMap<>();
			if (kmClass != null) {
				for (KmProperty property : kmClass.getProperties()) {
					JvmFieldSignature fieldSignature = JvmExtensionsKt.getFieldSignature(property);
					if (fieldSignature != null) {
						String name = fieldSignature.getName();
						KmType type = property.returnType;
						kmFieldAnnoRef.computeIfAbsent(name, s -> new IntMap2<>(List.class))
								.get(TypeReference.newTypeReference(TypeReference.FIELD).getValue(), () -> new ArrayList<>())
								.addAll(CollectionsKt.map(JvmExtensionsKt.getAnnotations(type), it -> handleKmAnnotation(it)));
					}
					JvmMethodSignature getterSignature = JvmExtensionsKt.getGetterSignature(property);
					if (getterSignature != null) {
						MethodSignature signature = MethodSignature.parse(getterSignature.getName(), getterSignature.getDescriptor());
						kmMethodAnnoRef.computeIfAbsent(signature, s -> new IntMap2<>(List.class))
								.get(TypeReference.newTypeReference(TypeReference.METHOD_RETURN).getValue(), () -> new ArrayList<>())
								.addAll(CollectionsKt.map(JvmExtensionsKt.getAnnotations(property.returnType), it -> handleKmAnnotation(it)));
					}
					JvmMethodSignature setterSignature = JvmExtensionsKt.getSetterSignature(property);
					if (setterSignature != null) {
						MethodSignature signature = MethodSignature.parse(setterSignature.getName(), setterSignature.getDescriptor());
						kmMethodAnnoRef.computeIfAbsent(signature, s -> new IntMap2<>(List.class))
								.get(TypeReference.newFormalParameterReference(0).getValue(), () -> new ArrayList<>())
								.addAll(CollectionsKt.map(JvmExtensionsKt.getAnnotations(property.returnType), it -> handleKmAnnotation(it)));
					}
				}
			}
			for (FieldNode field : classRoot.fields) {
				IntMap2<List<EAnnotation>> typeRefAnnoMap2 = handleTypeAnnotations(Collections2.plus(field.visibleTypeAnnotations, field.invisibleTypeAnnotations));

				IntMap2<List<EAnnotation>> refs = kmFieldAnnoRef.get(field.name);
				for (var entry : refs) {
					typeRefAnnoMap2.get(entry.key, i -> new ArrayList<>()).addAll(entry.value);
				}

				fields.add(new EField(
						this,
						field.name,
						new AnnotatedType<>(
								accessor.getClassDecl(ClassName.byDescriptor(field.desc)),
								typeRefAnnoMap2.get(TypeReference.newTypeReference(TypeReference.FIELD).getValue(), () -> new ArrayList<>())
						),
						field.access,
						field.value,
						handleAnnotations(Collections2.plus(classRoot.visibleAnnotations, classRoot.invisibleAnnotations))
				));
			}

			if (kmClass != null) {
				for (KmFunction function : kmClass.getFunctions()) {
					JvmMethodSignature funcSign = JvmExtensionsKt.getSignature(function);

					if (funcSign == null) continue;

					MethodSignature sign = MethodSignature.parse(funcSign.getName(), funcSign.getDescriptor());
					IntMap2<List<EAnnotation>> map = kmMethodAnnoRef.computeIfAbsent(sign, s -> new IntMap2<>(List.class));

					for (KmAnnotation kmAnnotation : JvmExtensionsKt.getAnnotations(function.returnType)) {
						EAnnotation annotation = handleKmAnnotation(kmAnnotation);
						map.get(TypeReference.newTypeReference(TypeReference.METHOD_RETURN).getValue(), () -> new ArrayList<>()).add(annotation);
					}
					List<KmType> types = Collections2.plus(CollectionsKt.listOfNotNull(function.getReceiverParameterType()),
							CollectionsKt.map(function.getValueParameters(), it -> it.type));
					for (int i = 0; i < types.size(); i++) {
						KmType type = types.get(i);
						List<KmAnnotation> annotations = JvmExtensionsKt.getAnnotations(type);
						map.get(TypeReference.newFormalParameterReference(i).getValue(), () -> new ArrayList<>())
								.addAll(CollectionsKt.map(annotations, it -> handleKmAnnotation(it)));
					}
				}
			}
			for (MethodNode method : classRoot.methods) {
				MethodSignature signature = MethodSignature.parse(method.name, method.desc);
				IntMap2<List<EAnnotation>> typeRefAnnoMap2 = handleTypeAnnotations(Collections2.plus(method.visibleTypeAnnotations, method.invisibleTypeAnnotations));

				@SuppressWarnings("unchecked")
				List<EAnnotation>[] paramAnnotations = new List[signature.paramTypes.size()];

				for (int i = 0; i < paramAnnotations.length; i++) paramAnnotations[i] = new ArrayList<>();

				if (method.visibleParameterAnnotations != null) {
					for (int i = 0; i < method.visibleParameterAnnotations.length; i++) {
						List<AnnotationNode> annotations = method.visibleParameterAnnotations[i];
						paramAnnotations[i].addAll(handleAnnotations(annotations));
					}
				}
				if (method.invisibleParameterAnnotations != null) {
					for (int i = 0; i < method.invisibleParameterAnnotations.length; i++) {
						List<AnnotationNode> annotations = method.invisibleParameterAnnotations[i];
						paramAnnotations[i].addAll(handleAnnotations(annotations));
					}
				}

				IntMap2<List<EAnnotation>> refs = kmMethodAnnoRef.get(signature);
				if (refs != null) {
					for (var entry : refs) {
						typeRefAnnoMap2.get(entry.key, () -> new ArrayList<>()).addAll(entry.value);
					}
				}

				List<String> paramNames = method.parameters == null ? null : CollectionsKt.map(method.parameters, it -> it.name);
				List<EParameter> params = new ArrayList<>(signature.paramTypes.size());
				for (int i = 0; i < signature.paramTypes.size(); i++) {
					String paramName = paramNames == null ? null : paramNames.get(i);
					params.add(new EParameter(
							paramName == null ? "arg" + i : paramName,
							new AnnotatedType<>(
									accessor.getClassDecl(signature.paramTypes.get(i)),
									typeRefAnnoMap2.get(TypeReference.newFormalParameterReference(i).getValue(), () -> new ArrayList<>())
							),
							CollectionsKt.toList(paramAnnotations[i])
					));
				}

				if (!method.name.equals("<init>")) {
					methods.add(new EMethod(
							this,
							method.name,
							params,
							new AnnotatedType<>(
									accessor.getClassDecl(signature.returnType),
									typeRefAnnoMap2.get(TypeReference.newTypeReference(TypeReference.METHOD_RETURN).getValue(), () -> new ArrayList<>())
							),
							method.access,
							handleAnnotations(Collections2.plus(method.visibleAnnotations, method.invisibleAnnotations))
					));
				} else {
					constructors.add(new EConstructor<>(
							this,
							params,
							method.access,
							handleAnnotations(Collections2.plus(method.visibleAnnotations, method.invisibleAnnotations))
					));
				}
			}

			initialized = true;
		}

		static IntMap2<List<EAnnotation>> handleTypeAnnotations(List<TypeAnnotationNode> nodes) {
			IntMap2<List<EAnnotation>> typeRefAnnoMap = new IntMap2<>(List.class);
			for (TypeAnnotationNode annotation : nodes) {
				int ref = annotation.typeRef;
				typeRefAnnoMap.get(ref, i -> new ArrayList<>()).add(handleAnnotation(annotation));
			}

			return typeRefAnnoMap;
		}

		static List<EAnnotation> handleAnnotations(List<AnnotationNode> nodes) {
			return CollectionsKt.map(nodes, it -> handleAnnotation(it));
		}

		static EAnnotation handleAnnotation(AnnotationNode node) {
			ClassName annotationName = ClassName.byDescriptor(node.desc);

			Map<String, AnnotationValue<?, ?>> annoValues = new HashMap<>();
			List<Object> valueList = node.values == null ? new ArrayList<>() : node.values;
			for (int i = 0; i < valueList.size(); i += 2) {
				String name = (String) valueList.get(i);
				Object raw = valueList.get(i + 1);
				AnnotationValue<?, ?> value = handleAnnotationValue(raw);

				annoValues.put(name, value);
			}

			return new EAnnotation(annotationName, MapsKt.toMap(annoValues));
		}

		static EAnnotation handleKmAnnotation(KmAnnotation kmAnnotation) {
			ClassName annotationName = ClassName.byInternalName(kmAnnotation.getClassName());

			Map<String, AnnotationValue<?, ?>> annoValues = new HashMap<>();
			for (var arg : kmAnnotation.getArguments().entrySet()) {
				String name = arg.getKey();
				KmAnnotationArgument raw = arg.getValue();
				AnnotationValue<?, ?> value = handleKmAnnoArg(raw);

				annoValues.put(name, value);
			}

			return new EAnnotation(annotationName, MapsKt.toMap(annoValues));
		}

		static AnnotationValue<?, ?> handleKmAnnoArg(KmAnnotationArgument argument) {
			if (argument instanceof KmAnnotationArgument.LiteralValue<?> value) {
				return new Value<>(value.getValue());
			} else if (argument instanceof KmAnnotationArgument.EnumValue value) {
				return new EnumValue<>(ClassName.byName(value.getEnumClassName()), value.getEnumEntryName());
			} else if (argument instanceof KmAnnotationArgument.KClassValue value) {
				return new TypeValue(ClassName.byName(value.getClassName()));
			} else if (argument instanceof KmAnnotationArgument.AnnotationValue value) {
				return new NestedAnnotationValue(handleKmAnnotation(value.getAnnotation()));
			} else if (argument instanceof KmAnnotationArgument.ArrayValue value) {
				KmAnnotationArgument arg = value.getElements().get(0);

				if (arg instanceof KmAnnotationArgument.ByteValue) {
					return new Value<>(CollectionsKt.toByteArray(CollectionsKt.map(value.getElements(), it -> ((KmAnnotationArgument.ByteValue) it).getValue())));
				} else if (arg instanceof KmAnnotationArgument.ShortValue) {
					return new Value<>(CollectionsKt.toShortArray(CollectionsKt.map(value.getElements(), it -> ((KmAnnotationArgument.ShortValue) it).getValue())));
				} else if (arg instanceof KmAnnotationArgument.IntValue) {
					return new Value<>(CollectionsKt.toIntArray(CollectionsKt.map(value.getElements(), it -> ((KmAnnotationArgument.IntValue) it).getValue())));
				} else if (arg instanceof KmAnnotationArgument.LongValue) {
					return new Value<>(CollectionsKt.toLongArray(CollectionsKt.map(value.getElements(), it -> ((KmAnnotationArgument.LongValue) it).getValue())));
				} else if (arg instanceof KmAnnotationArgument.FloatValue) {
					return new Value<>(CollectionsKt.toFloatArray(CollectionsKt.map(value.getElements(), it -> ((KmAnnotationArgument.FloatValue) it).getValue())));
				} else if (arg instanceof KmAnnotationArgument.DoubleValue) {
					return new Value<>(CollectionsKt.toDoubleArray(CollectionsKt.map(value.getElements(), it -> ((KmAnnotationArgument.DoubleValue) it).getValue())));
				} else if (arg instanceof KmAnnotationArgument.BooleanValue) {
					return new Value<>(CollectionsKt.toBooleanArray(CollectionsKt.map(value.getElements(), it -> ((KmAnnotationArgument.BooleanValue) it).getValue())));
				} else if (arg instanceof KmAnnotationArgument.CharValue) {
					return new Value<>(CollectionsKt.toCharArray(CollectionsKt.map(value.getElements(), it -> ((KmAnnotationArgument.CharValue) it).getValue())));
				} else {
					return new ArrayValue<>(CollectionsKt.map(value.getElements(), it -> handleKmAnnoArg(it)));
				}
			} else if (argument instanceof KmAnnotationArgument.ArrayKClassValue value) {
				ClassName c = ClassName.byName(value.getClassName());
				for (int n = 0; n < value.getArrayDimensionCount(); n++) {
					c = c.arrayName();
				}
				return new TypeValue(c);
			} else {
				throw new AssertionError();
			}
		}

		@SuppressWarnings("unchecked")
		static AnnotationValue<?, ?> handleAnnotationValue(Object raw) {
			if (raw instanceof Type type) {
				return new TypeValue(ClassName.byInternalName(type.getInternalName()));
			} else if (raw instanceof Object[] array) {
				return new EnumValue<>(ClassName.byDescriptor((String) array[0]), (String) array[1]);
			} else if (raw instanceof List<?> list) {
				Object value = list.isEmpty() ? null : list.get(0);
				if (value == null) {
					return new ArrayValue<>(List.of());
				} else if (value instanceof Byte) {
					return new Value<>(CollectionsKt.toByteArray((Collection<Byte>) raw));
				} else if (value instanceof Short) {
					return new Value<>(CollectionsKt.toShortArray((Collection<Short>) raw));
				} else if (value instanceof Integer) {
					return new Value<>(CollectionsKt.toIntArray((Collection<Integer>) raw));
				} else if (value instanceof Long) {
					return new Value<>(CollectionsKt.toLongArray((Collection<Long>) raw));
				} else if (value instanceof Float) {
					return new Value<>(CollectionsKt.toFloatArray((Collection<Float>) raw));
				} else if (value instanceof Double) {
					return new Value<>(CollectionsKt.toDoubleArray((Collection<Double>) raw));
				} else if (value instanceof Boolean) {
					return new Value<>(CollectionsKt.toBooleanArray((Collection<Boolean>) raw));
				} else if (value instanceof Character) {
					return new Value<>(CollectionsKt.toCharArray((Collection<Character>) raw));
				} else {
					return new ArrayValue<>(CollectionsKt.map(list, it -> handleAnnotationValue(it)));
				}
			} else if (raw instanceof AnnotationNode node) {
				return new NestedAnnotationValue(handleAnnotation(node));
			} else {
				return new Value<>(raw);
			}
		}
	}
}


