package mb.ops.web4log.web;

import mb.ops.web4log.service.AppEvent;
import mb.ops.web4log.service.AppInfo;
import mb.ops.web4log.service.ConfigService;
import mb.ops.web4log.service.LogService;
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
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.util.time.Duration;

import java.io.File;
import java.io.Serializable;
import java.util.Date;

public class Index extends WebPage {
	private WebMarkupContainer appListContainer;
	private Label serverDate;

	public Index(PageParameters parameters) {
		this(parameters.get("app").toOptionalString());
	}

	public Index(String app) {
		setVersioned(false);
		final String theApp = LogService.isAppRegistered(app) ? app : null;

		serverDate = new Label("serverDate", LogService.getFormattedDate(new Date()));
		serverDate.setOutputMarkupId(true);

		add(new Label("title", theApp != null ? String.format("[%s]@Web4Log", theApp) : "Web4Log"));
		add(new Label("headerLabel", ConfigService.getString("header.label")));
		add(serverDate);

		appListContainer = new WebMarkupContainer("appListContainer");
		appListContainer.setOutputMarkupId(true);
		add(appListContainer);

		appListContainer.add(new ListView<AppInfo>("appList", LogService.getAppList()) {
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
				item.add(new DownloadLink("downloadLogFile", new File(LogService.getLogFileLocation(anAppInfo.getName())))
					.setCacheDuration(Duration.NONE));
			}
		});

		add(
			new TailRemoteLogPanel("tailPanel")
				.setRemoteApp(theApp)
				.setVisible(theApp != null)
		);
	}

	@Subscribe
	public void watchDateEvent(AjaxRequestTarget target, Date date) {
		serverDate.setDefaultModel(new Model<Serializable>(LogService.getFormattedDate(date)));
		target.add(serverDate);
	}

	@Subscribe
	public void watchAppEvent(AjaxRequestTarget target, AppEvent event) {
		target.add(appListContainer);
		target.appendJavaScript(String.format("alert('App=[%s] %s');", event.getApp(), event.getEventType()));
	}
}
