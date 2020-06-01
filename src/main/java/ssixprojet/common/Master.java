package ssixprojet.common;

import java.util.concurrent.atomic.AtomicInteger;

import io.netty.channel.Channel;
import io.netty.handler.codec.http.websocketx.CloseWebSocketFrame;
import ssixprojet.server.connection.Connection;
import ssixprojet.server.connection.ConnectionClient;

public class Master implements ConnectionClient {
	private static final AtomicInteger COUNT = new AtomicInteger(0);
	private Connection connection;
	private final int id;

	public Master(Connection connection) {
		this.connection = connection;
		this.id = COUNT.incrementAndGet();
	}

	@Override
	public Connection getConnection() {
		return connection;
	}
	
	public int getId() {
		return id;
	}

	@Override
	public void kick(String msg) {
		Channel channel = connection.getChannel();
		CloseWebSocketFrame frame = new CloseWebSocketFrame(1000, msg);
		channel.writeAndFlush(frame);
		channel.close();
	}

	@Override
	public void onDisconnect(String reason) {
		System.out.println("[Master] diconnected : " + reason);
	}

}
