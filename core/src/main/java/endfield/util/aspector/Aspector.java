package endfield.util.aspector;

import endfield.util.aspector.classes.EAnnotatedType;
import endfield.util.aspector.classes.ArrayValue;
import endfield.util.aspector.classes.ClassDecl;
import endfield.util.aspector.classes.ClassName;
import endfield.util.aspector.classes.EAnnotation;
import endfield.util.aspector.classes.EAspectMethod;
import endfield.util.aspector.classes.EConstructor;
import endfield.util.aspector.classes.EField;
import endfield.util.aspector.classes.EMethod;
import endfield.util.aspector.classes.EnumValue;
import endfield.util.aspector.classes.TypeValue;
import endfield.util.aspector.generate.AspectFactory;
import endfield.util.aspector.generate.AspectFactory.AspectResult;
import kotlin.Pair;
import kotlin.collections.ArraysKt;
import kotlin.collections.CollectionsKt;
import kotlin.collections.MapsKt;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Aspector {
	public static final ClassName aspectExtendsT = ClassName.byClass(AspectExtends.class);
	public static final ClassName aspectElementT = ClassName.byClass(AspectElement.class);
	public static final ClassName stubT = ClassName.byClass(Stub.class);
	public static final ClassName sharedT = ClassName.byClass(Shared.class);

	AspectFactory aspectFactory;

	public Aspector(AspectFactory factory) {
		aspectFactory = factory;
	}

	public <T> AspectDecl<T> applyAspect(ClassDecl<T> targetClass, ClassDecl<?>... aspectClasses) {
		List<List<ClassDecl<?>>> aspectLayers = new ArrayList<>();

		List<Pair<ClassDecl<?>, Integer>> queue = CollectionsKt.toMutableList(ArraysKt.map(aspectClasses, it -> new Pair<>(it, 0)));
		Set<ClassDecl<?>> solved = new HashSet<>();

		while (!queue.isEmpty()) {
			Pair<ClassDecl<?>, Integer> curr = queue.remove(0);
			ClassDecl<?> clazz = curr.getFirst();
			int depth = curr.getSecond();

			if (solved.add(clazz)) {
				while (aspectLayers.size() <= depth) aspectLayers.add(new ArrayList<>());
				aspectLayers.get(depth).add(clazz);

				EAnnotation annotation = clazz.getAnnotation(aspectExtendsT);
				ArrayValue<Class<?>, TypeValue> value = annotation == null ? null : annotation.getValue("extend");
				List<ClassName> extensions = value == null ? null : CollectionsKt.map(value.rawValue(), it -> it.rawValue());

				if (extensions != null) {
					for (ClassName extension : extensions) {
						ClassDecl<Object> classDecl = aspectFactory.classAccessor.getClassDecl(extension);
						queue.add(new Pair<>(classDecl, depth + 1));
					}
				}
			}
		}

		List<ClassDecl<?>> flat = CollectionsKt.flatMap(aspectLayers, it -> it);
		checkAspectDeclare(targetClass, flat);
		aspectFactory.checkAspectable(targetClass, flat);

		AspectResult<T> aspectDecl = aspectFactory.makeClass(targetClass, b -> {
			Map<ClassDecl<?>, EAnnotation> stub = MapsKt.toMap(CollectionsKt.flatMap(flat, i -> CollectionsKt.mapNotNull(CollectionsKt.plus(CollectionsKt.listOfNotNull(i.annotatedSuperClass()), i.annotatedInterfaces()), it -> {
				EAnnotation stub1 = CollectionsKt.firstOrNull(it.annotations(), a -> a.type.equals(stubT));
				return stub1 == null ? null : new Pair<>(it.type(), stub1);
			})));
			Set<ClassDecl<?>> nonStubInterfaces = CollectionsKt.toSet(CollectionsKt.flatMap(flat, i -> CollectionsKt.map(CollectionsKt.filter(i.annotatedInterfaces(), it -> it.getAnnotation(stubT) == null), it -> it.type())));

			for (var entry : stub.entrySet()) {
				b.registerStubSpec(
						entry.getKey().name,
						((TypeValue) entry.getValue().getValue("attacheTo")).rawValue()
				);
			}

			for (ClassDecl<?> it : nonStubInterfaces) {
				b.registerInterfaces(it.name);
			}

			for (ClassDecl<?> decl : flat) {
				for (EField field : decl.fields()) {
					if (field.getAnnotation(sharedT) != null) {
						b.registerSharedField(field);
					} else {
						b.registerDeclField(field);
					}
				}

				for (EMethod method : CollectionsKt.filter(decl.methods(), method -> Modifier.isPrivate(method.flags)
						|| Modifier.isStatic(method.flags)
						|| Modifier.isFinal(method.flags))) {
					b.registerDeclMethod(method);
				}

				for (EConstructor<?> constructor : decl.constructors()) {
					b.registerDeclConstructor(constructor);
				}
			}

			for (int layer = 0; layer < aspectLayers.size(); layer++) {
				List<ClassDecl<?>> decls = aspectLayers.get(layer);

				for (ClassDecl<?> decl : decls) {
					for (Pair<EMethod, Using> entry : CollectionsKt.map(CollectionsKt.filter(decl.methods(), it -> !Modifier.isPrivate(it.flags)
							&& !Modifier.isStatic(it.flags)
							&& !Modifier.isFinal(it.flags)), it -> {
						EAnnotation anno = it.getAnnotation(aspectElementT);

						if (anno != null) {
							EnumValue<Using> using = anno.getValue("using");

							if (using != null) return new Pair<>(it, using.value());
						}

						return new Pair<>(it, Using.OVERRIDE);
					})) {
						EMethod method = entry.getFirst();
						Using using = entry.getSecond();

						b.registerAspectMethod(layer, new EAspectMethod(method.declaring, method.name, method.parameters, method.annotatedReturnType, method.flags, using, method.annotations));
					}
				}
			}
		}, flat.toArray(new ClassDecl[0]));

		return aspectDecl;
	}

	void checkAspectDeclare(ClassDecl<?> targetClass, List<ClassDecl<?>> aspectClasses) {
		if (targetClass.isPrimitive() || targetClass.isEnum() || targetClass.isArray() || targetClass.isInterface())
			throw new IllegalArgumentException("Source class " + targetClass.name + " must be a normal class");

		for (ClassDecl<?> decl : aspectClasses) {
			if (decl.isPrimitive() || decl.isEnum() || decl.isArray() || decl.isInterface())
				throw new IllegalArgumentException("Aspect implement class " + decl.name + " must be a normal class");

			EAnnotatedType<?> superClass = decl.annotatedSuperClass();
			if (superClass != null && superClass.type().name.equals(ClassName.jObject) && !CollectionsKt.any(superClass.annotations(), a -> a.type.equals(stubT)))
				throw new IllegalArgumentException("Super class of aspect implement must be annotated by @Stub");
		}
	}
}
