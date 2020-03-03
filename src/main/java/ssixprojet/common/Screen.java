package ssixprojet.common;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import io.netty.handler.codec.http.websocketx.CloseWebSocketFrame;
import lombok.AllArgsConstructor;
import ssixprojet.server.packet.PacketServer;

@AllArgsConstructor
public class Screen implements PacketSource {

	private Channel channel;
	
	@Override
	public void kick(String msg) {
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
		channel.writeAndFlush(frame);
	}

}
