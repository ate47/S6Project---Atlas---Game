package ssixprojet.server.packet.client;

import io.netty.buffer.ByteBuf;
import ssixprojet.common.Master;
import ssixprojet.server.AtlasGame;
import ssixprojet.server.packet.PacketMaster;
import ssixprojet.utils.MathUtils;

public class PacketC09SendInfection extends PacketMaster {

	public static PacketC09SendInfection create(ByteBuf buf) {
		if (!buf.isReadable(4))
			return null;
		return new PacketC09SendInfection(buf.readInt());
	}

	private int percentage;

	private PacketC09SendInfection(int percentage) {
		this.percentage = MathUtils.clamp(percentage, 0, 100);
	}

	@Override
	public void handle(Master screen) throws Exception {
		AtlasGame.getAtlas().randomInfection(percentage);
	}

}
