package ssixprojet.server.packet.client;

import io.netty.buffer.ByteBuf;
import ssixprojet.common.entity.Player;
import ssixprojet.server.packet.PacketPlayer;

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

	private PacketC04Move(double deltaX, double deltaY, double lookX, double lookY) {
		this.deltaX = deltaX;
		this.deltaY = deltaY;
		this.lookX = lookX;
		this.lookY = lookY;
	}

	public double getDeltaX() {
		return deltaX;
	}

	public double getDeltaY() {
		return deltaY;
	}

	public double getLookX() {
		return lookX;
	}

	public double getLookY() {
		return lookY;
	}

	@Override
	public void handle0(Player plr) throws Exception {
		plr.updateMove(this);
	}

}
