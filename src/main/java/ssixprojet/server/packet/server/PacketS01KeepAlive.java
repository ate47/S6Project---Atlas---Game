package ssixprojet.server.packet.server;

import ssixprojet.server.packet.PacketServer;

public class PacketS01KeepAlive extends PacketServer {

	public PacketS01KeepAlive() {
		super(0x01, 0);
	}

}
