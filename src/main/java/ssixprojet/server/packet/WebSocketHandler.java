package ssixprojet.server.packet;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import ssixprojet.common.PacketSource;

public class WebSocketHandler extends ChannelInboundHandlerAdapter {
	protected final PacketManager manager;
	private final PacketSource src;

	public WebSocketHandler(PacketManager manager, PacketSource source) {
		this.manager = manager;
		this.src = source;
	}

	protected void channelRead0(ChannelHandlerContext ctx, BinaryWebSocketFrame frame) {
		PacketClient packet = manager.buildPacket(frame);
		if (packet == null) {
			src.kick("Bad packet format");
			return;
		}

		try {
			packet.handle((PacketSource) src);
		} catch (Exception e) {
			e.printStackTrace();
			src.kick("Error while handling the packet");
		}
	}

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) {
		if (msg instanceof WebSocketFrame) {
			if (msg instanceof BinaryWebSocketFrame) {
				channelRead0(ctx, (BinaryWebSocketFrame) msg);
			} else {
				ctx.close();
				System.out.println("Unsupported WebSocketFrame: " + msg.getClass().getCanonicalName());
			}
		}
	}
}
