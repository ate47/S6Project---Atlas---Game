package ssixprojet.server.packet.server;

import io.netty.buffer.ByteBuf;
import ssixprojet.common.entity.AmmoCrate;
import ssixprojet.server.packet.PacketServer;

public class PacketS12CrateRemove extends PacketServer {
	private int id;

	public PacketS12CrateRemove(AmmoCrate crate) {
		super(0x12, 4);
		this.id = crate.getEntityId();
	}

	@Override
	public void write(ByteBuf buf) {
		buf.writeInt(id);
	}

}
