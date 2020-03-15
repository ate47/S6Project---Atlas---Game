package ssixprojet.server.packet;

import ssixprojet.common.Screen;
import ssixprojet.server.connection.ConnectionClient;

public abstract class PacketScreen extends PacketClient {

	@Override
	public void handle(ConnectionClient src) throws Exception {
		if (src instanceof Screen) {
			handle((Screen) src);
		} else
			src.kick("Bad packet type");
	}
	public abstract void handle(Screen screen) throws Exception;
}
