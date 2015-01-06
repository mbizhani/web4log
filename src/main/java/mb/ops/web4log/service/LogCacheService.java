package mb.ops.web4log.service;

import org.apache.log4j.*;
import org.apache.log4j.spi.LoggingEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;

public class LogCacheService {
	private static final Logger logger = LoggerFactory.getLogger(LogCacheService.class);

	private static final int CACHE_MAX_LINES = ConfigService.getInt("log.cache.max.lines");
	private static final Layout LOG_LAYOUT = new PatternLayout(ConfigService.getString("log.layout"));
	private static final Map<String, Map<Long, LoggingEvent>> APP_CONTENT_MAP = new LinkedHashMap<String, Map<Long, LoggingEvent>>();
	private static final Map<String, org.apache.log4j.Logger> APP_APPENDER_MAP = new HashMap<String, org.apache.log4j.Logger>();

	public static synchronized void addLog(String app, LoggingEvent event) {
		if (!APP_CONTENT_MAP.containsKey(app)) {
			APP_CONTENT_MAP.put(app, new MaxSizeMap<Long, LoggingEvent>(CACHE_MAX_LINES));
		}

		if (!APP_APPENDER_MAP.containsKey(app)) {
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

				APP_APPENDER_MAP.put(app, logger);
			} catch (IOException e) {
				logger.error("Create Appender: ", e);
			}

		}

		APP_CONTENT_MAP.get(app).put(event.getTimeStamp(), event);
		APP_APPENDER_MAP.get(app).error(LOG_LAYOUT.format(event));
	}

	public static synchronized Map<Long, LoggingEvent> getContentMap(String app) {
		return new LinkedHashMap<Long, LoggingEvent>(APP_CONTENT_MAP.get(app));
	}

	public static List<String> getAppList() {
		return new ArrayList<String>(APP_CONTENT_MAP.keySet());
	}

	public static LogContent getLogContent(String app, long fromIndex, String endOfLine, String logMessageFilter) {
		if (!APP_CONTENT_MAP.containsKey(app)) {
			return new LogContent("", 0);
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

}
