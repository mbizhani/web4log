package mb.ops.web4log.service;

import java.util.LinkedHashMap;
import java.util.Map;

public class MaxSizeMap<K, V> extends LinkedHashMap<K, V> {
	private final int maxSize;

	public MaxSizeMap(int maxSize) {
		this.maxSize = maxSize;
	}

	@Override
	protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
		return size() > maxSize;
	}
}
