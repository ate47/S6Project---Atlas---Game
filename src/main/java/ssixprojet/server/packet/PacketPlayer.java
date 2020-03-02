package ssixprojet.server.packet;

import ssixprojet.common.PacketSource;
import ssixprojet.common.Player;

public abstract class PacketPlayer extends PacketClient {
	
	@Override
	public void handle(PacketSource src) throws Exception {
		if (src instanceof Player) {
			handle((Player) src);
		} else
			src.kick("Bad packet type");
	}
	public abstract void handle(Player player) throws Exception;
}
