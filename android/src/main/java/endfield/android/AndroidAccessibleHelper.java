package endfield.android;

import arc.util.Log;
import endfield.util.AccessibleHelper;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;

import static endfield.util.GetKt.sneakyThrow;

public class AndroidAccessibleHelper implements AccessibleHelper {
	@Override
	public void makeAccessible(AccessibleObject object) {
		try {
			OverrideHelper.override.setBoolean(object, true);
		} catch (IllegalAccessException e) {
			throw sneakyThrow(e);
		}
	}

	@Override
	public void makeClassAccessible(Class<?> clazz) {
		try {
			int flags = AccessFlagsHelper.accessFlags.getInt(clazz);
			AccessFlagsHelper.accessFlags.setInt(clazz, 65535 & ((flags & 65535 & (-17) & (-3)) | 1));
		} catch (IllegalAccessException e) {
			Log.err(e);
		}
	}

	static class OverrideHelper {
		static final Field override;

		static {
			try {
				override = AccessibleObject.class.getDeclaredField("override");
				override.setAccessible(true);
			} catch (NoSuchFieldException e) {
				throw sneakyThrow(e);
			}
		}

		private OverrideHelper() {}
	}

	static class AccessFlagsHelper {
		static final Field accessFlags;

		static {
			try {
				accessFlags = Class.class.getDeclaredField("accessFlags");
				accessFlags.setAccessible(true);
			} catch (NoSuchFieldException e) {
				throw sneakyThrow(e);
			}
		}

		private AccessFlagsHelper() {}
	}
}
