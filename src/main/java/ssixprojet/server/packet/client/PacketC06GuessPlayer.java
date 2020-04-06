package ssixprojet.server.packet.client;

import java.util.Map;

import io.netty.buffer.ByteBuf;
import ssixprojet.common.Screen;
import ssixprojet.common.entity.Player;
import ssixprojet.server.AtlasGame;
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
		Map<Integer, Player> map = AtlasGame.getAtlas().getWebServer().getConnectionManager().getPlayerInternalMap();
		synchronized (map) {
			Player p = map.get(playerId);
			if (p != null)
				screen.sendPacket(p.createPacketSpawn());
		}
	}

}
