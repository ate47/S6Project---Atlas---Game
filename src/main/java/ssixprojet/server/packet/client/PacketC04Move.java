package ssixprojet.server.packet.client;

import io.netty.buffer.ByteBuf;
import lombok.Getter;
import ssixprojet.common.Player;
import ssixprojet.server.packet.PacketPlayer;

@Getter
public class PacketC04Move extends PacketPlayer {
	private double deltaX, deltaY, lookX, lookY;

	public PacketC04Move(ByteBuf buf) {
		deltaX = buf.readDouble();
		deltaY = buf.readDouble();
		lookX = buf.readDouble();
		lookY = buf.readDouble();
	}

	@Override
	public void handle0(Player plr) throws Exception {
		plr.updateMove(this);
	}

}
