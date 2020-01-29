package ssixprojet.server;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import ssixprojet.common.Player;
import ssixprojet.server.packet.PacketPlayerParser;
import ssixprojet.server.packet.PacketUnparserHandler;
import ssixprojet.server.web.HttpServerHandler;
import ssixprojet.server.web.WebBuffer;
import ssixprojet.server.web.WebByteBuffer;
import ssixprojet.server.web.WebFileBuffer;

public class Server extends ChannelInboundHandlerAdapter {
	private Map<String, WebBuffer> context = new HashMap<>();
	private WebBuffer defaultBuffer = new WebByteBuffer("", "text/plain", "Bad URI".getBytes());
	private final int port;
	private final int webServerPort;
	private Thread webThread, gameThread;
	private boolean bufferiseFile;

	public Server(boolean bufferiseFile) {
		this(2206, 2080, bufferiseFile);

	}

	public Server() {
		this(true);
	}

	public Server(int port, int webServerPort, boolean bufferiseFile) {
		this.port = port;
		this.webServerPort = webServerPort;
		this.bufferiseFile = bufferiseFile;
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		cause.printStackTrace();
		ctx.close();
	}

	private void startGameServer() throws Exception {
		EventLoopGroup bossGroup = new NioEventLoopGroup();
		EventLoopGroup workerGroup = new NioEventLoopGroup();
		try {
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

					.option(ChannelOption.SO_BACKLOG, 128).childOption(ChannelOption.SO_KEEPALIVE, true);

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

	/**
	 * launch the server
	 */
	public void startServers() {
		webThread = new Thread(() -> {
			try {
				startWebServer();
			} catch (Exception e1) {
				e1.printStackTrace();
			}
		});
		gameThread = new Thread(() -> {
			try {
				startGameServer();
			} catch (Exception e) {
				e.printStackTrace();
			}
		});
		webThread.start();
		gameThread.start();
	}

	public void registerWebContext(WebBuffer buffer) {
		Objects.requireNonNull(buffer, "File buffer can't be null");
		context.put(buffer.getUri().toLowerCase(), buffer);
	}

	private void startWebServer() throws Exception {

		// Configure the server.
		EventLoopGroup bossGroup = new NioEventLoopGroup(1);
		EventLoopGroup workerGroup = new NioEventLoopGroup();

		// Register web context
		registerWebContext(new WebFileBuffer("/telephone", "text/html", "web/telephone.html"));
		registerWebContext(new WebFileBuffer("/script.js", "text/javascript", "web/script.js"));
		registerWebContext(new WebFileBuffer("/style.css", "text/css", "web/style.css"));

		try {
			ServerBootstrap b = new ServerBootstrap();
			b.option(ChannelOption.SO_BACKLOG, 1024);
			b.group(bossGroup, workerGroup).channel(NioServerSocketChannel.class)
					.handler(new LoggingHandler(LogLevel.INFO)).childHandler(new ChannelInitializer<SocketChannel>() {
						@Override
						protected void initChannel(SocketChannel ch) throws Exception {
							ch.pipeline().addLast(new HttpServerCodec())
									.addLast(new HttpServerHandler(bufferiseFile, context, defaultBuffer));
						}
					});

			Channel ch = b.bind(webServerPort).sync().channel();

			System.out.println("Webserver: http://127.0.0.1:" + webServerPort + '/');

			ch.closeFuture().sync();
		} finally {
			bossGroup.shutdownGracefully();
			workerGroup.shutdownGracefully();
		}
	}
}
