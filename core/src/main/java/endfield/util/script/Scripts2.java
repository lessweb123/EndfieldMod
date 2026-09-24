package endfield.util.script;

import arc.func.Func;
import arc.util.Log;
import endfield.files.Files2;
import endfield.util.Reflects;
import mindustry.Vars;
import rhino.Context;
import rhino.Function;
import rhino.JavaAdapter;
import rhino.NativeArray;
import rhino.Scriptable;
import rhino.Wrapper;

/**
 * Utility class for transition between Java and JS scripts, as well as providing a custom top level scope for the sake of
 * cross-mod compatibility. Use the custom scope for programmatically compiling Rhino functions.
 *
 * @since 1.0.6
 */
public final class Scripts2 {
	/** Don't let anyone instantiate this class. */
	private Scripts2() {}

	/** Initializes the Mod JS. */
	public static void init() {
		try {
			Vars.mods.getScripts().context.evaluateReader(
					Vars.mods.getScripts().scope,
					Files2.otherDir.child("endfield-global.js").reader(),
					"endfield-global.js",
					0
			);
		} catch (Throwable e) {
			Log.err(e);
		}
	}

	public static Function compileFunc(Scriptable scope, String sourceName, String source) {
		return compileFunc(scope, sourceName, source, 1);
	}

	public static Function compileFunc(Scriptable scope, String sourceName, String source, int lineNum) {
		return Vars.mods.getScripts().context.compileFunction(scope, source, sourceName, lineNum);
	}

	@SuppressWarnings("unchecked")
	public static <T> Func<Object[], T> requireType(Function func, Context context, Scriptable scope, Class<?> returnType) {
		Class<?> type = Reflects.wrapper(returnType);
		return args -> {
			Object res = func.call(context, scope, scope, args);

			if (type == Void.class || res == null) return null;

			if (res instanceof Wrapper w) res = w.unwrap();
			if (!type.isInstance(res))
				throw new ClassCastException("Incompatible return type: Expected '" + returnType + "', but got '" + res.getClass() + "'!");
			return (T) res;
		};
	}

	public static Object[] convertArgs(NativeArray arr, Class<?>[] types) {
		return convertArgs(arr.toArray(), types);
	}

	public static Object[] convertArgs(Object[] arr, Class<?>[] types) {
		Object[] res = new Object[arr.length];
		for (int i = 0; i < arr.length; i++) {
			res[i] = JavaAdapter.convertResult(arr[i], types[i]);
		}
		return res;
	}
}
