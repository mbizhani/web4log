package mb.ops.web4log.web;

import mb.ops.web4log.service.LogCacheService;
import mb.ops.web4log.web.panel.TailRemoteLogPanel;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import java.util.ArrayList;
import java.util.List;

public class Index extends WebPage {
	private List<String> apps = new ArrayList<String>();

	private WebMarkupContainer appListContainer;

	public Index(PageParameters parameters) {
		this(parameters.get("app").toOptionalString());
	}

	public Index(String app) {
		final String theApp = LogCacheService.isAppRegistered(app) ? app : null;

		add(new AjaxLink("reloadAppList") {
			@Override
			public void onClick(AjaxRequestTarget target) {
				apps.clear();
				apps.addAll(LogCacheService.getAppList());
				target.add(appListContainer);
			}
		});

		appListContainer = new WebMarkupContainer("appListContainer");
		appListContainer.setOutputMarkupId(true);
		add(appListContainer);

		apps.addAll(LogCacheService.getAppList());
		appListContainer.add(new ListView<String>("appList", apps) {
			@Override
			protected void populateItem(final ListItem<String> item) {
				final String selectedApp = item.getModelObject();
				Label selectedAppLbl = new Label("selectedApp", selectedApp);
				Link<String> appLink = new Link<String>("appLink") {
					@Override
					public void onClick() {
						setResponsePage(new Index(selectedApp));
					}
				};
				appLink.add(new Label("appLinkLabel", selectedApp));

				selectedAppLbl.setVisible(selectedApp.equals(theApp));
				appLink.setVisible(!selectedAppLbl.isVisible());

				item.add(selectedAppLbl);
				item.add(appLink);
			}
		});

		add(
			new TailRemoteLogPanel("tailPanel")
				.setRemoteApp(theApp)
				.setVisible(theApp != null)
		);
	}
}
