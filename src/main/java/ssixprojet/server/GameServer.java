package ssixprojet.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import ssixprojet.common.Player;
import ssixprojet.server.packet.PacketPlayerParser;
import ssixprojet.server.packet.PacketUnparserHandler;

public class GameServer extends Server {
	private final int port;

	public GameServer(int port) {
		this.port = port;
	}

	
	protected void startServer() throws Exception {
		EventLoopGroup bossGroup = new NioEventLoopGroup();
		EventLoopGroup workerGroup = new NioEventLoopGroup();
		try {
			// start server
			System.out.println("Starting server...");
			ServerBootstrap b = new ServerBootstrap();
			b.group(bossGroup, workerGroup).channel(NioServerSocketChannel.class)
					.childHandler(new ChannelInitializer<SocketChannel>() {
						@Override
						public void initChannel(SocketChannel ch) throws Exception {
							Player p = new Player(ch);
							ch.pipeline().addLast(new PacketUnparserHandler()).addLast(new PacketPlayerParser(p));
						}
					})

					.option(ChannelOption.SO_BACKLOG, 1024).childOption(ChannelOption.SO_KEEPALIVE, true);

			System.out.println("Binding connections...");
			// Bind and start to accept incoming connections.
			ChannelFuture f = b.bind(port).sync();

			// Wait until the server socket is closed.
			f.channel().closeFuture().sync();
		} finally {
			System.out.println("Shutdown...");
			workerGroup.shutdownGracefully();
			bossGroup.shutdownGracefully();
		}

	}
}
