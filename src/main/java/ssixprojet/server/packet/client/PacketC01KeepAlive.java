package ssixprojet.server.packet.client;

import ssixprojet.common.Player;
import ssixprojet.server.packet.PacketPlayer;

public class PacketC01KeepAlive extends PacketPlayer {

	public PacketC01KeepAlive() {
	}
	
	@Override
	public void handle0(Player player) throws Exception {
		player.resetKeepAliveCount();
	}

}
