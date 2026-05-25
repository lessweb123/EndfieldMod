package endfield.util;

import arc.func.Prov;

import java.util.Map;

public final class Maps2 {
	private Maps2() {}

	public static <K, V> V getOrElse(Map<K, V> map, K key, Prov<V> defaultValue) {
		V value = map.get(key);
		return value == null ? defaultValue.get() : value;
	}
}
