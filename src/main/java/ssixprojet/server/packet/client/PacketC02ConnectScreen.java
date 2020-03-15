package ssixprojet.server.packet.client;

import io.netty.buffer.ByteBuf;
import ssixprojet.server.connection.ConnectionClient;
import ssixprojet.server.packet.PacketClient;

public class PacketC02ConnectScreen extends PacketClient {
	public static PacketC02ConnectScreen create(ByteBuf buf) {
		return new PacketC02ConnectScreen();
	}

	private PacketC02ConnectScreen() {
	}

	@Override
	public void handle(ConnectionClient src) throws Exception {
		src.getConnection().connectScreen();
	}

}
