package ssixprojet.server.packet;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import ssixprojet.server.packet.PacketClient.PacketBuilder;
import ssixprojet.server.packet.client.PacketC00HandShake;

public class PacketUnparserHandler extends ChannelInboundHandlerAdapter {

	private PacketBuilder[] packets = new PacketBuilder[256];

	public PacketUnparserHandler() {
		registerPacket(0x00, b -> new PacketC00HandShake());
	}
	
	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		ByteBuf buffer = (ByteBuf) msg;
		int type = buffer.readInt();
		if (type < 0 || type >= packets.length) {
			ctx.close();
			buffer.release();
			return;
		}
		PacketBuilder bld = packets[type];
		if (bld == null) {
			ctx.close();
			buffer.release();
			return;
		}
		PacketClient packet = bld.build(buffer);
		buffer.release();
		super.channelRead(ctx, packet);
	}

	public void registerPacket(int packetId, PacketBuilder builder) {
		if (packets.length <= packetId || packetId < 0)
			throw new IllegalArgumentException("Bad packet id");
		if (packets[packetId] != null)
			throw new IllegalArgumentException("Already registered packet");

		packets[packetId] = builder;
	}
}
