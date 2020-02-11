package ssixprojet.server;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import ssixprojet.server.web.HttpServerHandler;
import ssixprojet.server.web.MimeTypeProvider;
import ssixprojet.server.web.WebBuffer;
import ssixprojet.server.web.WebByteBuffer;
import ssixprojet.server.web.WebFileBuffer;

public class WebServer extends Server {

	private Map<String, WebBuffer> context = new HashMap<>();
	private WebBuffer defaultBuffer = new WebByteBuffer("", "text/plain", "Bad URI".getBytes());
	private final int webServerPort;
	private boolean bufferiseFile;

	public WebServer(int port, boolean bufferiseFile) {
		this.webServerPort = port;
		this.bufferiseFile = bufferiseFile;
		// Register web context
		registerDirectory("/", new File("web"));
		registerWebContext(new WebFileBuffer("/", MimeTypeProvider.getMime("index.html"), "web/index.html"));
	}

	public void registerDirectory(String context, File d) {
		if (!d.isDirectory())
			return;
		for (File f : d.listFiles())
			if (f.isDirectory())
				registerDirectory(context + f.getName() + "/", f);
			else
				registerWebContext(new WebFileBuffer(context + f.getName(), MimeTypeProvider.getMime(f.getName()), f));
	}

	public void registerWebContext(WebBuffer buffer) {
		Objects.requireNonNull(buffer, "File buffer can't be null");
		context.put(buffer.getUri().toLowerCase(), buffer);
	}

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
							ch.pipeline().addLast(new HttpServerCodec())
									.addLast(new HttpServerHandler(bufferiseFile, context, defaultBuffer));
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
