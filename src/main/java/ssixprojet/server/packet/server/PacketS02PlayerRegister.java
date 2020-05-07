package ssixprojet.server.packet.server;

import java.util.UUID;

import io.netty.buffer.ByteBuf;
import ssixprojet.server.packet.PacketServer;

public class PacketS02PlayerRegister extends PacketServer {
	private UUID playerUUID;
	private int id;
	public PacketS02PlayerRegister(UUID playerUUID, int id) {
		super(0x02, 20);
		this.playerUUID = playerUUID;
		this.id = id;
	}

	@Override
	public void write(ByteBuf buf) {
		writeUUID(buf, playerUUID);
		buf.writeInt(id);
	}
}
