package endfield.android;

import java.util.Objects;

import static endfield.android.Unsafer2.internalUnsafe;
import static endfield.util.GetKt.sneakyThrow;

public class AndroidClassHelper2 extends AndroidClassHelper {
	@SuppressWarnings("unchecked")
	@Override
	public <T> T allocateInstance(Class<? extends T> clazz) {
		Objects.requireNonNull(clazz);

		try {
			return (T) internalUnsafe.allocateInstance(clazz);
		} catch (InstantiationException e) {
			throw sneakyThrow(e);
		}
	}
}
