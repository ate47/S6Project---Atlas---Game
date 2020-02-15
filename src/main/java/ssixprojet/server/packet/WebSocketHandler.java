package ssixprojet.server.packet;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;

public abstract class WebSocketHandler extends ChannelInboundHandlerAdapter {
	protected final PacketManager manager;

	public WebSocketHandler(PacketManager manager) {
		this.manager = manager;
	}

	protected abstract void channelRead0(ChannelHandlerContext ctx, TextWebSocketFrame frame);

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) {

		if (msg instanceof WebSocketFrame) {
			if (msg instanceof TextWebSocketFrame) {
				channelRead0(ctx, (TextWebSocketFrame) msg);
			} else {
				ctx.close();
				System.out.println("Unsupported WebSocketFrame");
			}
		}
	}
}
