package ssixprojet.common;

import ssixprojet.server.packet.PacketServer;

public interface PacketSource {
	void kick(String msg);
	void sendPacket(PacketServer packet);
}
