package ssixprojet.server;

import java.io.File;

import ssixprojet.common.config.Config;
import ssixprojet.common.config.ConfigManager;

public class ServerManager {
	private static ConfigManager configManager = new ConfigManager(new File(new File("config"), "server.json"));

	public static Config getConfig() {
		return configManager.getConfig();
	}

	private WebServer webServer;

	public ServerManager() {
		Config cfg = getConfig();
		this.webServer = new WebServer(cfg.getPort(), cfg.isBufferiseFile());
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
