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
import endfield.util.holder.ObjectIntHolder;
import kotlin.collections.CollectionsKt;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Aspector {
	AspectFactory aspectFactory;

	public Aspector(AspectFactory factory) {
		aspectFactory = factory;
	}

	public <T> AspectDecl<T> applyAspect(ClassDecl<T> targetClass, ClassDecl<?>... aspectClasses) {
		List<List<ClassDecl<?>>> aspectLayers = new ArrayList<>();

		List<ObjectIntHolder<ClassDecl<?>>> queue = new ArrayList<>(aspectClasses.length);
		for (ClassDecl<?> it : aspectClasses) queue.add(new ObjectIntHolder<>(it, 0));

		Set<ClassDecl<?>> solved = new HashSet<>();

		while (!queue.isEmpty()) {
			ObjectIntHolder<ClassDecl<?>> curr = queue.remove(0);
			ClassDecl<?> clazz = curr.key;
			int depth = curr.value;

			if (solved.add(clazz)) {
				while (aspectLayers.size() <= depth) aspectLayers.add(new ArrayList<>());
				aspectLayers.get(depth).add(clazz);

				EAnnotation annotation = clazz.getAnnotation(ClassName.byClass(AspectExtends.class));

				if (annotation != null) {
					ArrayValue<Class<?>, TypeValue> value = annotation.getValue("extend");

					if (value != null) {
						for (TypeValue it : value.rawValue()) {
							ClassName extension = it.rawValue();
							ClassDecl<?> classDecl = aspectFactory.classAccessor.getClassDecl(extension);
							queue.add(new ObjectIntHolder<>(classDecl, depth + 1));
						}
					}
				}
			}
		}

		List<ClassDecl<?>> flat = new ArrayList<>();
		for (List<ClassDecl<?>> it : aspectLayers) flat.addAll(it);

		checkAspectDeclare(targetClass, flat);
		aspectFactory.checkAspectable(targetClass, flat);

		AspectResult<T> aspectDecl = aspectFactory.makeClass(targetClass, b -> {
			Map<ClassDecl<?>, EAnnotation> stub = new HashMap<>();
			Set<ClassDecl<?>> nonStubInterfaces = new HashSet<>();
			for (ClassDecl<?> decl : flat) {
				EAnnotatedType<?> annotatedSuperClass = decl.annotatedSuperClass();
				if (annotatedSuperClass != null) {
					EAnnotation annotation = annotatedSuperClass.getAnnotation(ClassName.byClass(Stub.class));
					if (annotation != null) stub.put(annotatedSuperClass.type(), annotation);
				}

				for (EAnnotatedType<?> it : decl.annotatedInterfaces()) {
					EAnnotation annotation = it.getAnnotation(ClassName.byClass(Stub.class));
					if (annotation == null) nonStubInterfaces.add(it.type());
					else stub.put(it.type(), annotation);
				}
			}

			for (var entry : stub.entrySet()) {
				b.registerStubSpec(
						entry.getKey().name,
						entry.getValue().<TypeValue>getValue("attacheTo").rawValue()
				);
			}

			for (ClassDecl<?> it : nonStubInterfaces) {
				b.registerInterfaces(it.name);
			}

			for (ClassDecl<?> decl : flat) {
				for (EField field : decl.fields()) {
					if (field.getAnnotation(ClassName.byClass(Shared.class)) != null) {
						b.registerSharedField(field);
					} else {
						b.registerDeclField(field);
					}
				}

				for (EMethod method : decl.methods()) {
					if ((method.flags & (Modifier.PRIVATE | Modifier.STATIC | Modifier.FINAL)) != 0) continue;

					b.registerDeclMethod(method);
				}

				for (EConstructor<?> constructor : decl.constructors()) {
					b.registerDeclConstructor(constructor);
				}
			}

			for (int i = 0; i < aspectLayers.size(); i++) {
				List<ClassDecl<?>> decls = aspectLayers.get(i);

				for (ClassDecl<?> decl : decls) {
					for (EMethod method : decl.methods()) {
						if ((method.flags & (Modifier.PRIVATE | Modifier.STATIC | Modifier.FINAL)) != 0) continue;

						EAnnotation anno = method.getAnnotation(ClassName.byClass(AspectElement.class));

						if (anno != null) {
							EnumValue<Using> usingValue = anno.getValue("using");
							Using using = usingValue == null ? Using.OVERRIDE : usingValue.value();

							b.registerAspectMethod(i, new EAspectMethod(method.declaring, method.name, method.parameters, method.annotatedReturnType, method.flags, using, method.annotations));
						}
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
			if (superClass != null && superClass.type().name.equals(ClassName.OBJECT) && !CollectionsKt.any(superClass.annotations(), a -> a.type.equals(ClassName.byClass(Stub.class))))
				throw new IllegalArgumentException("Super class of aspect implement must be annotated by @Stub");
		}
	}
}
