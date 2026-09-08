package endfield.util.aspector.generate;

import arc.func.Cons;
import endfield.util.aspector.AspectDecl;
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
import kotlin.collections.ArraysKt;
import kotlin.collections.CollectionsKt;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class AspectFactory {
	public ClassAccessor classAccessor;

	public AspectFactory(ClassAccessor accessor) {
		classAccessor = accessor;
	}

	public <T> AspectResult<T> makeClass(ClassDecl<T> targetClass, Cons<AspectBuilder> scope, ClassDecl<?>... aspectClasses) {
		AspectBuilder builder = new AspectBuilder(
				generateClassName(targetClass, aspectClasses),
				targetClass.flags(),
				targetClass.name,
				ArraysKt.map(aspectClasses, it -> it.name)
		);
		scope.get(builder);

		return new AspectResult<>(builder);
	}

	public abstract ClassName generateClassName(ClassDecl<?> targetClass, ClassDecl<?>... aspectClasses);

	public abstract byte[] generateBytecode(AspectBuilder builder);

	public abstract Class<?> loadClass(BytecodeLoader loader, ClassName className, byte[] bytecode);

	public abstract void checkAspectable(ClassDecl<?> sourceClass, List<ClassDecl<?>> aspectClasses);

	public class AspectResult<T> extends AspectDecl<T> {
		byte[] bytecode;

		public AspectResult(AspectBuilder builder) {
			super(builder);
		}

		@Override
		public ClassName getClassName() {
			return context.className;
		}

		@Override
		public byte[] getBytecode() {
			if (bytecode == null) {
				bytecode = generateBytecode(context);
			}
			return bytecode;
		}

		@SuppressWarnings("unchecked")
		@Override
		public Class<T> load(BytecodeLoader loader) {
			return (Class<T>) loadClass(
					loader,
					getClassName(),
					getBytecode()
			);
		}
	}

	public static abstract class MethodUsing {
		MethodSignature signature;
		int layer;

		public MethodUsing(MethodSignature sign, int lay) {
			signature = sign;
			layer = lay;
		}

		public abstract void addElement(EAspectMethod method);

		public abstract List<EAspectMethod> getElements();
	}

	public static class SingleUsing extends MethodUsing {
		public EAspectMethod method;
		public Using using;
		public List<EAspectMethod> methods;

		public SingleUsing(MethodSignature signature, int layer, EAspectMethod aspectMethod) {
			super(signature, layer);

			method = aspectMethod;
			using = method.using;
			methods = new ArrayList<>();

			switch (using) {
				case OVERRIDE, REPLACE -> {
				}
				default -> throw new IllegalArgumentException("Illegal using. method: " + method.signature());
			}
		}

		public boolean conflict() {
			return methods.size() > 1;
		}

		@Override
		public void addElement(EAspectMethod method) {
			methods.add(method);
		}

		@Override
		public List<EAspectMethod> getElements() {
			return methods;
		}
	}

	public static class MixinUsing extends MethodUsing {
		public List<EAspectMethod> methods;

		public MixinUsing(MethodSignature signature, int layer) {
			super(signature, layer);

			methods = new ArrayList<>();
		}

		@Override
		public void addElement(EAspectMethod method) {
			switch (method.using) {
				case BEFORE, BEFORE_RETURN, AFTER, AFTER_RETURN -> {
				}
				default ->
						throw new IllegalArgumentException("Aspect element with signature " + method.signature() + " was declared with " + method.using.name() + " using, but the layer was mixin.");
			}

			methods.add(method);
		}

		@Override
		public List<EAspectMethod> getElements() {
			return methods;
		}
	}

	public static class AspectBuilder {
		public ClassName className;
		public int accessFlags;
		public ClassName superClass;
		public List<ClassName> aspectDecl;

		public Map<ClassName, ClassName> stubAttaches = new HashMap<>();
		public List<ClassName> interfaces = new ArrayList<>();

		public Map<String, List<EField>> declFields = new HashMap<>();
		public Map<String, EField> sharedFields = new HashMap<>();
		public Map<MethodSignature, List<EMethod>> declMethods = new HashMap<>();
		public Map<MethodSignature, List<EConstructor<?>>> declConstructors = new HashMap<>();

		public Map<MethodSignature, List<MethodUsing>> aspectMethods = new HashMap<>();

		public AspectBuilder(ClassName name, int flags, ClassName superName, List<ClassName> aspectDecls) {
			className = name;
			accessFlags = flags;
			superClass = superName;
			aspectDecl = aspectDecls;
		}

		public void registerStubSpec(ClassName stub, ClassName attached) {
			stubAttaches.put(stub, attached);
		}

		public void registerInterfaces(ClassName inter) {
			interfaces.add(inter);
		}

		public void registerDeclField(EField field) {
			declFields.computeIfAbsent(field.name, k -> new ArrayList<>()).add(field);
		}

		public void registerSharedField(EField field) {
			sharedFields.putIfAbsent(field.name, field);
		}

		public void registerDeclMethod(EMethod method) {
			declMethods.computeIfAbsent(method.signature(), s -> new ArrayList<>()).add(method);
		}

		public void registerDeclConstructor(EConstructor<?> constructor) {
			declConstructors.computeIfAbsent(constructor.signature(), s -> new ArrayList<>()).add(constructor);
		}

		public void registerAspectMethod(int layer, EAspectMethod method) {
			List<MethodUsing> usings = aspectMethods.computeIfAbsent(method.signature(), s -> new ArrayList<>());
			MethodUsing using = CollectionsKt.firstOrNull(usings, it -> it.layer == layer);
			if (using == null) {
				using = switch (method.using) {
					case OVERRIDE, REPLACE -> new SingleUsing(method.signature(), layer, method);
					case BEFORE, BEFORE_RETURN, AFTER, AFTER_RETURN -> new MixinUsing(method.signature(), layer);
				};
				usings.add(using);
			}
			using.addElement(method);
		}
	}
}
