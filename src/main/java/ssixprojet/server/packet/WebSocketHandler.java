package ssixprojet.server.packet;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import io.netty.handler.codec.http.websocketx.CloseWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import ssixprojet.server.GameServer;
import ssixprojet.server.connection.Connection;
import ssixprojet.server.connection.ConnectionClient;

public class WebSocketHandler extends ChannelInboundHandlerAdapter implements ChannelFutureListener {
	protected final PacketManager manager;
	private final GameServer gameServer;
	private final Connection src;

	public WebSocketHandler(PacketManager manager, Connection source, GameServer gameServer) {
		this.manager = manager;
		this.src = source;
		this.gameServer = gameServer;
		source.getChannel().closeFuture().addListener(this);
	}

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) {
		if (msg instanceof WebSocketFrame) {
			if (msg instanceof BinaryWebSocketFrame) {
				channelRead0(ctx, (BinaryWebSocketFrame) msg);
			} else if (msg instanceof CloseWebSocketFrame) {
				ctx.close();
			} else {
				ctx.close();
				System.out.println("Unsupported WebSocketFrame: " + msg.getClass().getCanonicalName());
			}
		}
	}

	protected void channelRead0(ChannelHandlerContext ctx, BinaryWebSocketFrame frame) {
		PacketClient packet = manager.buildPacket(frame);
		ConnectionClient client = src.getAttachedClient();
		if (packet == null) {
			client.kick("Bad packet format");
			return;
		}
		gameServer.registerAction(() -> {
			try {
				packet.handle(client);
			} catch (Exception e) {
				e.printStackTrace();
				client.kick("Error while handling the packet");
			}
		});
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		src.onError(cause.getMessage());
	}

	@Override
	public void operationComplete(ChannelFuture future) throws Exception {
		// close operation
		src.onClose();
	}
}
