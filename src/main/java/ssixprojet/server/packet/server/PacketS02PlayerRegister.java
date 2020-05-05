package ssixprojet.server.packet.server;

import java.util.UUID;

import io.netty.buffer.ByteBuf;
import ssixprojet.server.packet.PacketServer;

public class PacketS02PlayerRegister extends PacketServer {
	private UUID playerUUID;
	public PacketS02PlayerRegister(UUID playerUUID) {
		super(0x02, 16);
		this.playerUUID = playerUUID;
	}

	@Override
	public void write(ByteBuf buf) {
		writeUUID(buf, playerUUID);
	}
}
