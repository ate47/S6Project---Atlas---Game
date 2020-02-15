package ssixprojet.server.packet;

import io.netty.buffer.ByteBuf;
import ssixprojet.server.packet.PacketClient.PacketBuilder;
import ssixprojet.server.packet.client.PacketC00HandShake;
import ssixprojet.server.packet.client.PacketC01KeepAlive;

public class PacketManager {
	private PacketBuilder[] packets = new PacketBuilder[256];

	public PacketManager() {
		registerPacket(0x00, b -> new PacketC00HandShake());
		registerPacket(0x01, b -> new PacketC01KeepAlive());
	}

	public void registerPacket(int packetId, PacketClient.PacketBuilder builder) {
		if (packets.length <= packetId || packetId < 0)
			throw new IllegalArgumentException("Bad packet id");
		if (packets[packetId] != null)
			throw new IllegalArgumentException("Already registered packet");

		packets[packetId] = builder;
	}

	public PacketClient buildPacket(int type, ByteBuf buffer) {
		if (type < 0 || type >= packets.length) {
			return null;
		}
		// get the packet builder for this type
		PacketBuilder bld = packets[type];
		if (bld == null) {
			return null;
		}

		// build the packet and release the buffer data
		return bld.build(buffer);
	}

}
