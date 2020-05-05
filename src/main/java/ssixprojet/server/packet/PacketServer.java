package ssixprojet.server.packet;

import java.util.UUID;

import org.apache.commons.io.Charsets;

import io.netty.buffer.ByteBuf;

public abstract class PacketServer {

	public static void writeUUID(ByteBuf buf, UUID uuid) {
		buf.writeLong(uuid.getMostSignificantBits());
		buf.writeLong(uuid.getLeastSignificantBits());
	}

	private int initialSize;

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

	public byte[] prepareUTF8String(String str) {
		byte[] bytes = str.getBytes(Charsets.UTF_8);
		this.initialSize += 4 + bytes.length;
		return bytes;
	}

	/**
	 * write the packet data to the byte buffer
	 * 
	 * @param buf
	 */
	public void write(ByteBuf buf) {}

	public void writeUTF8String(ByteBuf buf, byte[] preparedString) {
		buf.writeInt(preparedString.length);
		buf.writeBytes(preparedString);
	}
}
