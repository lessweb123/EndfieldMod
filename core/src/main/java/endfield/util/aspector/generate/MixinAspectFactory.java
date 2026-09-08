package endfield.util.aspector.generate;

import endfield.util.aspector.classes.BytecodeLoader;
import endfield.util.aspector.classes.ClassAccessor;
import endfield.util.aspector.classes.ClassDecl;
import endfield.util.aspector.classes.ClassName;
import kotlin.NotImplementedError;

import java.util.List;

public class MixinAspectFactory extends AspectFactory {
	public MixinAspectFactory(ClassAccessor accessor) {
		super(accessor);
	}

	@Override
	public ClassName generateClassName(ClassDecl<?> targetClass, ClassDecl<?>... aspectClasses) {
		return targetClass.name;
	}

	@Override
	public byte[] generateBytecode(AspectBuilder builder) {
		throw new NotImplementedError();
	}

	@Override
	public Class<?> loadClass(BytecodeLoader loader, ClassName className, byte[] bytecode) {
		throw new NotImplementedError();
	}

	@Override
	public void checkAspectable(ClassDecl<?> sourceClass, List<ClassDecl<?>> aspectClasses) {
		throw new NotImplementedError();
	}
}
