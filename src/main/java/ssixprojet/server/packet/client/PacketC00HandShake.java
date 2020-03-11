package ssixprojet.server.packet.client;

import io.netty.buffer.ByteBuf;
import ssixprojet.server.packet.PacketPlayer;
import ssixprojet.common.entity.Player;
import ssixprojet.server.packet.PacketManager;

public class PacketC00HandShake extends PacketPlayer {
	public static PacketC00HandShake create(ByteBuf buf) {
		String name = PacketManager.readUTF8String(buf);
		return name == null ? null : new PacketC00HandShake(name);
	}

	private String name;

	private PacketC00HandShake(String name) {
		this.name = name;
	}

	@Override
	public void handle0(Player player) throws Exception {
		player.connect(name);
	}

}
