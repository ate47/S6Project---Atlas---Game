package ssixprojet.server.packet.server;

import io.netty.buffer.ByteBuf;
import ssixprojet.server.packet.PacketServer;

public class PacketS06PlayerType extends PacketServer {
	private int type;

	public PacketS06PlayerType(int type) {
		super(0x06, 4);
		this.type = type;
	}

	@Override
	public void write(ByteBuf buf) {
		buf.writeInt(type);
	}

}
