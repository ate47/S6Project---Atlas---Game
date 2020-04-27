package ssixprojet.server.packet;

import ssixprojet.common.Master;
import ssixprojet.server.connection.ConnectionClient;

public abstract class PacketMaster extends PacketClient {

	@Override
	public void handle(ConnectionClient src) throws Exception {
		if (src instanceof Master) {
			handle((Master) src);
		} else
			src.kick("Bad packet type: " + src.getClass().getCanonicalName());
	}

	public abstract void handle(Master screen) throws Exception;

}
