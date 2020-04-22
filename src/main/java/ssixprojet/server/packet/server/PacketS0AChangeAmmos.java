package ssixprojet.server.packet.server;

import io.netty.buffer.ByteBuf;
import ssixprojet.server.packet.PacketServer;

public class PacketS0AChangeAmmos extends PacketServer {
	private int ammos;

	public PacketS0AChangeAmmos(int ammos) {
		super(0x0A, 4);
		this.ammos = ammos;
	}

	@Override
	public void write(ByteBuf buf) {
		buf.writeInt(ammos);
	}
}
