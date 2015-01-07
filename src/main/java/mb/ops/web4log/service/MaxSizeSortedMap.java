package mb.ops.web4log.service;

import java.util.Comparator;
import java.util.TreeMap;

public class MaxSizeSortedMap<K, V> extends TreeMap<K, V> {
	private final int maxSize;

	public MaxSizeSortedMap(int maxSize) {
		this(null, maxSize);
	}

	public MaxSizeSortedMap(Comparator<? super K> comparator, int maxSize) {
		super(comparator);

		this.maxSize = maxSize;
	}

	@Override
	public V put(K key, V value) {
		V put = super.put(key, value);
		while (size() > maxSize) {
			remove(firstKey());
		}
		return put;
	}
}
