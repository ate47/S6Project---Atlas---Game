package ssixprojet.server.packet.client;

import ssixprojet.common.Player;
import ssixprojet.server.packet.PacketClient;

public class PacketC00HandShake extends PacketClient {

	public PacketC00HandShake() {
	}

	@Override
	public void handle(Player player) throws Exception {
		player.connect();
	}

}
