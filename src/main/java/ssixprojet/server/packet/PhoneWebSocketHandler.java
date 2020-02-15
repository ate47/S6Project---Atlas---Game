package ssixprojet.server.packet;

import org.apache.commons.io.Charsets;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;

public class PhoneWebSocketHandler extends WebSocketHandler {

	public PhoneWebSocketHandler(PacketManager manager) {
		super(manager);
	}

	@Override
	protected void channelRead0(ChannelHandlerContext ctx, TextWebSocketFrame frame) {
		System.out.println("Phone WS: " + frame.content().toString(Charsets.UTF_8));
	}

}
