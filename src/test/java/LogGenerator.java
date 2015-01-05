import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LogGenerator {
	static Logger logger = LoggerFactory.getLogger("REMOTE");

	public static void main(String[] args) throws InterruptedException {

		for (int a = 1; a < 5; a++) {
			final int finalA = a;
			new Thread() {
				@Override
				public void run() {
					for (int i = 1; i < 200; i++) {
						logger.info("Hi: " + i);
						try {
							if (i == 50) {
								throw new RuntimeException("OOPS!");
							}
						} catch (RuntimeException e) {
							logger.warn("warn: " + i, e);
						}
						try {
							Thread.sleep(finalA * 200);
						} catch (InterruptedException e) {
							logger.error("sleep", e);
						}
					}
				}
			}.start();
		}
	}
}
