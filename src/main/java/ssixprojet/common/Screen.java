package ssixprojet.common;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import io.netty.handler.codec.http.websocketx.CloseWebSocketFrame;
import ssixprojet.server.connection.Connection;
import ssixprojet.server.connection.ConnectionClient;
import ssixprojet.server.packet.PacketServer;

public class Screen implements ConnectionClient {

	private Connection connection;

	public Screen(Connection connection) {
		this.connection = connection;
	}

	public void disconnect() {
		connection = null;
	}

	@Override
	public Connection getConnection() {
		return connection;
	}

	@Override
	public void kick(String msg) {
		Channel channel = connection.getChannel();
		CloseWebSocketFrame frame = new CloseWebSocketFrame(1000, msg);
		channel.writeAndFlush(frame);
		channel.close();
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
