package ssixprojet.server.packet.client;

import io.netty.buffer.ByteBuf;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import ssixprojet.common.entity.Player;
import ssixprojet.server.packet.PacketPlayer;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class PacketC04Move extends PacketPlayer {
	public static PacketC04Move create(ByteBuf buf) {
		if (!buf.isReadable(8 * 4))
			return null;
		double deltaX, deltaY, lookX, lookY;
		deltaX = buf.readDouble();
		deltaY = buf.readDouble();
		lookX = buf.readDouble();
		lookY = buf.readDouble();
		
		return new PacketC04Move(deltaX, deltaY, lookX, lookY);
	}
	private double deltaX, deltaY, lookX, lookY;

	@Override
	public void handle0(Player plr) throws Exception {
		plr.updateMove(this);
	}

}
