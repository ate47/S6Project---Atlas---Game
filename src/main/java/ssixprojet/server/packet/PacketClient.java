package ssixprojet.server.packet;

import ssixprojet.common.PacketSource;

public abstract class PacketClient implements Packet {

	public PacketClient() {}

	/**
	 * handle the packet
	 * 
	 * @throws Exception
	 *             if the packet throw an exception
	 */
	public abstract void handle(PacketSource src) throws Exception;
}
