package mb.ops.web4log.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Properties;

public abstract class ConfigService {
	private static final Logger logger = LoggerFactory.getLogger(ConfigService.class);

	private static final Properties PROPERTIES = new Properties();

	public static void start() {
		try {
			PROPERTIES.load(ConfigService.class.getResourceAsStream("/config.properties"));
		} catch (IOException e) {
			logger.error("ConfigService Start: ", e);
			throw new RuntimeException(e);
		}
	}

	public static String getString(String key) {
		if (PROPERTIES.size() == 0)
			throw new RuntimeException("No Properties!");

		return PROPERTIES.getProperty(key);
	}

	public static int getInt(String key) {
		return Integer.parseInt(getString(key));
	}
}
