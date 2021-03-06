package ssixprojet.server.packet.client;

import java.util.UUID;

import io.netty.buffer.ByteBuf;
import ssixprojet.server.connection.ConnectionClient;
import ssixprojet.server.packet.PacketClient;
import ssixprojet.server.packet.PacketManager;

public class PacketC03ReconnectPlayer extends PacketClient {
	public static PacketC03ReconnectPlayer create(ByteBuf buf) {
		UUID uuid = PacketManager.readUUID(buf);
		if (uuid == null)
			return null;
		String name = PacketManager.readUTF8String(buf);
		return name == null || name.length() > 15 ? null : new PacketC03ReconnectPlayer(uuid, name);
	}

	private UUID uuid;
	private String name;

	private PacketC03ReconnectPlayer(UUID uuid, String name) {
		this.uuid = uuid;
		this.name = name;
	}

	@Override
	public void handle(ConnectionClient src) throws Exception {
		src.getConnection().reconnectPlayer(uuid, name);
	}

}
