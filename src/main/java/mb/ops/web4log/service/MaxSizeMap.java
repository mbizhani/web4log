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

	/*public static void main(String[] args) {
		Map<Integer, Integer> map = new MaxSizeMap<Integer, Integer>(10);

		for(int i=1; i<20; i++)
			map.put(i, i);

		for (Map.Entry<Integer, Integer> entry : map.entrySet()) {
			System.out.println(entry.getKey());
		}
	}*/
}
