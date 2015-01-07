package mb.ops.web4log.service;

import org.apache.log4j.*;
import org.apache.log4j.spi.LoggingEvent;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class LogCacheService {
	private static final Layout LOG_LAYOUT = new PatternLayout(ConfigService.getString("log.layout"));

	private static final Map<String, AppProfile> APP_MAP = new LinkedHashMap<String, AppProfile>();

	public static synchronized void addLog(String app, LoggingEvent event) {
		if (!APP_MAP.containsKey(app)) {
			APP_MAP.put(app, new AppProfile(app, createLoggerForApp(app)));
		}

		APP_MAP.get(app).addLog(event.getTimeStamp(), event);
	}

	public static synchronized Map<Long, LoggingEvent> getContentMap(String app) {
		return APP_MAP.get(app).getContentMap();
	}

	public static boolean isAppRegistered(String app) {
		return APP_MAP.containsKey(app);
	}

	public static List<String> getAppList() {
		return new ArrayList<String>(APP_MAP.keySet());
	}

	public static LogContent getLogContent(String app, long fromIndex, String endOfLine, String logMessageFilter) {
		if (!APP_MAP.containsKey(app)) {
			return null;
		}

		Map<Long, LoggingEvent> amp = getContentMap(app);
		StringBuilder builder = new StringBuilder();
		long lastIndex = 0L;
		for (Map.Entry<Long, LoggingEvent> entry : amp.entrySet()) {
			lastIndex = entry.getKey();
			if (lastIndex > fromIndex) {
				LoggingEvent le = entry.getValue();
				String line = LOG_LAYOUT.format(le)
					.replace("\r", "")
					.replace("\n", endOfLine);
				if (logMessageFilter == null || line.contains(logMessageFilter)) {
					builder.append(line);
					if (le.getThrowableStrRep() != null) {
						for (String s : le.getThrowableStrRep()) {
							builder.append(s).append(endOfLine);
						}
					}
				}
			}
		}

		return new LogContent(builder.toString(), lastIndex);
	}

	private static org.apache.log4j.Logger createLoggerForApp(String app) {
		try {
			DailyRollingFileAppender appender = new DailyRollingFileAppender(new PatternLayout("%m"),
				String.format("logs/web4log/%s.log", app), ".yyyy-MM-dd");
			appender.setThreshold(Level.ALL);
			appender.setName("APP_" + app);
			appender.setAppend(true);
			appender.activateOptions();

			org.apache.log4j.Logger logger = LogManager.getLogger("APP_" + app);
			logger.addAppender(appender);
			logger.setAdditivity(false);
			return logger;
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
}
