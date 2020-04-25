package ssixprojet.server.packet;

import java.util.UUID;

import org.apache.commons.io.Charsets;

import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import ssixprojet.server.packet.Packet.PacketBuilder;
import ssixprojet.server.packet.client.PacketC00ConnectPlayer;
import ssixprojet.server.packet.client.PacketC01KeepAlive;
import ssixprojet.server.packet.client.PacketC02ConnectScreen;
import ssixprojet.server.packet.client.PacketC03ReconnectPlayer;
import ssixprojet.server.packet.client.PacketC04Move;
import ssixprojet.server.packet.client.PacketC05Shot;
import ssixprojet.server.packet.client.PacketC06GuessPlayer;
import ssixprojet.server.packet.client.PacketC07ConnectMaster;
import ssixprojet.server.packet.client.PacketC08LaunchPlayingPhase;

public class PacketManager {
	/**
	 * read an UTF8 string from a buffer
	 * 
	 * @param buf
	 *            the buffer
	 * @return the string or null if can't read enough bytes
	 */
	public static String readUTF8String(ByteBuf buf) {
		if (!buf.isReadable(4))
			return null;

		int size = buf.readInt();
		byte[] bytes = new byte[size];

		if (!buf.isReadable(size))
			return null;
		buf.readBytes(bytes);
		return new String(bytes, Charsets.UTF_8);
	}

	/**
	 * read a UUID from a buffer
	 * 
	 * @param buf
	 *            the buffer
	 * @return the uuid or null if can't read enough bytes
	 */
	public static UUID readUUID(ByteBuf buf) {
		if (!buf.isReadable(16))
			return null;
		int mostTop = buf.readInt();
		int mostBottom = buf.readInt();
		int leastTop = buf.readInt();
		int leastBottom = buf.readInt();
		return new UUID(((long) mostTop << 32) | mostBottom, ((long) leastTop << 32) | leastBottom);
	}

	@SuppressWarnings("unchecked")
	private PacketBuilder<? extends PacketClient>[] packets = new PacketBuilder[256];

	public PacketManager() {
		registerPacket(0x00, PacketC00ConnectPlayer::create);
		registerPacket(0x01, b -> new PacketC01KeepAlive());
		registerPacket(0x02, PacketC02ConnectScreen::create);
		registerPacket(0x03, PacketC03ReconnectPlayer::create);
		registerPacket(0x04, PacketC04Move::create);
		registerPacket(0x05, b -> new PacketC05Shot());
		registerPacket(0x06, PacketC06GuessPlayer::create);
		registerPacket(0x07, PacketC07ConnectMaster::create);
		registerPacket(0x08, b -> new PacketC08LaunchPlayingPhase());
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
			if (!buffer.isReadable(4))
				return null;
			int type = (int) buffer.readUnsignedInt(); // read u32

			return buildPacket(type, buffer);
		} catch (Exception e) {
			return null;
		}
	}

	/**
	 * register a {@link PacketBuilder} for client packets
	 * 
	 * @param packetId
	 *            the packet id
	 * @param builder
	 *            the builder
	 */
	public void registerPacket(int packetId, PacketBuilder<? extends PacketClient> builder) {
		if (packets.length <= packetId || packetId < 0)
			throw new IllegalArgumentException("Bad packet id");
		if (packets[packetId] != null)
			throw new IllegalArgumentException("Already registered packet");

		packets[packetId] = builder;
	}

}
