package endfield.util.pooling;

import arc.func.Prov;
import arc.util.pooling.Pool;

public class PoolImpl<T> extends Pool<T> {
	public final Prov<T> provider;

	public PoolImpl(Prov<T> prov) {
		provider = prov;
	}

	public PoolImpl(Prov<T> prov, int initialCapacity) {
		super(initialCapacity);
		provider = prov;
	}

	public PoolImpl(Prov<T> prov, int initialCapacity, int max) {
		super(initialCapacity, max);
		provider = prov;
	}

	@Override
	protected T newObject() {
		return provider.get();
	}
}
