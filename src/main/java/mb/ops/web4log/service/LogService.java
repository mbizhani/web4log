package mb.ops.web4log.service;

import org.apache.log4j.*;
import org.apache.log4j.spi.LoggingEvent;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

public class LogService {
	private static final Layout LOG_LAYOUT = new PatternLayout(ConfigService.getString("log.layout"));
	private static final Map<String, AppProfile> APP_MAP = new LinkedHashMap<String, AppProfile>();
	private static final List<AppInfo> APP_INFO_LIST = new ArrayList<AppInfo>();

	private static IEventListener eventListener;
	private static ScheduledExecutorService scheduler;

	public static void start(IEventListener eventListener) {
		LogService.eventListener = eventListener;

		startDateDispatcher();

		Log4jSocketServer.start();
	}

	public static void stop() {
		Log4jSocketServer.stop();
		scheduler.shutdown();
	}

	public static void dispatchEvent(Object event) {
		if (eventListener != null) {
			eventListener.handleEvent(event);
		} else {
			throw new RuntimeException("No IEventListener!");
		}
	}

	public static void appConnected(String app) {
		if (app != null) {
			if (!APP_MAP.containsKey(app)) {
				APP_MAP.put(app, new AppProfile(app, createLoggerForApp(app)));
				APP_INFO_LIST.add(new AppInfo(app));
				dispatchEvent(new AppEvent(app, EAppEvent.CONNECTED));
			} else {
				for (AppInfo appInfo : APP_INFO_LIST) {
					if (appInfo.getName().equals(app)) {
						appInfo.setConnected(true);
						break;
					}
				}
				dispatchEvent(new AppEvent(app, EAppEvent.RECONNECTED));
			}
		}
	}

	public static void appDisconnected(String app) {
		if (app != null) {
			for (AppInfo appInfo : APP_INFO_LIST) {
				if (appInfo.getName().equals(app)) {
					appInfo.setConnected(false);
					break;
				}
			}
			dispatchEvent(new AppEvent(app, EAppEvent.DISCONNECTED));
		}
	}

	public static synchronized void addLog(String app, LoggingEvent le) {
		APP_MAP.get(app).addLog(le, getMessageOfLoggingEvent(le, "\n") + getThrowableOfLoggingEvent(le, "\n"));

		dispatchEvent(le);

		if (Level.ERROR.equals(le.getLevel())) {
			dispatchEvent(new AppEvent(app, EAppEvent.ERROR));
		}
	}

	public static synchronized Map<Long, LoggingEvent> getContentMap(String app) {
		return APP_MAP.get(app).getContentMap();
	}

	public static boolean isAppRegistered(String app) {
		return APP_MAP.containsKey(app);
	}

	public static List<AppInfo> getAppList() {
		return APP_INFO_LIST;
	}

	public static String getLogContent(String app, String endOfLine) {
		return getLogContent(app, endOfLine, null);
	}

	public static String getLogContent(String app, String endOfLine, String logMessageFilter) {
		if (!APP_MAP.containsKey(app)) {
			return null;
		}

		Map<Long, LoggingEvent> amp = getContentMap(app);
		StringBuilder builder = new StringBuilder();
		for (Map.Entry<Long, LoggingEvent> entry : amp.entrySet()) {
			LoggingEvent le = entry.getValue();
			String line = getMessageOfLoggingEvent(le, endOfLine);
			if (logMessageFilter == null || line.contains(logMessageFilter)) {
				builder.append(line).append(getThrowableOfLoggingEvent(le, endOfLine));
			}
		}

		return builder.toString();
	}

	public static String getMessageOfLoggingEvent(LoggingEvent le, String endOfLine) {
		return LOG_LAYOUT.format(le)
			.replace("\r", "")
			.replace("\n", endOfLine);
	}

	public static String getThrowableOfLoggingEvent(LoggingEvent le, String endOfLine) {
		StringBuilder builder = new StringBuilder();
		if (le.getThrowableStrRep() != null) {
			for (String s : le.getThrowableStrRep()) {
				builder.append(s).append(endOfLine);
			}
		}
		return builder.toString();
	}

	public static String getApplicationOfLoggingEvent(LoggingEvent le) {
		Object application = le.getMDC("application");
		if (application != null)
			return application.toString();
		return null;
	}

	public static String getLogFileLocation(String app) {
		return APP_MAP.get(app).getLogFile();
	}

	public static String getFormattedDate(Date date) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm");
		return sdf.format(date);
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

	private static void startDateDispatcher() {
		scheduler = Executors.newScheduledThreadPool(1, new ThreadFactory() {
			@Override
			public Thread newThread(Runnable r) {
				Thread ret = new Thread(r);
				ret.setDaemon(true);
				return ret;
			}
		});
		final Runnable beeper = new Runnable() {
			@Override
			public void run() {
				dispatchEvent(new Date());
			}
		};
		int currSeconds = Calendar.getInstance().get(Calendar.SECOND);
		scheduler.scheduleWithFixedDelay(beeper, 60 - currSeconds, 60, TimeUnit.SECONDS);
	}
}
