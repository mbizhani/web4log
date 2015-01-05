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

import java.util.ArrayList;
import java.util.List;

public class Index extends WebPage {
	private String remoteApp;
	private List<String> apps = new ArrayList<String>();

	private WebMarkupContainer appListContainer;
	private TailRemoteLogPanel tailPanel;

	public Index() {
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
				final String theApp = item.getModelObject();
				Label selectedApp = new Label("selectedApp", theApp);
				Link<String> appLink = new Link<String>("appLink") {
					@Override
					public void onClick() {
						remoteApp = theApp;
						tailPanel
								.setRemoteApp(theApp)
								.setVisible(true);
					}
				};
				appLink.add(new Label("appLinkLabel", theApp));

				selectedApp.setVisible(theApp.equals(remoteApp));
				appLink.setVisible(!selectedApp.isVisible());

				item.add(selectedApp);
				item.add(appLink);
			}
		});

		tailPanel = new TailRemoteLogPanel("tailPanel");
		tailPanel
				.setVisible(false)
				.setOutputMarkupId(true)
				.setOutputMarkupPlaceholderTag(true);
		add(tailPanel);
	}
}
