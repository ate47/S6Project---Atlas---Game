package ssixprojet.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import ssixprojet.common.Player;
import ssixprojet.server.packet.PacketClient;
import ssixprojet.server.packet.PacketClient.PacketBuilder;
import ssixprojet.server.packet.PacketPlayerParser;
import ssixprojet.server.packet.PacketUnparserHandler;
import ssixprojet.server.packet.client.PacketC00HandShake;
import ssixprojet.server.packet.client.PacketC01KeepAlive;

public class GameServer extends Server {
	private PacketBuilder[] packets = new PacketBuilder[256];
	private final int port;

	public GameServer(int port) {
		this.port = port;

		registerPacket(0x00, b -> new PacketC00HandShake());
		registerPacket(0x01, b -> new PacketC01KeepAlive());
	}

	/**
	 * 
	 * @param packetId
	 * @param builder
	 */
	public void registerPacket(int packetId, PacketClient.PacketBuilder builder) {
		if (packets.length <= packetId || packetId < 0)
			throw new IllegalArgumentException("Bad packet id");
		if (packets[packetId] != null)
			throw new IllegalArgumentException("Already registered packet");

		packets[packetId] = builder;
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
							ch.pipeline().addLast(new PacketUnparserHandler(GameServer.this)).addLast(new PacketPlayerParser(p));
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
	
	public PacketClient buildPacket(int type, ByteBuf buffer) {
		if (type < 0 || type >= packets.length) {
			return null;
		}
		// get the packet builder for this type
		PacketBuilder bld = packets[type];
		if (bld == null) {
			return null;
		}

		// build the packet and release the buffer data
		return bld.build(buffer);
	}
}
