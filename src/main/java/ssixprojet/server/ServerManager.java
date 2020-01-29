package ssixprojet.server;

public class ServerManager {
	private WebServer webServer;
	private GameServer gameServer;

	public ServerManager(int gameServerPort, int webServerPort, boolean bufferiseFile) {
		this.gameServer = new GameServer(gameServerPort);
		this.webServer = new WebServer(gameServerPort, bufferiseFile);
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
		webServer.start();
		gameServer.start();
	}

}
