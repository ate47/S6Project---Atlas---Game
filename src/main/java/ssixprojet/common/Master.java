package ssixprojet.common;

import java.util.concurrent.atomic.AtomicInteger;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import io.netty.handler.codec.http.websocketx.CloseWebSocketFrame;
import ssixprojet.server.connection.Connection;
import ssixprojet.server.connection.ConnectionClient;
import ssixprojet.server.packet.PacketServer;

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

	@Override
	public void sendPacket(PacketServer packet) {
		ByteBuf buffer = Unpooled.buffer(packet.getInitialSize() + 4);
		buffer.writeInt(packet.getPacketId());
		packet.write(buffer);
		BinaryWebSocketFrame frame = new BinaryWebSocketFrame(buffer);
		connection.getChannel().writeAndFlush(frame);
	}

}
