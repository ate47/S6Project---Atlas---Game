package ssixprojet.server.packet.server;

import ssixprojet.server.packet.PacketServer;

public class PacketS00HandShake extends PacketServer {

	public PacketS00HandShake() {
		super(0x00, 0);
	}

}
