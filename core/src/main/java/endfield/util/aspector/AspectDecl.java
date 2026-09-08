package endfield.util.aspector;

import endfield.util.aspector.classes.BytecodeLoader;
import endfield.util.aspector.classes.ClassName;
import endfield.util.aspector.generate.AspectFactory.AspectBuilder;

public abstract class AspectDecl<T> {
	protected AspectBuilder context;

	public AspectDecl(AspectBuilder cx) {
		context = cx;
	}

	public abstract ClassName getClassName();

	public abstract byte[] getBytecode();

	public abstract Class<T> load(BytecodeLoader loader);
}
