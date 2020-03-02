package ssixprojet.server.packet;

import org.apache.commons.io.Charsets;

import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import ssixprojet.server.packet.Packet.PacketBuilder;
import ssixprojet.server.packet.client.PacketC00HandShake;
import ssixprojet.server.packet.client.PacketC01KeepAlive;

public class PacketManager {
	/**
	 * read an UTF8 string from a buffer
	 * 
	 * @param buf
	 *            the buffer
	 * @return the string
	 */
	public static String readUTF8String(ByteBuf buf) {
		int size = buf.readInt();
		byte[] bytes = new byte[size];

		buf.readBytes(bytes);
		return new String(bytes, Charsets.UTF_8);
	}

	@SuppressWarnings("unchecked")
	private PacketBuilder<? extends PacketClient>[] packets = new PacketBuilder[256];

	public PacketManager() {
		registerPacket(0x00, PacketC00HandShake::new);
		registerPacket(0x01, b -> new PacketC01KeepAlive());
	}

	public PacketClient buildPacket(int type, ByteBuf buffer) {
		if (type < 0 || type >= packets.length) {
			return null;
		}
		// get the packet builder for this type
		PacketBuilder<?> bld = packets[type];
		if (bld == null) {
			return null;
		}

		// build the packet and release the buffer data
		return (PacketClient) bld.build(buffer);
	}

	/**
	 * build a packet from a {@link TextWebSocketFrame}
	 * 
	 * @param frame
	 *            the frame
	 * @return the packet or null if an error occurred
	 */
	public PacketClient buildPacket(BinaryWebSocketFrame frame) {
		ByteBuf buffer = frame.content();
		try {
			int type = (int) buffer.readUnsignedInt(); // read u32

			return buildPacket(type, buffer);
		} catch (Exception e) {
			return null;
		}
	}

	public void registerPacket(int packetId, PacketBuilder<? extends PacketClient> builder) {
		if (packets.length <= packetId || packetId < 0)
			throw new IllegalArgumentException("Bad packet id");
		if (packets[packetId] != null)
			throw new IllegalArgumentException("Already registered packet");

		packets[packetId] = builder;
	}

}
