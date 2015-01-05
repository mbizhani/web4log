package mb.ops.web4log.web.panel;

import mb.ops.web4log.service.ConfigService;
import mb.ops.web4log.service.LogCacheService;
import mb.ops.web4log.service.LogContent;
import org.apache.wicket.ajax.AbstractAjaxTimerBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;
import org.apache.wicket.util.time.Duration;

public class TailRemoteLogPanel extends Panel {
	private static final int VIEW_MAX_LINES = ConfigService.getInt("log.view.max.lines");

	private String remoteApp, logMessageFilter;
	private long lastLine = 0;

	private TextArea<String> logArea;
	private TextField<String> filter;

	public TailRemoteLogPanel(String id) {
		this(id, null);
	}

	public TailRemoteLogPanel(String id, String remoteApp) {
		super(id);
		this.remoteApp = remoteApp;
	}

	public TailRemoteLogPanel setRemoteApp(String remoteApp) {
		this.remoteApp = remoteApp;
		return this;
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();

		filter = new TextField<String>("filter", new Model<String>());
		filter.setOutputMarkupId(true);

		Form form = new Form("form");
		form.add(filter);
		form.add(new AjaxButton("search", form) {
			@Override
			protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
				logMessageFilter = filter.getModelObject();
			}
		});

		form.add(new AjaxLink("reset") {
			@Override
			public void onClick(AjaxRequestTarget target) {
				logMessageFilter = null;
				filter.setModel(new Model<String>());
				target.add(filter);
			}
		});
		add(form);

		logArea = new TextArea<String>("logArea");
		logArea.setOutputMarkupId(true);
		add(logArea);

		add(new AbstractAjaxTimerBehavior(Duration.ONE_SECOND) {
			@Override
			protected void onTimer(AjaxRequestTarget target) {
				LogContent logContent = LogCacheService.getLogContent(remoteApp, lastLine, "\\n", logMessageFilter);
				if (logContent != null) {
					target.appendJavaScript(String.format("scroll('%s', '%s', %s, true);", logArea.getMarkupId(), logContent.getContent(), VIEW_MAX_LINES));
					lastLine = logContent.getLastIndex();
				}
			}
		});

		add(new AjaxLink("reload") {
			@Override
			public void onClick(AjaxRequestTarget target) {
				LogContent logContent = LogCacheService.getLogContent(remoteApp, 0, "\\n", logMessageFilter);
				if (logContent != null) {
					target.appendJavaScript(String.format("scroll('%s', '%s', %s, false);", logArea.getMarkupId(), logContent.getContent(), VIEW_MAX_LINES));
					lastLine = logContent.getLastIndex();
				}
			}
		});
	}
}
