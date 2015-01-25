package mb.ops.web4log.service;

import org.apache.log4j.spi.LoggingEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class Log4jSocketServer {
	private static final Logger logger = LoggerFactory.getLogger(Log4jSocketServer.class);

	private static final AtomicInteger CLIENT_COUNTER = new AtomicInteger(0);
	private static final AtomicBoolean ACCEPTING = new AtomicBoolean(true);
	private static final Map<Integer, ClientHandler> CLIENT_HANDLER_MAP = new ConcurrentHashMap<Integer, ClientHandler>();

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
					setName("Web4Log:ServerSocket:Acceptor");

					try {
						while (ACCEPTING.get()) {
							Socket socket = serverSocket.accept();
							if (ACCEPTING.get()) {
								int clientNo = CLIENT_COUNTER.getAndIncrement();
								ClientHandler clientHandler = new ClientHandler(socket, clientNo);
								CLIENT_HANDLER_MAP.put(clientNo, clientHandler);
								new Thread(clientHandler).start();
							} else {
								try {
									socket.close();
								} catch (IOException e) {
									logger.warn("ServerSocket ACCEPTING: {}", ACCEPTING.get());
								}
							}
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
		logger.info("Log4jServer Stopping");

		ACCEPTING.set(false);

		for (ClientHandler clientHandler : CLIENT_HANDLER_MAP.values()) {
			clientHandler.close();
		}

		CLIENT_HANDLER_MAP.clear();

		try {
			if (serverSocket != null) {
				serverSocket.close();
			}
		} catch (IOException e) {
			logger.warn("Log4jServer.stop(): ", e);
		}

		logger.info("Log4jServer Stopped");
	}

	private static class ClientHandler implements Runnable {
		private int clientNo;
		private String application;
		private Socket socket;

		private ClientHandler(Socket socket, int clientNo) {
			this.socket = socket;
			this.clientNo = clientNo;
		}

		@Override
		public void run() {
			Thread.currentThread().setName(String.format("Web4Log:ClientHandler:%03d", clientNo));

			logger.info("ClientHandler Start: no={}, client={}", clientNo, socket.getInetAddress());

			try {
				ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
				while (socket.isConnected()) {
					LoggingEvent le = (LoggingEvent) ois.readObject();
					String app = LogService.getApplicationOfLoggingEvent(le);
					if (app == null) {
						throw new RuntimeException("Invalid LoggingEvent: No Application Name!");
					}

					if (application == null) {
						application = app;
						LogService.appConnected(application);
						logger.info("Application Connected: {}", application);
					}
					LogService.addLog(application, le);
				}
			} catch (Exception e) {
				logger.warn("ClientHandler", e);
			} finally {
				try {
					if (!socket.isClosed()) {
						socket.close();
					}
				} catch (IOException e) {
					logger.warn("Client Socket Closing", e);
				}
			}

			logger.info("Application Disconnected: {}", application);
			LogService.appDisconnected(application);

			CLIENT_HANDLER_MAP.remove(clientNo);
		}

		private void close() {
			try {
				if (socket != null && !socket.isClosed()) {
					socket.close();
				}
			} catch (IOException e) {
				logger.warn("Closing ClientHandler Socket: no=" + clientNo, e);
			}
		}
	}
}
