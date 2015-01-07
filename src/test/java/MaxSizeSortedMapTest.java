import mb.ops.web4log.service.MaxSizeSortedMap;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

public class MaxSizeSortedMapTest {
	private static MaxSizeSortedMap<Integer, Integer> map = new MaxSizeSortedMap<Integer, Integer>(50);
	private static AtomicBoolean insertion = new AtomicBoolean(true);

	public static void main(String[] args) throws InterruptedException {

		for (int i = 1; i < 60; i++) {
			map.put(i, i);
		}

		for (int t = 0; t < 3; t++) {
			new Thread() {
				@Override
				public void run() {
					while (insertion.get()) {
						for (Map.Entry<Integer, Integer> entry : map.entrySet()) {
							System.out.println(entry.getKey() + " : " + entry.getValue());
						}

						Map<Integer, Integer> m = new LinkedHashMap<Integer, Integer>(map);
						System.out.println("@@@ SIZE: " + m.size());

						try {
							Thread.sleep(200);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
				}
			}.start();
		}

		Thread ins = new Thread() {
			@Override
			public void run() {
				for (int i = 20; i < 200; i++) {
					map.put(i, i);
					try {
						Thread.sleep(100);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		};

		ins.start();
		ins.join();
		insertion.set(false);
	}
}
