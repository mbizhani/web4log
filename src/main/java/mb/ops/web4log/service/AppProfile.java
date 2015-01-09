package mb.ops.web4log.service;

import org.apache.log4j.FileAppender;
import org.apache.log4j.Layout;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.spi.LoggingEvent;

import java.util.LinkedHashMap;
import java.util.Map;

public class AppProfile {
	private static final int CACHE_MAX_LINES = ConfigService.getInt("log.cache.max.lines");
	private static final Layout LOG_LAYOUT = new PatternLayout(ConfigService.getString("log.layout"));

	private String app;
	private Map<Long, LoggingEvent> contentMap;
	private Logger logger;

	public AppProfile(String app, Logger logger) {
		this.app = app;
		this.contentMap = new MaxSizeSortedMap<Long, LoggingEvent>(CACHE_MAX_LINES);
		this.logger = logger;
	}

	public String getApp() {
		return app;
	}

	public Map<Long, LoggingEvent> getContentMap() {
		return new LinkedHashMap<Long, LoggingEvent>(contentMap);
	}

	public Logger getLogger() {
		return logger;
	}

	public String getLogFile() {
		return ((FileAppender) getLogger().getAppender("APP_" + app)).getFile();
	}

	public void addLog(Long timestamp, LoggingEvent event) {
		contentMap.put(timestamp, event);
		logger.error(LOG_LAYOUT.format(event));
	}
}
