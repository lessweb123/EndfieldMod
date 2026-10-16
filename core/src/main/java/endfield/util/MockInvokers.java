package endfield.util;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;

public class MockInvokers implements Invokers {
	@Override
	public Lookup lookup(Class<?> refc) {
		return MethodHandles.publicLookup();
	}
}
