package ssixprojet.server.packet;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import ssixprojet.server.GameServer;

public class PacketUnparserHandler extends ChannelInboundHandlerAdapter {
	private GameServer server;
	public PacketUnparserHandler(GameServer server) {
		this.server = server;
	}

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		ByteBuf buffer = (ByteBuf) msg;

		// get packet type
		int type = buffer.readInt();
		
		PacketClient packet = server.buildPacket(type, buffer);
		buffer.release();

		// send to the next handler the packet
		super.channelRead(ctx, packet);
	}
}
