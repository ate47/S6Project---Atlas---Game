package ssixprojet.server;

public class ServerManager {
	private WebServer webServer;

	public ServerManager(int webServerPort, boolean bufferiseFile) {
		this.webServer = new WebServer(webServerPort, bufferiseFile);
	}

	public WebServer getWebServer() {
		return webServer;
	}

	/**
	 * launch the server
	 */
	public void startServers() {
		webServer.start();
	}

}
