package mb.ops.web4log.service;

import org.apache.log4j.Layout;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.spi.LoggingEvent;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class LogCacheService {
	private static final int CACHE_MAX_LINES = ConfigService.getInt("log.cache.max.lines");
	private static final Layout LOG_LAYOUT = new PatternLayout(ConfigService.getString("log.layout"));
	private static Map<String, Map<Long, LoggingEvent>> APP_MAP = new ConcurrentHashMap<String, Map<Long, LoggingEvent>>();

	public static void put(String app, LoggingEvent event) {
		if (!APP_MAP.containsKey(app)) {
			APP_MAP.put(app, Collections.synchronizedMap(new MaxSizeMap<Long, LoggingEvent>(CACHE_MAX_LINES)));
		}

		synchronized (APP_MAP.get(app)) {
			APP_MAP.get(app).put(event.getTimeStamp(), event);
		}
	}

	public static List<String> getAppList() {
		return new ArrayList<String>(APP_MAP.keySet());
	}

	public static LogContent getLogContent(String app, long fromIndex, String endOfLine, String logMessageFilter) {
		if (!APP_MAP.containsKey(app)) {
			return new LogContent("", 0);
		}

		LinkedHashMap<Long, LoggingEvent> amp;
		synchronized (APP_MAP.get(app)) {
			amp = new LinkedHashMap<Long, LoggingEvent>(APP_MAP.get(app));
		}

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
