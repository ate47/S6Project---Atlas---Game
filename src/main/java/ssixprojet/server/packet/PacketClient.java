package ssixprojet.server.packet;

import ssixprojet.server.connection.ConnectionClient;

public abstract class PacketClient implements Packet {

	public PacketClient() {}

	/**
	 * handle the packet
	 * 
	 * @throws Exception
	 *             if the packet throw an exception
	 */
	public abstract void handle(ConnectionClient src) throws Exception;
}
