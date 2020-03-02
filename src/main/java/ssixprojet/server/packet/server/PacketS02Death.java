package ssixprojet.server.packet.server;

import org.apache.commons.io.Charsets;

import io.netty.buffer.ByteBuf;
import ssixprojet.server.packet.PacketServer;

public class PacketS02Death extends PacketServer {
	private byte[] data;
	public PacketS02Death(String msg) {
		super(0x02, 4 + msg.length() * 3);
		msg.getBytes(Charsets.UTF_8);
	}
	
	@Override
	public void write(ByteBuf buf) {
		buf.writeInt(data.length);
		buf.writeBytes(data);
	}

}
