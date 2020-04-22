package ssixprojet.server.packet.server;

import io.netty.buffer.ByteBuf;
import ssixprojet.server.packet.PacketServer;

public class PacketS06PlayerType extends PacketServer {
	private int type;
	private int playerID;

	public PacketS06PlayerType(int type, int playerID) {
		super(0x06, 8);
		this.type = type;
		this.playerID = playerID;
	}

	@Override
	public void write(ByteBuf buf) {
		buf.writeInt(type);
		buf.writeInt(playerID);
	}

}
