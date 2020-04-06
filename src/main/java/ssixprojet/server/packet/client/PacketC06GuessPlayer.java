package ssixprojet.server.packet.client;

import io.netty.buffer.ByteBuf;
import ssixprojet.common.Screen;
import ssixprojet.server.packet.PacketScreen;

public class PacketC06GuessPlayer extends PacketScreen {

	public static PacketC06GuessPlayer create(ByteBuf buffer) {
		if (!buffer.isReadable(4))
			return null;

		return new PacketC06GuessPlayer(buffer.readInt());
	}

	private PacketC06GuessPlayer(int playerId) {
		this.playerId = playerId;
	}

	private int playerId;

	@Override
	public void handle(Screen screen) throws Exception {
		// TODO send PacketS03PlayerSpawn packet for playerId
	}

}
