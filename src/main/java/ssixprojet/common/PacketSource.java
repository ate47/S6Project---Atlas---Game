package ssixprojet.common;

import ssixprojet.server.packet.PacketServer;

public interface PacketSource {
	/**
	 * kick this source from the server ie: close the connection
	 * 
	 * @param msg
	 *            the close connection reason
	 */
	void kick(String msg);

	/**
	 * send a packet to this source
	 * 
	 * @param packet
	 *            the packet to send
	 */
	void sendPacket(PacketServer packet);
}
