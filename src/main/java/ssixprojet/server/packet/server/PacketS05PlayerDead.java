package ssixprojet.server.packet.server;

import io.netty.buffer.ByteBuf;
import ssixprojet.server.packet.PacketServer;

public class PacketS05PlayerDead extends PacketServer {
	private int id;
	public PacketS05PlayerDead(int id) {
		super(0x05, 4);
		this.id = id;
	}
	
	@Override
	public void write(ByteBuf buf) {
		buf.writeInt(id);
	}

}