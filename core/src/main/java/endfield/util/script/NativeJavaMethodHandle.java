package endfield.util.script;

import rhino.BaseFunction;
import rhino.Context;
import rhino.Scriptable;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodType;

import static endfield.util.GetKt.sneakyThrow;
import static endfield.util.script.Scripts2.convertArgs;

/**
 * @see rhino.NativeJavaMethod
 */
public class NativeJavaMethodHandle extends BaseFunction {
	protected final MethodHandle target, spread;

	protected final int paramCount;
	protected final Class<?> returnType;
	protected final Class<?>[] parameterArray;

	public NativeJavaMethodHandle(Scriptable scope, MethodHandle handle) {
		super(scope, null);

		if (handle.isVarargsCollector()) handle = handle.asFixedArity();

		target = handle;

		MethodType type = handle.type();

		paramCount = type.parameterCount();
		returnType = type.returnType();
		parameterArray = type.parameterArray();

		spread = handle.asSpreader(Object[].class, paramCount)
				.asType(MethodType.methodType(Object.class, Object[].class));
	}

	@Override
	public String toString() {
		return target.toString();
	}

	@Override
	public Object get(Object key) {
		if ("__javaObject__".equals(key)) return target;
		return super.get(key);
	}

	@Override
	public Object call(Context cx, Scriptable scope, Scriptable thisObj, Object[] args) {
		try {
			return cx.getWrapFactory().wrap(cx, scope, spread.invokeExact(convertArgs(args, parameterArray)), returnType);
		} catch (Throwable e) {
			throw sneakyThrow(e);
		}
	}
}
