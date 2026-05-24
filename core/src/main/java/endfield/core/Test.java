package endfield.core;

import arc.util.Log;
import endfield.util.CollectionObjectMap;
import endfield.util.ExtraVariable;
import org.jetbrains.annotations.TestOnly;

import java.util.Map;

/** Classes for testing purposes only, do not use. */
@TestOnly
public class Test implements Cloneable, ExtraVariable {
	private static short count;

	public Map<String, Object> extraVar = new CollectionObjectMap<>(String.class, Object.class);
	public short id;

	public Object target;
	public int index;

	public Test() {
		id = count++;
	}

	public static void test() throws Throwable {}

	public static void call() {
		try {
			test();
		} catch (Throwable e) {
			Log.err(e);
		}
	}

	public Test copy() {
		try {
			return (Test) super.clone();
		} catch (CloneNotSupportedException e) {
			throw new AssertionError(e);
		}
	}

	@Override
	public Map<String, Object> extra() {
		return extraVar;
	}

	public interface Aspect {
		Package definePackage(Class<?> c);
	}

	public interface AccessStub {
		default Package definePackage(Class<?> c) {
			Log.infoTag(toString(), "TODO");
			return null;
		}
	}
}
