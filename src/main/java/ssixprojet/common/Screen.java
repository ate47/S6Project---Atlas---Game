package ssixprojet.common;

import java.util.concurrent.atomic.AtomicInteger;

import io.netty.channel.Channel;
import io.netty.handler.codec.http.websocketx.CloseWebSocketFrame;
import ssixprojet.server.connection.Connection;
import ssixprojet.server.connection.ConnectionClient;

public class Screen implements ConnectionClient {
	private static final AtomicInteger COUNT = new AtomicInteger(0);
	private Connection connection;
	private final int internalId;

	public Screen(Connection connection) {
		this.connection = connection;
		this.internalId = COUNT.incrementAndGet();
	}

	public void disconnect() {
		connection = null;
	}

	@Override
	public Connection getConnection() {
		return connection;
	}
	
	public int getInternalId() {
		return internalId;
	}
	@Override
	public void onDisconnect(String reason) {
		System.out.println("[Screen#" + getInternalId() + "] diconnected : " + reason);
	}

	@Override
	public void kick(String msg) {
		if (connection == null)
			return; // TODO better
		Channel channel = connection.getChannel();
		CloseWebSocketFrame frame = new CloseWebSocketFrame(1000, msg);
		channel.writeAndFlush(frame);
		channel.close();
	}

}
