package ssixprojet.server.web;

import java.io.IOException;

import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpHeaders.Names;
import io.netty.handler.codec.http.HttpHeaders.Values;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshaker;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshakerFactory;
import ssixprojet.server.AtlasGame;
import ssixprojet.server.GameServer;
import ssixprojet.server.WebServer;
import ssixprojet.server.packet.WebSocketHandler;

public class HttpServerHandler extends SimpleChannelInboundHandler<Object> {
	private static final WebBuffer BAD_WEBSOCKET_RESPONSE = new WebByteBuffer("", MimeTypeProvider.TEXT_PLAIN,
			"BAD WEBSOCKET URI".getBytes());
	private WebServer webServer;
	private GameServer gameServer;
	private WebSocketServerHandshaker handshaker;

	public HttpServerHandler(AtlasGame atlas) throws IOException {
		this.webServer = atlas.getWebServer();
		this.gameServer = atlas.getGameServer();
	}

	@Override
	public void channelReadComplete(ChannelHandlerContext ctx) {
		ctx.flush();
	}

	private static String getUri(HttpRequest req) {
		String uri = req.getUri();
		int index = req.getUri().indexOf('?');

		return index == -1 ? uri : uri.substring(0, index);
	}

	@Override
	public void channelRead0(ChannelHandlerContext ctx, Object msg) throws IOException {
		if (msg instanceof HttpRequest) {
			HttpRequest req = (HttpRequest) msg;

			HttpHeaders headers = req.headers();

			String uri = getUri(req);
			System.out.println("[" + req.getProtocolVersion() + "] " + uri);

			// check if this connection is a websocket
			if ("Upgrade".equalsIgnoreCase(headers.get(Names.CONNECTION))
					&& "WebSocket".equalsIgnoreCase(headers.get(Names.UPGRADE))) {

				// Websocket request

				ChannelHandler handler;

				// create a WebSocketHandler for the good actor
				if (uri.equals("/game")) {
					handler = new WebSocketHandler(webServer.getPacketManager(),
							webServer.getConnectionManager().createConnection(ctx.channel()), gameServer);
				} else {
					System.out.println("Bad WS actor: " + uri);
					ctx.write(BAD_WEBSOCKET_RESPONSE.buildResponse(false));
					ctx.close();
					return;
				}
				// Adding new handler to the existing pipeline to handle WebSocket Messages
				ctx.pipeline().replace(this, "websocketHandler", handler);

				// Do the Handshake to upgrade connection from HTTP to WebSocket protocol
				handleHandshake(ctx, req);
			} else {
				// simple HTTP request
				WebBuffer file = webServer.getContext().get(uri.toLowerCase());
				if (file == null)
					file = webServer.getDefaultBuffer();

				boolean keepAlive = HttpHeaders.isKeepAlive(req);
				FullHttpResponse response = file.buildResponse(!webServer.isBufferiseFile());
				if (!keepAlive) {
					ctx.write(response).addListener(ChannelFutureListener.CLOSE);
				} else {
					response.headers().set(Names.CONNECTION, Values.KEEP_ALIVE);
					ctx.write(response);
				}
			}
		}

	}

	/* Do the handshaking for WebSocket request */
	protected void handleHandshake(ChannelHandlerContext ctx, HttpRequest req) {
		WebSocketServerHandshakerFactory wsFactory = new WebSocketServerHandshakerFactory(getWebSocketURL(req), null,
				true);
		handshaker = wsFactory.newHandshaker(req);
		if (handshaker == null) {
			WebSocketServerHandshakerFactory.sendUnsupportedVersionResponse(ctx.channel());
		} else {
			handshaker.handshake(ctx.channel(), req);
		}
	}

	protected String getWebSocketURL(HttpRequest req) {
		return "ws://" + req.headers().get("Host") + getUri(req);
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		// nothing to do
	}
}
