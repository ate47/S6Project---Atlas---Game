package ssixprojet.server.packet;

import ssixprojet.common.PacketSource;
import ssixprojet.common.Screen;

public abstract class PacketScreen extends PacketClient {

	@Override
	public void handle(PacketSource src) throws Exception {
		if (src instanceof Screen) {
			handle((Screen) src);
		} else
			src.kick("Bad packet type");
	}
	public abstract void handle(Screen screen) throws Exception;
}
