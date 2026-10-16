package endfield.desktop;

import endfield.util.AccessibleHelper;

import java.lang.invoke.VarHandle;
import java.lang.reflect.AccessibleObject;

import static endfield.desktop.DesktopImpl.lookup;
import static endfield.util.GetKt.sneakyThrow;

public class DesktopAccessibleHelper implements AccessibleHelper {
	@Override
	public void makeAccessible(AccessibleObject object) {
		Companion.override.set(object, true);
	}

	public static final class Companion {
		static VarHandle override;

		static {
			try {
				override = lookup.findVarHandle(AccessibleObject.class, "override", boolean.class);
			} catch (NoSuchFieldException | IllegalAccessException e) {
				throw sneakyThrow(e);
			}
		}

		private Companion() {}
	}
}
