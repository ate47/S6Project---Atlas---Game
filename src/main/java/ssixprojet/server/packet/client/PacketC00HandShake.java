package ssixprojet.server.packet.client;

import io.netty.buffer.ByteBuf;
import ssixprojet.common.Player;
import ssixprojet.server.packet.PacketPlayer;
import ssixprojet.server.packet.PacketManager;

public class PacketC00HandShake extends PacketPlayer {
	private String name;
	public PacketC00HandShake(ByteBuf buf) {
		name = PacketManager.readUTF8String(buf);
	}

	@Override
	public void handle(Player player) throws Exception {
		player.connect(name);
	}

}
