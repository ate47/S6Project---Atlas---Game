package ssixprojet.server.packet.client;

import java.util.UUID;

import io.netty.buffer.ByteBuf;
import ssixprojet.server.connection.ConnectionClient;
import ssixprojet.server.packet.PacketClient;
import ssixprojet.server.packet.PacketManager;

public class PacketC03ReconnectPlayer extends PacketClient {
	public static PacketC03ReconnectPlayer create(ByteBuf buf) {
		UUID uuid = PacketManager.readUUID(buf);
		return uuid == null ? null : new PacketC03ReconnectPlayer(uuid);
	}

	private UUID uuid;

	private PacketC03ReconnectPlayer(UUID uuid) {
		this.uuid = uuid;
	}

	@Override
	public void handle(ConnectionClient src) throws Exception {
		src.getConnection().reconnectPlayer(uuid);
	}

}
