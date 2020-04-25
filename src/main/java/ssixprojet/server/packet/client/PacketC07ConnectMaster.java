package ssixprojet.server.packet.client;

import io.netty.buffer.ByteBuf;
import ssixprojet.server.connection.ConnectionClient;
import ssixprojet.server.packet.PacketClient;
import ssixprojet.server.packet.PacketManager;

public class PacketC07ConnectMaster extends PacketClient {
	public static PacketC07ConnectMaster create(ByteBuf buf) {
		String password = PacketManager.readUTF8String(buf);
		return password == null ? null : new PacketC07ConnectMaster(password);
	}
	private String password;
	private PacketC07ConnectMaster(String password) {
		this.password = password;
	}
	
	@Override
	public void handle(ConnectionClient src) throws Exception {
		src.getConnection().connectMaster(password);
	}

}
