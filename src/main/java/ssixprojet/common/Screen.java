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
	public void kick(String msg) {
		if (connection == null)
			return; // TODO better
		Channel channel = connection.getChannel();
		CloseWebSocketFrame frame = new CloseWebSocketFrame(1000, msg);
		channel.writeAndFlush(frame);
		channel.close();
	}

	@Override
	public void sendPacket(PacketServer packet) {
		if (connection == null)
			return; // TODO better
		ByteBuf buffer = Unpooled.buffer(packet.getInitialSize() + 4);
		buffer.writeInt(packet.getPacketId());
		packet.write(buffer);
		BinaryWebSocketFrame frame = new BinaryWebSocketFrame(buffer);
		connection.getChannel().writeAndFlush(frame);
	}

}
