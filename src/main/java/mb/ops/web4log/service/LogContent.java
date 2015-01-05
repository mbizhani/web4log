package mb.ops.web4log.service;

public class LogContent {
	private String content;
	private long lastIndex;

	public LogContent(String content, long lastIndex) {
		this.content = content;
		this.lastIndex = lastIndex;
	}

	public String getContent() {
		return content;
	}

	public long getLastIndex() {
		return lastIndex;
	}
}
