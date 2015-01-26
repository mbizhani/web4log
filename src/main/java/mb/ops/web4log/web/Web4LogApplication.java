package mb.ops.web4log.web;

import mb.ops.web4log.service.ConfigService;
import mb.ops.web4log.service.IEventListener;
import mb.ops.web4log.service.LogService;
import org.apache.wicket.atmosphere.EventBus;
import org.apache.wicket.atmosphere.config.AtmosphereLogLevel;
import org.apache.wicket.atmosphere.config.AtmosphereParameters;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.protocol.http.WebApplication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Web4LogApplication extends WebApplication implements IEventListener {
	private static final Logger logger = LoggerFactory.getLogger(Web4LogApplication.class);

	private EventBus eventBus;

	@Override
	public Class<? extends WebPage> getHomePage() {
		return Index.class;
	}

	@Override
	public void init() {
		logger.info("********************");
		logger.info("* Starting Web4Log *");
		logger.info("********************");

		super.init();

		ConfigService.start();

		getMarkupSettings().setStripWicketTags(true);
		getMarkupSettings().setStripComments(true);
		getMarkupSettings().setCompressWhitespace(true);
		getMarkupSettings().setDefaultMarkupEncoding("UTF-8");

		eventBus = new EventBus(this);
		AtmosphereParameters parameters = eventBus.getParameters();
		parameters.setLogLevel(AtmosphereLogLevel.INFO);

		String timeout = ConfigService.getString("atmosphere.timeout");
		if (timeout != null) {
			parameters.setTimeout(Integer.parseInt(timeout));
		}

		String reconnectInterval = ConfigService.getString("atmosphere.reconnect.interval");
		if (reconnectInterval != null) {
			parameters.setReconnectInterval(Integer.parseInt(reconnectInterval));
		}

		String maxReconnectOnClose = ConfigService.getString("atmosphere.max.reconnect.on.close");
		if (maxReconnectOnClose != null) {
			parameters.setReconnectInterval(Integer.parseInt(maxReconnectOnClose));
		}

		LogService.start(this);

		logger.info("Web4LogApplication Inited");
	}

	@Override
	public void handleEvent(Object event) {
		eventBus.post(event);
	}

	@Override
	protected void onDestroy() {
		LogService.stop();
	}
}
