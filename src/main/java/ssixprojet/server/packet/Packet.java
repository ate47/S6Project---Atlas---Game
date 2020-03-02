package ssixprojet.server.packet;

import io.netty.buffer.ByteBuf;

public interface Packet {
	@FunctionalInterface
	interface PacketBuilder<P extends Packet> {
		/**
		 * build the packet from a byte buffer
		 * 
		 * @param buf
		 *            the buffer
		 * @return the built packet
		 */
		P build(ByteBuf buf);
	}
}
