package ssixprojet.server.packet;

import io.netty.buffer.ByteBuf;

public abstract class PacketServer {

	private final int initialSize;
	private final int id;

	public PacketServer(int id, int initialSize) {
		if (id < 0)
			throw new IllegalArgumentException("an id can't be negative");
		this.id = id;
		if (initialSize < 0)
			throw new IllegalArgumentException("an initial size can't be negative");
		this.initialSize = initialSize;
	}

	/**
	 * @return the initial size
	 */
	public int getInitialSize() {
		return initialSize;
	}

	/**
	 * @return the packet id
	 */
	public int getPacketId() {
		return id;
	}

	/**
	 * write the packet data to the byte buffer
	 * 
	 * @param buf
	 */
	public void write(ByteBuf buf) {
	}
}
