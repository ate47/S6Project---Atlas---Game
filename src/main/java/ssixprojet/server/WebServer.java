package ssixprojet.server;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import ssixprojet.common.GameMap;
import ssixprojet.server.packet.PacketManager;
import ssixprojet.server.web.HttpServerHandler;
import ssixprojet.server.web.MimeTypeProvider;
import ssixprojet.server.web.WebBuffer;
import ssixprojet.server.web.WebByteBuffer;
import ssixprojet.server.web.WebDirectoryBuffer;
import ssixprojet.server.web.WebFileBuffer;

public class WebServer extends Server {

	private PacketManager manager = new PacketManager();
	private Map<String, WebBuffer> context = new HashMap<>();
	private WebBuffer defaultBuffer = new WebByteBuffer("", "text/plain", "Bad URI".getBytes());
	private final int webServerPort;
	private boolean bufferiseFile;
	private GameMap gameMap;

	public WebServer(int port, boolean bufferiseFile) {
		this.webServerPort = port;
		this.bufferiseFile = bufferiseFile;
		// Register web context
		registerDirectory("/", new File("web"));
		if ((gameMap = GameMap.readMap(new File(new File("config"), "map.json"))) == null)
			throw new RuntimeException("Can't load the game map");
	}

	/**
	 * @return the game map
	 */
	public GameMap getGameMap() {
		return gameMap;
	}

	/**
	 * register all sub files of a directory
	 * 
	 * @param context the context to enter to this directory
	 * @param dir     the directory
	 */
	public void registerDirectory(String context, File dir) {
		// pass if not a directory
		if (!dir.isDirectory())
			return;
		boolean indexFound = false;
		List<String> directory = new ArrayList<>();
		for (File f : dir.listFiles())
			// register sub directory
			if (f.isDirectory()) {
				if (!indexFound)
					directory.add(f.getName() + "/");
				registerDirectory(context + f.getName() + "/", f);
			} else {
				// search the file name and the extension
				String fileName = f.getName();
				int point = fileName.lastIndexOf(".");
				String mime = MimeTypeProvider.getMime(fileName.substring(point + 1));
				// register this file as an index
				if (point != -1 && fileName.substring(0, point).equalsIgnoreCase("index")) {
					registerWebContext(new WebFileBuffer(context, mime, f));
					indexFound = true;
				}

				if (!indexFound)
					directory.add(f.getName());
				// register the file
				registerWebContext(new WebFileBuffer(context + f.getName(), mime, f));
			}
		// register an index if no index exists
		if (!indexFound)
			registerWebContext(new WebDirectoryBuffer(context, directory));
	}

	/**
	 * register a buffer
	 * 
	 * @param buffer the buffer
	 */
	public void registerWebContext(WebBuffer buffer) {
		Objects.requireNonNull(buffer, "File buffer can't be null");
		context.put(buffer.getUri().toLowerCase(), buffer);
	}

	@Override
	protected void startServer() throws Exception {

		// Configure the server.
		EventLoopGroup bossGroup = new NioEventLoopGroup(1);
		EventLoopGroup workerGroup = new NioEventLoopGroup();
		try {
			ServerBootstrap b = new ServerBootstrap();
			// max request connections
			b.option(ChannelOption.SO_BACKLOG, 1024);
			b.group(bossGroup, workerGroup).channel(NioServerSocketChannel.class)
					.handler(new LoggingHandler(LogLevel.INFO)).childHandler(new ChannelInitializer<SocketChannel>() {
						@Override
						protected void initChannel(SocketChannel ch) throws Exception {
							ChannelPipeline pipeline = ch.pipeline();

							pipeline.addLast("httpCodec", new HttpServerCodec());
							pipeline.addLast("httpHandler",
									new HttpServerHandler(bufferiseFile, context, defaultBuffer, manager));
						}
					});

			// binding channel
			Channel ch = b.bind(webServerPort).sync().channel();

			System.out.println("Webserver: http://127.0.0.1:" + webServerPort + '/');
			// wait the end
			ch.closeFuture().sync();
		} finally {
			bossGroup.shutdownGracefully();
			workerGroup.shutdownGracefully();
		}
	}
}
