package ssixprojet.server.web;

import java.io.IOException;
import java.util.Map;

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
import ssixprojet.server.packet.PacketManager;
import ssixprojet.server.packet.PhoneWebSocketHandler;
import ssixprojet.server.packet.ScreenWebSocketHandler;

public class HttpServerHandler extends SimpleChannelInboundHandler<Object> {
	private static final WebBuffer BAD_WEBSOCKET_RESPONSE = new WebByteBuffer("", MimeTypeProvider.TEXT_PLAIN,
			"BAD WEBSOCKET URI".getBytes());
	private boolean reloadFile;
	private Map<String, WebBuffer> context;
	private WebBuffer defaultContext;
	private PacketManager manager;
	private WebSocketServerHandshaker handshaker;

	public HttpServerHandler(boolean bufferiseFile, Map<String, WebBuffer> context, WebBuffer defaultContext,
			PacketManager manager) throws IOException {
		this.reloadFile = !bufferiseFile;
		this.context = context;
		this.defaultContext = defaultContext;
		this.manager = manager;
	}

	@Override
	public void channelReadComplete(ChannelHandlerContext ctx) {
		ctx.flush();
	}

	@Override
	public void channelRead0(ChannelHandlerContext ctx, Object msg) throws IOException {
		if (msg instanceof HttpRequest) {
			HttpRequest req = (HttpRequest) msg;

			HttpHeaders headers = req.headers();

			System.out.println("[" + req.getProtocolVersion() + "] " + req.getUri());

			if ("Upgrade".equalsIgnoreCase(headers.get(Names.CONNECTION))
					&& "WebSocket".equalsIgnoreCase(headers.get(Names.UPGRADE))) {

				ChannelHandler handler;

				// create a WebSocketHandler for the good actor
				if (req.getUri().equals("/game/phone")) {
					handler = new PhoneWebSocketHandler(manager);
				} else if (req.getUri().equals("/game/screen")) {
					handler = new ScreenWebSocketHandler(manager);
				} else {
					System.out.println("Bad WS actor: " + req.getUri());
					ctx.write(BAD_WEBSOCKET_RESPONSE.buildResponse(false));
					ctx.close();
					return;
				}
				// Adding new handler to the existing pipeline to handle WebSocket Messages
				ctx.pipeline().replace(this, "websocketHandler", handler);

				// Do the Handshake to upgrade connection from HTTP to WebSocket protocol
				handleHandshake(ctx, req);
			} else {
				WebBuffer file = context.getOrDefault(req.getUri().toLowerCase(), defaultContext);

				boolean keepAlive = HttpHeaders.isKeepAlive(req);
				FullHttpResponse response = file.buildResponse(reloadFile);
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
		return "ws://" + req.headers().get("Host") + req.getUri();
	}
}