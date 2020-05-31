package ssixprojet.server.packet.server;

import io.netty.buffer.ByteBuf;
import ssixprojet.common.entity.AmmoCrate;
import ssixprojet.server.packet.PacketServer;

public class PacketS11CrateSpawn extends PacketServer {
	private AmmoCrate crate;

	public PacketS11CrateSpawn(AmmoCrate crate) {
		super(0x11, 4 + 8 * 2);
		this.crate = crate;
	}

	@Override
	public void write(ByteBuf buf) {
		buf.writeInt(crate.getEntityId());
		buf.writeDouble(crate.getX());
		buf.writeDouble(crate.getY());
	}

}
