package ssixprojet.server;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

public class Server extends ChannelInboundHandlerAdapter {
	private WebServer webServer;
	private GameServer gameServer;
	private Thread gameThread, webThread;

	public Server(int gameServerPort, int webServerPort, boolean bufferiseFile) {
		this.gameServer = new GameServer(gameServerPort);
		this.webServer = new WebServer(gameServerPort, bufferiseFile);
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		cause.printStackTrace();
		ctx.close();
	}

	public GameServer getGameServer() {
		return gameServer;
	}

	public WebServer getWebServer() {
		return webServer;
	}

	/**
	 * launch the server
	 */
	public void startServers() {
		webThread = new Thread(() -> {
			try {
				webServer.startServer();
			} catch (Exception e1) {
				e1.printStackTrace();
			}
		});
		gameThread = new Thread(() -> {
			try {
				gameServer.startServer();
			} catch (Exception e) {
				e.printStackTrace();
			}
		});
		webThread.start();
		gameThread.start();
	}

}
