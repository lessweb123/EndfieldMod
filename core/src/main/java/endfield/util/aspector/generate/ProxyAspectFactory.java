package endfield.util.aspector.generate;

import endfield.util.Constant;
import endfield.util.aspector.Using;
import endfield.util.aspector.classes.BytecodeLoader;
import endfield.util.aspector.classes.ClassAccessor;
import endfield.util.aspector.classes.ClassDecl;
import endfield.util.aspector.classes.ClassName;
import endfield.util.aspector.classes.EAspectMethod;
import endfield.util.aspector.classes.EConstructor;
import endfield.util.aspector.classes.EField;
import endfield.util.aspector.classes.EMethod;
import endfield.util.aspector.classes.MethodSignature;
import kotlin.collections.CollectionsKt;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static endfield.util.Objects2.let;

public class ProxyAspectFactory extends AspectFactory {
	public ProxyAspectFactory(ClassAccessor accessor) {
		super(accessor);
	}

	@Override
	public ClassName generateClassName(ClassDecl<?> targetClass, List<ClassDecl<?>> aspectClasses) {
		String name = targetClass.name.name();

		if (name.startsWith("java.")) name = name.replace("java.", "java_.");
		else if (name.startsWith("javax.")) name = name.replace("javax.", "javax_.");
		else if (name.startsWith("sun.")) name = name.replace("sun.", "sun_.");
		else if (name.startsWith("jdk.")) name = name.replace("jdk.", "jdk_.");
		else if (name.startsWith("android.")) name = name.replace("android.", "android_.");
		else if (name.startsWith("androidx.")) name = name.replace("androidx.", "androidx_.");
		else if (name.startsWith("dalvik.")) name = name.replace("dalvik.", "dalvik_.");
		else if (name.startsWith("libcore.")) name = name.replace("libcore.", "libcore_.");
		else if (name.startsWith("com.sun.")) name = name.replace("com.sun.", "com.sun_.");
		else if (name.startsWith("com.android.")) name = name.replace("com.android.", "com.android_.");

		int hash = 1;
		for (ClassDecl<?> it : aspectClasses) {
			hash = 31 * hash + it.name.hashCode();
		}
		return ClassName.byName(name + "$" + Integer.toHexString(hash));
	}

	@Override
	public byte[] generateBytecode(AspectBuilder builder) {
		ClassName thisClass = builder.className;
		int accessFlags = builder.accessFlags;
		Map<ClassName, ClassName> stubAttaches = builder.stubAttaches;
		ClassName superClass = builder.superClass;
		List<ClassName> aspectDecl = builder.aspectDecl;
		List<ClassName> interfaces = builder.interfaces;

		byte[] bs = classAccessor.getBytes(superClass);
		ClassReader cr = new ClassReader(bs);
		ClassNode superNode = new ClassNode(Opcodes.ASM9);
		cr.accept(superNode, ClassReader.SKIP_DEBUG);

		List<MethodNode> superConstructors = new ArrayList<>();
		for (MethodNode it : superNode.methods) {
			if (it.name.equals("<init>") && (it.access & Opcodes.ACC_PRIVATE) == 0) superConstructors.add(it);
		}

		Map<ClassName, Map<MethodSignature, MethodNode>> declMethods = new HashMap<>();
		for (ClassName decl : aspectDecl) {
			byte[] bytecode = classAccessor.getBytes(decl);

			ClassReader reader = new ClassReader(bytecode);
			ClassNode implRoot = new ClassNode(Opcodes.ASM9);
			reader.accept(implRoot, ClassReader.SKIP_DEBUG);

			Map<MethodSignature, MethodNode> map = new HashMap<>();

			for (MethodNode m : implRoot.methods) map.put(MethodSignature.parse(m.name, m.desc), m);

			declMethods.put(decl, map);
		}

		Map<MethodSignature, List<AspectFactory.MethodUsing>> aspectElements = builder.aspectMethods;

		Map<String, EField> sharedFields = builder.sharedFields;
		Map<String, List<EField>> fields = builder.declFields;
		Map<MethodSignature, List<EMethod>> methods = builder.declMethods;

		List<EConstructor<?>> constructors = let(builder.declConstructors, map -> {
			MethodSignature allowedSignature = MethodSignature.parse("<init>", "()V");

			for (var e : map.entrySet()) {
				if (!allowedSignature.equals(e.getKey())) throw new IllegalArgumentException("Constructors with parameters cannot exist in the context of Aspect declare. decl: " + e.getValue().get(0).declaring);
			}

			List<EConstructor<?>> get = map.get(allowedSignature);
			return get == null ? new ArrayList<>() : get;
		});

		Map<MethodSignature, Map<ClassName, MethodSignature>> methodMapping = new HashMap<>(methods.size());
		for (var e : methods.entrySet()) {
			Map<ClassName, MethodSignature> map = new HashMap<>();
			for (EMethod method : e.getValue()) {
				MethodSignature methodSignature = MethodSignature.parse(
						method.name + "$" + Integer.toHexString(method.declaring.name.hashCode()),
						method.signature().jvmDescriptor()
				);
				map.put(method.declaring.name, methodSignature);
			}
			methodMapping.put(e.getKey(), map);
		}

		for (var e : aspectElements.entrySet()) {
			Map<ClassName, MethodSignature> map = methodMapping.computeIfAbsent(e.getKey(), it -> new HashMap<>());

			for (MethodUsing elements : e.getValue()) {
				for (EAspectMethod method : elements.getElements()) {
					MethodSignature methodSignature = MethodSignature.parse(
							method.name + "Aspect$" + Integer.toHexString(method.declaring.name.hashCode()),
							method.signature().jvmDescriptor()
					);

					map.put(method.declaring.name, methodSignature);
				}
			}
		}

		ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES);

		String[] inters = new String[interfaces.size()];
		for (int i = 0; i < inters.length; i++) inters[i] = interfaces.get(i).internalName();

		cw.visit(
				Opcodes.V1_8,
				accessFlags | Opcodes.ACC_SUPER,
				thisClass.internalName(),
				null,
				superClass.internalName(),
				inters
		);

		for (EField it : sharedFields.values()) {
			FieldVisitor fieldVisitor = cw.visitField(
					it.flags,
					it.name,
					it.type().name.descriptor(),
					null,
					it.constant
			);
			fieldVisitor.visitEnd();
		}

		Map<String, Map<ClassName, String>> fieldMapping = new HashMap<>();
		for (var e : fields.entrySet()) {
			Map<ClassName, String> map = new HashMap<>();

			for (EField field : e.getValue()) {
				String fieldName = field.name + "$" + Integer.toHexString(field.declaring.name.hashCode());
				FieldVisitor fieldVisitor = cw.visitField(
						field.flags,
						fieldName,
						field.type().name.descriptor(),
						null,
						field.constant
				);
				fieldVisitor.visitEnd();
				map.put(field.declaring.name, fieldName);
			}

			fieldMapping.put(e.getKey(), map);
		}

		List<MethodSignature> constructorList = new ArrayList<>(constructors.size());
		for (EConstructor<?> constructor : constructors) {
			ClassDecl<?> declaring = constructor.declaring;
			String realName = "init$" + Integer.toHexString(declaring.name.hashCode());

			Map<MethodSignature, MethodNode> byMethods = declMethods.get(declaring.name);
			MethodNode byMethod = byMethods == null ? null : byMethods.get(constructor.signature());

			if (byMethod == null)
				throw new IllegalArgumentException("No such constructor declared in aspect class " + declaring.name + ".");

			MethodVisitor methodVisitor = cw.visitMethod(
					Opcodes.ACC_PRIVATE | Opcodes.ACC_BRIDGE,
					realName,
					constructor.signature().jvmDescriptor(),
					null,
					null
			);
			methodVisitor.visitCode();
			List<AbstractInsnNode> list = CollectionsKt.toList(byMethod.instructions).subList(2 + constructor.parameters.size(), byMethod.instructions.size());
			InsnList newInsnList = new InsnList();
			for (AbstractInsnNode it : list) newInsnList.add(it);
			byMethod.instructions = newInsnList;
			visitMethodBy(methodVisitor, byMethod, thisClass, superClass, aspectDecl, fieldMapping, methodMapping, stubAttaches);
			methodVisitor.visitMaxs(0, 0);
			methodVisitor.visitEnd();

			constructorList.add(MethodSignature.parse(realName, "()V"));
		}
		for (var e : aspectElements.entrySet()) {
			MethodSignature sign = e.getKey();

			for (MethodUsing elements : e.getValue()) {
				for (EAspectMethod element : elements.getElements()) {
					ClassDecl<?> declaring = element.declaring;
					MethodSignature realSignature = methodMapping.get(sign).get(declaring.name);

					Map<MethodSignature, MethodNode> byMethods = declMethods.get(declaring.name);
					MethodNode byMethod = byMethods == null ? null : byMethods.get(sign);

					if (byMethod == null)
						throw new IllegalArgumentException("No such method declared in aspect class " + declaring.name + ".");

					MethodVisitor methodVisitor = cw.visitMethod(
							Opcodes.ACC_PRIVATE,
							realSignature.methodName,
							sign.jvmDescriptor(),
							null,
							null
					);
					methodVisitor.visitCode();
					visitMethodBy(
							methodVisitor, byMethod,
							thisClass, superClass, aspectDecl,
							fieldMapping, methodMapping, stubAttaches
					);
					methodVisitor.visitMaxs(0, 0);
					methodVisitor.visitEnd();
				}
			}
		}

		for (MethodNode constructor : superConstructors) {
			MethodVisitor methodVisitor = cw.visitMethod(
					constructor.access,
					constructor.name,
					constructor.desc,
					constructor.signature,
					constructor.exceptions.toArray(Constant.EMPTY_STRING)
			);
			methodVisitor.visitCode();

			invokeMethod(
					methodVisitor,
					Opcodes.INVOKESPECIAL,
					superClass,
					MethodSignature.parse(constructor.name, constructor.desc),
					false
			);
			for (MethodSignature constructor1 : constructorList) {
				invokeMethod(
						methodVisitor,
						Opcodes.INVOKESPECIAL,
						thisClass,
						constructor1,
						false
				);
			}
			methodVisitor.visitInsn(Opcodes.RETURN);
			methodVisitor.visitMaxs(0, 0);
			methodVisitor.visitEnd();
		}

		for (var e : aspectElements.entrySet()) {
			MethodSignature sign = e.getKey();
			List<AspectFactory.MethodUsing> elements = e.getValue();

			AspectFactory.MethodUsing methodUsing = elements.get(0);
			if (methodUsing instanceof MixinUsing) {
				List<EAspectMethod> aspectMethods = new ArrayList<>();
				for (MethodUsing element : elements) aspectMethods.addAll(element.getElements());
				aspectMethods.sort(Comparator.comparing(it -> it.using.ordinal()));

				String desc = sign.jvmDescriptor();
				MethodNode superMethod = null;
				for (MethodNode it : superNode.methods) {
					if (it.name.equals(sign.methodName) && it.desc.equals(desc)) superMethod = it;
				}

				int insert = CollectionsKt.indexOfFirst(aspectMethods, it -> it.using == Using.AFTER || it.using == Using.AFTER_RETURN);
				List<ClassName> mixinList = new ArrayList<>();
				for (EAspectMethod aspectMethod : aspectMethods) mixinList.add(aspectMethod.declaring.name);
				if (superMethod != null) {
					List<ClassName> list = new ArrayList<>(insert + 1 + elements.size());
					list.addAll(mixinList.subList(0, insert));
					list.add(superClass);
					list.addAll(mixinList.subList(insert, elements.size()));
					mixinList = list;
				}

				EAspectMethod method = CollectionsKt.firstOrNull(aspectMethods, it -> it.using == Using.BEFORE_RETURN || it.using == Using.AFTER_RETURN);
				ClassName returnDeclaring = method != null ? method.declaring.name : superMethod != null ? superClass : null;

				if (returnDeclaring == null && !sign.returnType.equals(ClassName.V))
					throw new IllegalArgumentException("Mixin method with signature " + sign + " have no return value declared, and this method no existed target in super class " + superClass + ".");

				MethodVisitor methodVisitor = cw.visitMethod(
						Opcodes.ACC_PUBLIC,
						sign.methodName,
						sign.jvmDescriptor(),
						null,
						null
				);
				methodVisitor.visitCode();

				for (ClassName declaring : mixinList) {
					MethodSignature invokeBridge = methodMapping.get(sign).get(declaring);

					invokeMethod(
							methodVisitor,
							Opcodes.INVOKESPECIAL,
							thisClass,
							invokeBridge,
							false
					);
					if (!sign.returnType.equals(ClassName.V) && !declaring.equals(returnDeclaring))
						methodVisitor.visitInsn(Opcodes.POP);
				}
				returnValue(methodVisitor, sign.returnType);

				methodVisitor.visitMaxs(0, 0);
				methodVisitor.visitEnd();
			} else if (methodUsing instanceof SingleUsing su) {
				if (su.conflict())
					throw new IllegalArgumentException("Method " + su.method.name + " in aspect declare have conflict with other aspect method with same layer and signature.");

				EAspectMethod method = su.method;

				ClassDecl<?> declaring = method.declaring;
				Map<MethodSignature, MethodNode> byMethods = declMethods.get(declaring.name);
				MethodNode byMethod = byMethods == null ? null : byMethods.get(sign);

				if (byMethod == null)
					throw new IllegalArgumentException("No such method declared in aspect class " + declaring.name + ".");

				MethodSignature invokeBridge = methodMapping.get(sign).get(declaring.name);

				if (su.using == Using.REPLACE) {
					for (AbstractInsnNode insn : byMethod.instructions) {
						if (insn instanceof MethodInsnNode methodInsn
								&& insn.getOpcode() == Opcodes.INVOKESPECIAL
								&& aspectDecl.contains(ClassName.byInternalName(methodInsn.owner))) {
							throw new IllegalArgumentException("Method " +
									method.name +
									" in aspect declare is not allowed to call super method of aspect declaring classes when using REPLACE strategy.");
						}
					}
				}

				MethodVisitor methodVisitor = cw.visitMethod(
						Opcodes.ACC_PUBLIC,
						sign.methodName,
						sign.jvmDescriptor(),
						null,
						null
				);
				methodVisitor.visitCode();
				invokeMethod(
						methodVisitor,
						Opcodes.INVOKESPECIAL,
						thisClass,
						invokeBridge,
						false
				);
				returnValue(methodVisitor, sign.returnType);
				methodVisitor.visitMaxs(0, 0);
				methodVisitor.visitEnd();
			}
		}

		for (var e : methods.entrySet()) {
			MethodSignature sign = e.getKey();
			List<EMethod> list = e.getValue();

			for (EMethod method : list) {
				ClassDecl<?> declaring = method.declaring;
				MethodSignature realSign = methodMapping.get(sign).get(declaring.name);

				Map<MethodSignature, MethodNode> byMethods = declMethods.get(declaring.name);
				MethodNode byMethod = byMethods == null ? null : byMethods.get(sign);

				if (byMethod == null)
					throw new IllegalArgumentException("No such method declared in aspect class " + declaring.name + ".");

				MethodVisitor methodVisitor = cw.visitMethod(
						method.flags,
						realSign.methodName,
						realSign.jvmDescriptor(),
						byMethod.signature,
						byMethod.exceptions.toArray(Constant.EMPTY_STRING)
				);
				methodVisitor.visitCode();
				visitMethodBy(
						methodVisitor,
						byMethod,
						thisClass, superClass, aspectDecl,
						fieldMapping, methodMapping, stubAttaches
				);
				methodVisitor.visitMaxs(0, 0);
				methodVisitor.visitEnd();
			}
		}

		cw.visitEnd();

		return cw.toByteArray();
	}

	@Override
	public Class<?> loadClass(BytecodeLoader loader, ClassName className, byte[] bytecode) {
		String name = className.name();

		loader.declareClass(name, bytecode);
		return loader.loadClass(name);
	}

	@Override
	public void checkAspectable(ClassDecl<?> sourceClass, List<ClassDecl<?>> aspectClasses) {
		if ((sourceClass.flags() & (Modifier.PRIVATE | Modifier.FINAL)) != 0)
			throw new IllegalArgumentException("Source class " + sourceClass.name + " must not be final or private");
	}

	public static void returnValue(MethodVisitor write, ClassName returnType) {
		if (returnType.equals(ClassName.V)) write.visitInsn(Opcodes.RETURN);
		else if (returnType.equals(ClassName.B) || returnType.equals(ClassName.S) || returnType.equals(ClassName.I) || returnType.equals(ClassName.Z) || returnType.equals(ClassName.C))
			write.visitInsn(Opcodes.IRETURN);
		else if (returnType.equals(ClassName.J)) write.visitInsn(Opcodes.LRETURN);
		else if (returnType.equals(ClassName.F)) write.visitInsn(Opcodes.FRETURN);
		else if (returnType.equals(ClassName.D)) write.visitInsn(Opcodes.DRETURN);
		else write.visitInsn(Opcodes.ARETURN);
	}

	public static void invokeMethod(MethodVisitor write, int opcode, ClassName owner, MethodSignature method, boolean isInterface) {
		write.visitVarInsn(Opcodes.ALOAD, 0);

		for (int n = 0; n < method.paramTypes.size(); n++) {
			ClassName param = method.paramTypes.get(n);
			int varIndex = n + 1;

			switch (param.descriptor()) {
				case "B", "S", "I", "Z", "C" -> write.visitVarInsn(Opcodes.ILOAD, varIndex);
				case "J" -> write.visitVarInsn(Opcodes.LLOAD, varIndex);
				case "F" -> write.visitVarInsn(Opcodes.FLOAD, varIndex);
				case "D" -> write.visitVarInsn(Opcodes.DLOAD, varIndex);
				default -> write.visitVarInsn(Opcodes.ALOAD, varIndex);
			}
		}
		write.visitMethodInsn(opcode, owner.internalName(), method.methodName, method.jvmDescriptor(), isInterface);
	}

	public static void visitMethodBy(MethodVisitor write, MethodNode byMethod, ClassName thisClass, ClassName superClass, List<ClassName> aspectDecl, Map<String, Map<ClassName, String>> fieldMapping, Map<MethodSignature, Map<ClassName, MethodSignature>> methodMapping, Map<ClassName, ClassName> stubAttaches) {
		Set<String> aspectDeclSet = new HashSet<>(aspectDecl.size());
		for (ClassName decl : aspectDecl) aspectDeclSet.add(decl.internalName());

		MethodVisitor swap = new MethodVisitor(Opcodes.ASM9, write) {
			@Override
			public void visitMethodInsn(int opcode, String owner, String name, String descriptor, boolean isInterface) {
				ClassName realOwner = ClassName.byInternalName(owner);
				MethodSignature realMethod = MethodSignature.parse(name, descriptor);
				boolean realInterface = isInterface;

				if (name.equals("<init>") && aspectDeclSet.contains(owner)) {
					throw new IllegalArgumentException("Constructor " + realOwner.name() + "." + realMethod.methodName + realMethod.jvmDescriptor() + " in aspect declare is not allowed to be called in aspect method.");
				}

				if (opcode == Opcodes.INVOKESPECIAL || opcode == Opcodes.INVOKESTATIC) {
					ClassName attache = stubAttaches.get(realOwner);
					ClassName stubAttache = attache == null ? null : attache.equals(ClassName.V) ? superClass : attache;
					if (stubAttache != null) {
						realOwner = stubAttache;
						realInterface = false;
					}

					Map<ClassName, MethodSignature> map = methodMapping.get(realMethod);
					MethodSignature mapping = map == null ? null : map.get(realOwner);
					if (mapping != null) {
						realMethod = mapping;
						realOwner = thisClass;
						realInterface = false;
					}
				} else if (aspectDeclSet.contains(owner)) {
					realOwner = thisClass;
					realInterface = false;
				}

				write.visitMethodInsn(opcode, realOwner.internalName(), realMethod.methodName, realMethod.jvmDescriptor(), realInterface);
			}

			@Override
			public void visitFieldInsn(int opcode, String owner, String name, String descriptor) {
				ClassName realOwner = ClassName.byInternalName(owner);
				String realField = name;

				ClassName attache = stubAttaches.get(realOwner);
				ClassName stubAttache = attache == null ? null : attache.equals(ClassName.V) ? superClass : attache;
				if (stubAttache != null) {
					realOwner = stubAttache;
				}

				Map<ClassName, String> map = fieldMapping.get(name);
				String mapping = map == null ? null : map.get(realOwner);
				if (mapping != null) {
					realField = mapping;
					realOwner = thisClass;
				}

				if (aspectDeclSet.contains(owner)) {
					realOwner = thisClass;
				}

				write.visitFieldInsn(opcode, realOwner.internalName(), realField, descriptor);
			}

			@Override
			public void visitTypeInsn(int opcode, String type) {
				String realType = type;
				if (aspectDeclSet.contains(type)) realType = thisClass.internalName();
				write.visitTypeInsn(opcode, realType);
			}
		};
		byMethod.accept(swap);
	}
}
