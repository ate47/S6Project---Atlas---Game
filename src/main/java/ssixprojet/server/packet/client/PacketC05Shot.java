package ssixprojet.server.packet.client;

import ssixprojet.common.entity.Player;
import ssixprojet.server.packet.PacketPlayer;

public class PacketC05Shot extends PacketPlayer {
	public PacketC05Shot() {}

	@Override
	public void handle0(Player player) throws Exception {
		player.shooting();
	}

}
