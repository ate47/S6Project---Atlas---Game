package ssixprojet.server.packet;

import ssixprojet.common.entity.Player;
import ssixprojet.server.connection.ConnectionClient;

public abstract class PacketPlayer extends PacketClient {
	
	@Override
	public void handle(ConnectionClient src) throws Exception {
		if (src instanceof Player) {
			handle0((Player) src);
		} else
			src.kick("Bad packet type");
	}
	public abstract void handle0(Player player) throws Exception;
}
