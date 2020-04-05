package ssixprojet.server.packet.client;

import io.netty.buffer.ByteBuf;
import ssixprojet.common.entity.Player;
import ssixprojet.server.packet.PacketPlayer;

public class PacketC05Shot extends PacketPlayer {
	public static PacketC05Shot create(ByteBuf buffer) {
		if (!buffer.isReadable(1))
			return null;

		boolean shooting = buffer.readBoolean();
		return new PacketC05Shot(shooting);
	}

	private boolean shooting;

	private PacketC05Shot(boolean shooting) {
		this.shooting = shooting;
	}

	public boolean isShooting() {
		return shooting;
	}

	@Override
	public void handle0(Player player) throws Exception {
		player.shooting(shooting);
	}

}
