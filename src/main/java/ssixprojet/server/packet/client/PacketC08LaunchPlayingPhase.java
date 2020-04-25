package ssixprojet.server.packet.client;

import ssixprojet.common.GamePhase;
import ssixprojet.common.Master;
import ssixprojet.server.AtlasGame;
import ssixprojet.server.packet.PacketMaster;

public class PacketC08LaunchPlayingPhase extends PacketMaster {
	public PacketC08LaunchPlayingPhase() {}

	@Override
	public void handle(Master screen) throws Exception {
		AtlasGame.getAtlas().setPhase(GamePhase.PLAYING);
	}

}
