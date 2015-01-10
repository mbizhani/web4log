package mb.ops.web4log.web.panel;

import mb.ops.web4log.service.ConfigService;
import mb.ops.web4log.service.LogService;
import org.apache.log4j.spi.LoggingEvent;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.atmosphere.Subscribe;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;

public class TailRemoteLogPanel extends Panel {
	private static final int VIEW_MAX_LINES = ConfigService.getInt("log.view.max.lines");

	private String remoteApp, logMessageFilter;

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

		Model<String> model = null;
		if (remoteApp != null) {
			String logContent = LogService.getLogContent(remoteApp, "\n");
			model = new Model<String>(logContent);
		}
		logArea = new TextArea<String>("logArea", model);
		logArea.setOutputMarkupId(true);
		add(logArea);

		add(new AjaxLink("reload") {
			@Override
			public void onClick(AjaxRequestTarget target) {
				String logContent = LogService.getLogContent(remoteApp, "\\n", logMessageFilter);
				if (logContent != null) {
					target.appendJavaScript(String.format("scroll('%s', '%s', %s, false);", logArea.getMarkupId(),
						logContent, VIEW_MAX_LINES));
				}
			}
		});
	}

	@Subscribe
	public void appendLoggingEvent(AjaxRequestTarget target, LoggingEvent le) {
		if (remoteApp.equals(LogService.getApplicationOfLoggingEvent(le))) {
			String message = LogService.getMessageOfLoggingEvent(le, "\\n");
			if (logMessageFilter == null || message.contains(logMessageFilter)) {
				target.appendJavaScript(String.format("scroll('%s', '%s', %s, true);", logArea.getMarkupId(),
					message + LogService.getThrowableOfLoggingEvent(le, "\\n"), VIEW_MAX_LINES));
			}
		}
	}
}
