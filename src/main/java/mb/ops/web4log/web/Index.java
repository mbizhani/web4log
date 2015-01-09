package mb.ops.web4log.web;

import mb.ops.web4log.service.AppEvent;
import mb.ops.web4log.service.AppInfo;
import mb.ops.web4log.service.LogCacheService;
import mb.ops.web4log.web.panel.TailRemoteLogPanel;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.atmosphere.Subscribe;
import org.apache.wicket.markup.html.WebComponent;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.DownloadLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import java.io.File;

public class Index extends WebPage {
	private WebMarkupContainer appListContainer;

	public Index(PageParameters parameters) {
		this(parameters.get("app").toOptionalString());
	}

	public Index(String app) {
		final String theApp = LogCacheService.isAppRegistered(app) ? app : null;

		add(new Label("title", theApp != null ? String.format("[%s]@Web4Log", theApp) : "Web4Log"));

		appListContainer = new WebMarkupContainer("appListContainer");
		appListContainer.setOutputMarkupId(true);
		add(appListContainer);

		appListContainer.add(new ListView<AppInfo>("appList", LogCacheService.getAppList()) {
			@Override
			protected void populateItem(final ListItem<AppInfo> item) {
				final AppInfo anAppInfo = item.getModelObject();
				WebComponent stat = new WebComponent("stat");
				stat.add(new AttributeModifier("class", anAppInfo.isConnected() ? "fa fa-link" : "fa fa-chain-broken"));
				stat.add(new AttributeModifier("style", String.format("color:%s;", anAppInfo.isConnected() ? "#8aff27" : "red")));

				Label selectedAppLbl = new Label("selectedApp", anAppInfo.getName());
				Link<String> appLink = new Link<String>("appLink") {
					@Override
					public void onClick() {
						setResponsePage(new Index(anAppInfo.getName()));
					}
				};
				appLink.add(new Label("appLinkLabel", anAppInfo.getName()));

				selectedAppLbl.setVisible(anAppInfo.getName().equals(theApp));
				appLink.setVisible(!selectedAppLbl.isVisible());

				item.add(stat);
				item.add(selectedAppLbl);
				item.add(appLink);
				item.add(new DownloadLink("downloadLogFile", new File(LogCacheService.getLogFileLocation(anAppInfo.getName()))));
			}
		});

		add(
				new TailRemoteLogPanel("tailPanel")
						.setRemoteApp(theApp)
						.setVisible(theApp != null)
		);
	}

	@Subscribe
	public void watchAppEvent(AjaxRequestTarget target, AppEvent event) {
		target.add(appListContainer);

		switch (event.getEventType()) {
			case CONNECTED:
				target.appendJavaScript(String.format("alert('[%s] CONNECTED');", event.getApp()));
				break;

			case DISCONNECTED:
				target.appendJavaScript(String.format("alert('[%s] DIS-CONNECTED!!!');", event.getApp()));
				break;

			case RE_CONNECTED:
				target.appendJavaScript(String.format("alert('[%s] RE-CONNECTED!');", event.getApp()));
				break;

			case ERROR:
				target.appendJavaScript(String.format("alert('[%s] ERROR!!!');", event.getApp()));
				break;
		}
	}
}
