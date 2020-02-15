package ssixprojet.server.packet;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

public class PacketValidateHandler extends ChannelInboundHandlerAdapter {
	private PacketManager manager;
	public PacketValidateHandler(PacketManager manager) {
		this.manager = manager;
	}

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		ByteBuf buffer = (ByteBuf) msg;

		// get packet type
		int type = buffer.readInt();
		
		PacketClient packet = manager.buildPacket(type, buffer);
		buffer.release();

		// send to the next handler the packet
		super.channelRead(ctx, packet);
	}
}
