package ssixprojet.server.packet.client;

import ssixprojet.common.Player;
import ssixprojet.server.packet.PacketClient;

public class PacketC01KeepAlive extends PacketClient {

	public PacketC01KeepAlive() {
	}
	
	@Override
	public void handle(Player player) throws Exception {
		player.resetKeepAliveCount();
	}

}
