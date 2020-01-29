package ssixprojet.server.packet;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import ssixprojet.common.Player;

public class PacketPlayerParser extends ChannelInboundHandlerAdapter {
	private Player player;

	public PacketPlayerParser(Player player) {
		this.player = player;
	}

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		if (msg == null)
			return;

		((PacketClient) msg).handle(player);
		super.channelRead(ctx, msg);
	}

}
