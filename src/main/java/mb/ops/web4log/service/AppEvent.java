package mb.ops.web4log.service;

public class AppEvent {
	private String app;
	private EAppEvent eventType;

	public AppEvent(String app, EAppEvent eventType) {
		this.app = app;
		this.eventType = eventType;
	}

	public String getApp() {
		return app;
	}

	public EAppEvent getEventType() {
		return eventType;
	}
}
