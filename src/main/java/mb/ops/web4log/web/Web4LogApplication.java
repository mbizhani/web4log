package mb.ops.web4log.web;

import mb.ops.web4log.service.Log4jSocketServer;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.protocol.http.WebApplication;


public class Web4LogApplication extends WebApplication {
	@Override
	public Class<? extends WebPage> getHomePage() {
		return Index.class;
	}

	@Override
	public void init() {
		super.init();

		getMarkupSettings().setStripWicketTags(true);
		getMarkupSettings().setStripComments(true);
		getMarkupSettings().setCompressWhitespace(true);
		getMarkupSettings().setDefaultMarkupEncoding("UTF-8");

		Log4jSocketServer.start();
	}

	@Override
	protected void onDestroy() {
		Log4jSocketServer.stop();
	}
}
