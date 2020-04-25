package ssixprojet.server.packet.server;

import io.netty.buffer.ByteBuf;
import ssixprojet.common.GamePhase;
import ssixprojet.server.packet.PacketServer;

public class PacketS0BSetGamePhase extends PacketServer {
	private int id;

	public PacketS0BSetGamePhase(GamePhase phase) {
		super(0x0b, 4);
		id = phase.getId();
	}

	@Override
	public void write(ByteBuf buf) {
		buf.writeInt(id);
	}

}
