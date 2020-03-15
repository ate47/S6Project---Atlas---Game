package ssixprojet.server.packet.server;

import java.util.UUID;

import io.netty.buffer.ByteBuf;
import ssixprojet.server.packet.PacketServer;

public class PacketS02PlayerRegister extends PacketServer {
	private UUID uuid;
	public PacketS02PlayerRegister(UUID uuid) {
		super(0x02, 16);
		this.uuid = uuid;
	}

	@Override
	public void write(ByteBuf buf) {
		writeUUID(buf, uuid);
	}
}
