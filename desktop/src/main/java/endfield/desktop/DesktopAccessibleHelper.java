package endfield.desktop;

import endfield.util.AccessibleHelper;

import java.lang.invoke.VarHandle;
import java.lang.reflect.AccessibleObject;

import static endfield.desktop.DesktopImpl.lookup;
import static endfield.util.GetKt.sneakyThrow;

public class DesktopAccessibleHelper implements AccessibleHelper {
	static final VarHandle override;

	static {
		try {
			override = lookup.findVarHandle(AccessibleObject.class, "override", boolean.class);
		} catch (NoSuchFieldException | IllegalAccessException e) {
			throw sneakyThrow(e);
		}
	}

	@Override
	public void makeAccessible(AccessibleObject object) {
		override.set(object, true);
	}
}
