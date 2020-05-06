package ssixprojet.server.packet.client;

import io.netty.buffer.ByteBuf;
import ssixprojet.server.connection.ConnectionClient;
import ssixprojet.server.packet.PacketClient;
import ssixprojet.server.packet.PacketManager;

public class PacketC00ConnectPlayer extends PacketClient {
	public static PacketC00ConnectPlayer create(ByteBuf buf) {
		String name = PacketManager.readUTF8String(buf);
		return name == null || name.length() > 15 ? null : new PacketC00ConnectPlayer(name);
	}

	private String name;

	private PacketC00ConnectPlayer(String name) {
		this.name = name;
	}

	@Override
	public void handle(ConnectionClient src) throws Exception {
		src.getConnection().connectPlayer(name);
	}

}
