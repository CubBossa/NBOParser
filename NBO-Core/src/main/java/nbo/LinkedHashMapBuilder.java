package nbo;

import java.util.LinkedHashMap;

public class LinkedHashMapBuilder<K, V> {

	private final LinkedHashMap<K, V> map = new LinkedHashMap<>();

	public LinkedHashMapBuilder<K, V> put(K key, V value) {
		map.put(key, value);
		return this;
	}

	public LinkedHashMap<K, V> build() {
		return map;
	}
}
