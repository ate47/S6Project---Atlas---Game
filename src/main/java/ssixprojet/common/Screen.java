package ssixprojet.common;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import lombok.AllArgsConstructor;
import ssixprojet.server.packet.PacketServer;
import ssixprojet.server.packet.server.PacketS02Death;

@AllArgsConstructor
public class Screen implements PacketSource {

	private Channel channel;
	
	@Override
	public void kick(String msg) {
		sendPacket(new PacketS02Death(msg));
		channel.close();
	}

	@Override
	public void sendPacket(PacketServer packet) {
		ByteBuf buffer = Unpooled.buffer(packet.getInitialSize() + 4);
		buffer.writeInt(packet.getPacketId());
		packet.write(buffer);
		channel.write(buffer);
		channel.flush();
	}

}
