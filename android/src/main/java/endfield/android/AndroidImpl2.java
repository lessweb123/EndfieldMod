package endfield.android;

import android.annotation.TargetApi;
import android.os.Build.VERSION_CODES;
import endfield.util.FieldAccessor;
import endfield.util.ReflectionMethodInvokeHelper;

import java.lang.reflect.Field;

import static endfield.Vars2.accessibleHelper;
import static endfield.Vars2.classHelper;
import static endfield.Vars2.fieldAccessHelper;
import static endfield.Vars2.imageHandle;
import static endfield.Vars2.methodInvokeHelper;
import static endfield.android.Unsafer2.internalUnsafe;

// Sdk_version>=33
@TargetApi(VERSION_CODES.TIRAMISU)
public class AndroidImpl2 extends AndroidImpl {
	public AndroidImpl2() {}

	@Override
	void init() {
		accessibleHelper = new AndroidAccessibleHelper();
		classHelper = new AndroidClassHelper2();
		fieldAccessHelper = new UnsafeFieldAccessHelper2();
		methodInvokeHelper = new ReflectionMethodInvokeHelper();

		imageHandle = new AndroidImageHandle();
	}

	@Override
	public FieldAccessor fieldAccessor(Field field) {
		return UnsafeFieldAccessor2.getUnsafeFieldAccessor(field);
	}

	@Override
	public void put(long srcAddress, long destAddress, long bytes) {
		internalUnsafe.copyMemory(srcAddress, destAddress, bytes);
	}

	@Override
	public void put(Object src, int srcOffset, Object dst, int dstOffset, long bytes) {
		internalUnsafe.copyMemory(src, srcOffset, dst, dstOffset, bytes);
	}
}
