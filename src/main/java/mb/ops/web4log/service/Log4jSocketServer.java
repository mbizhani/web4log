package mb.ops.web4log.service;

import org.apache.log4j.spi.LoggingEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class Log4jSocketServer {
	private static final Logger logger = LoggerFactory.getLogger(Log4jSocketServer.class);

	private static ServerSocket serverSocket;

	public static void start() {
		try {
			String bindingAddress = ConfigService.getString("network.binding.address");
			int port = ConfigService.getInt("network.binding.port");
			if (bindingAddress == null) {
				serverSocket = new ServerSocket(port);
			} else {
				serverSocket = new ServerSocket(port, 50, InetAddress.getByName(bindingAddress));
			}

			new Thread() {
				@Override
				public void run() {
					try {
						while (true) {
							Socket socket = serverSocket.accept();
							new Thread(new ClientHandler(socket)).start();
						}
					} catch (IOException e) {
						logger.error("Waiting for connections: ", e);
					}
				}
			}.start();

			logger.info("Start Log4jSocketServer: address = {}, port = {}", bindingAddress, port);
		} catch (IOException e) {
			logger.error("Log4jServer.start:", e);
		}
	}

	public static void stop() {
		try {
			if (serverSocket != null)
				serverSocket.close();
		} catch (IOException e) {
			logger.warn("Log4jServer.stop:", e);
		}
	}

	private static class ClientHandler implements Runnable {
		private Socket socket;
		private String application;

		private ClientHandler(Socket socket) {
			this.socket = socket;
		}

		@Override
		public void run() {
			logger.info("ClientHandler Start: client={}", socket.getInetAddress());

			try {
				ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
				while (socket.isConnected()) {
					LoggingEvent le = (LoggingEvent) ois.readObject();
					Object app = le.getMDC("application");
					if (app == null) {
						throw new RuntimeException("Invalid LoggingEvent: No Application Name!");
					}

					if (application == null) {
						application = app.toString();
						logger.info("Application Connected: {}", application);
					}
					LogCacheService.addLog(application, le);
				}
			} catch (Exception e) {
				logger.error("ClientHandler", e);
			} finally {
				try {
					socket.close();
				} catch (IOException e) {
					logger.warn("Client Socket Closing", e);
				}
			}

			logger.info("Application Disconnected: {}", application);
		}
	}
}
