package ssixprojet.server.packet;

import io.netty.buffer.ByteBuf;
import ssixprojet.common.Player;

public abstract class PacketClient {
	@FunctionalInterface
	public interface PacketBuilder {
		/**
		 * build the packet from a byte buffer
		 * 
		 * @param buf
		 *            the buffer
		 * @return the built packet
		 */
		PacketClient build(ByteBuf buf);
	}

	public PacketClient() {
	}
	/**
	 * handle the packet
	 * 
	 * @throws Exception
	 *             if the packet throw an exception
	 */
	public abstract void handle(Player player) throws Exception;
}
