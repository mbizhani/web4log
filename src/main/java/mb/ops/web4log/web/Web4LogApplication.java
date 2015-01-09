package mb.ops.web4log.web;

import mb.ops.web4log.service.Log4jSocketServer;
import mb.ops.web4log.service.LogCacheService;
import org.apache.wicket.atmosphere.EventBus;
import org.apache.wicket.atmosphere.config.AtmosphereLogLevel;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.protocol.http.WebApplication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class Web4LogApplication extends WebApplication {
	private static final Logger logger = LoggerFactory.getLogger(Web4LogApplication.class);

	@Override
	public Class<? extends WebPage> getHomePage() {
		return Index.class;
	}

	@Override
	public void init() {
		super.init();

		logger.info("Start Web4LogApplication");

		getMarkupSettings().setStripWicketTags(true);
		getMarkupSettings().setStripComments(true);
		getMarkupSettings().setCompressWhitespace(true);
		getMarkupSettings().setDefaultMarkupEncoding("UTF-8");

		EventBus eventBus = new EventBus(this);
		eventBus.getParameters().setLogLevel(AtmosphereLogLevel.INFO);
		LogCacheService.setEventBus(eventBus);

		Log4jSocketServer.start();

		logger.info("Web4LogApplication Inited");
	}

	@Override
	protected void onDestroy() {
		Log4jSocketServer.stop();
	}
}
