package mb.ops.web4log.service;

import java.io.Serializable;

public class AppInfo implements Serializable {
	private String name;
	private boolean connected = true;

	public AppInfo(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public boolean isConnected() {
		return connected;
	}

	public void setConnected(boolean connected) {
		this.connected = connected;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		AppInfo appInfo = (AppInfo) o;

		if (name != null ? !name.equals(appInfo.name) : appInfo.name != null) return false;

		return true;
	}

	@Override
	public int hashCode() {
		return name != null ? name.hashCode() : 0;
	}
}
