package endfield.desktop;

import endfield.util.aspector.PackageAccessor;
import endfield.util.aspector.accesses.PackageAccessHandler;
import endfield.util.aspector.classes.ClassAccessor;
import endfield.util.aspector.classes.ClassElement;
import endfield.util.aspector.classes.ClassName;
import endfield.util.aspector.classes.EConstructor;
import endfield.util.aspector.classes.EMethod;
import endfield.util.aspector.classes.MethodSignature;
import endfield.util.aspector.generate.ProxyAspectFactory;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.security.ProtectionDomain;
import java.util.ArrayList;
import java.util.List;

import static endfield.desktop.Unsafer.unsafe;

public class UnsafePackageAccessHandler extends PackageAccessHandler {
	static ClassName packageAccessorT = ClassName.byClass(PackageAccessor.class);

	public UnsafePackageAccessHandler(ClassAccessor accessor) {
		super(accessor);
	}

	@Override
	protected byte[] genPackageAccessClass(AccessBuilder builder) {
		ClassName className = builder.className;
		ClassName targetName = builder.accessTarget;

		List<ClassElement> elements = builder.enhanceElements;
		List<EMethod> methods = new ArrayList<>();
		List<EConstructor<?>> constructors = new ArrayList<>();

		for (ClassElement element : elements) {
			if (element instanceof EMethod method) {
				methods.add(method);
			} else if (element instanceof EConstructor<?> constructor) {
				constructors.add(constructor);
			}
		}

		ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
		classWriter.visit(
				Opcodes.V1_8,
				Opcodes.ACC_PUBLIC + Opcodes.ACC_SUPER,
				className.internalName(),
				null,
				targetName.internalName(),
				null
		);
		classWriter.visitAnnotation(
				packageAccessorT.descriptor(),
				true
		).visitEnd();

		for (EMethod method : methods) {
			MethodVisitor methodVisitor = classWriter.visitMethod(
					method.flags | Opcodes.ACC_PUBLIC,
					method.name,
					method.signature().jvmDescriptor(),
					null,
					null
			);
			methodVisitor.visitCode();

			ProxyAspectFactory.invokeMethod(
					methodVisitor,
					Opcodes.INVOKESPECIAL,
					targetName,
					method.signature(),
					false
			);
			ProxyAspectFactory.returnValue(methodVisitor, method.signature().returnType);

			methodVisitor.visitMaxs(0, 0);
			methodVisitor.visitEnd();
		}

		for (EConstructor<?> constructor : constructors) {
			MethodVisitor methodVisitor = classWriter.visitMethod(
					constructor.flags & ~Opcodes.ACC_PROTECTED | Opcodes.ACC_PUBLIC,
					"<init>",
					constructor.signature().jvmDescriptor(),
					null,
					null
			);
			methodVisitor.visitCode();

			ProxyAspectFactory.invokeMethod(
					methodVisitor,
					Opcodes.INVOKESPECIAL,
					targetName,
					constructor.signature(),
					false
			);
			methodVisitor.visitInsn(Opcodes.RETURN);

			methodVisitor.visitMaxs(0, 0);
			methodVisitor.visitEnd();
		}
		classWriter.visitEnd();

		return classWriter.toByteArray();
	}

	@Override
	protected Class<?> loadClass(ClassName className, byte[] bytecode, Class<?> accessTarget) {
		ProtectionDomain accessTargetDomain = accessTarget.getProtectionDomain();

		ClassAccessor.sharedClassByte.put(className, bytecode);

		return unsafe.defineClass(
				className.name(),
				bytecode, 0, bytecode.length,
				accessTarget.getClassLoader(), accessTargetDomain
		);
	}

	void returnValue(MethodVisitor write, EMethod method) {
		ClassName returnType = method.signature().returnType;

		if (returnType.equals(ClassName.V)) {
			write.visitInsn(Opcodes.RETURN);
		} else if (returnType.equals(ClassName.B) || returnType.equals(ClassName.S) || returnType.equals(ClassName.I) || returnType.equals(ClassName.Z) || returnType.equals(ClassName.C)) {
			write.visitInsn(Opcodes.IRETURN);
		} else if (returnType.equals(ClassName.J)) {
			write.visitInsn(Opcodes.LRETURN);
		} else if (returnType.equals(ClassName.F)) {
			write.visitInsn(Opcodes.FRETURN);
		} else if (returnType.equals(ClassName.D)) {
			write.visitInsn(Opcodes.DRETURN);
		} else {
			write.visitInsn(Opcodes.ARETURN);
		}
	}

	void invokeMethod(MethodVisitor write, ClassName owner, MethodSignature method, boolean isInterface) {
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
		write.visitMethodInsn(
				Opcodes.INVOKESPECIAL,
				owner.internalName(),
				method.methodName,
				method.jvmDescriptor(),
				isInterface
		);
	}
}
