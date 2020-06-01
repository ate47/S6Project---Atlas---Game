package ssixprojet.server.connection;

import ssixprojet.server.packet.PacketServer;

public interface ConnectionClient {
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
	default void sendPacket(PacketServer packet) {
		Connection c = getConnection();
		if (c != null)
			c.sendPacket(packet);
	}
	
	Connection getConnection();
	
	void onDisconnect(String reason);
}
