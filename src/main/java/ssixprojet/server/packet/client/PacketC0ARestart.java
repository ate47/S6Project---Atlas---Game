package ssixprojet.server.packet.client;

import io.netty.buffer.ByteBuf;
import ssixprojet.common.Master;
import ssixprojet.server.AtlasGame;
import ssixprojet.server.packet.PacketMaster;

public class PacketC0ARestart extends PacketMaster {

	public static PacketC0ARestart create(ByteBuf buf) {
		return new PacketC0ARestart();
	}

	@Override
	public void handle(Master screen) throws Exception {
		AtlasGame.getAtlas().restart();
	}

}
