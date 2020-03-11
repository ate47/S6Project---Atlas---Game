package ssixprojet.server;

import java.io.File;

import lombok.Getter;
import ssixprojet.common.GameMap;
import ssixprojet.common.config.Config;
import ssixprojet.common.config.ConfigManager;
import ssixprojet.common.world.World;

@Getter
public class AtlasGame {
	private static ConfigManager configManager = new ConfigManager(new File(new File("config"), "server.json"));

	public static Config getConfig() {
		return configManager.getConfig();
	}

	private GameMap gameMap;
	private WebServer webServer;
	private World mainWorld;

	public AtlasGame() {
		Config cfg = getConfig();
		this.webServer = new WebServer(cfg.getPort(), cfg.isBufferiseFile());
		if ((gameMap = GameMap.readMap(new File(new File("config"), "map.json"))) == null)
			throw new RuntimeException("Can't load the game map");
		
		this.mainWorld = new World();
		// TODO: build world
		
	}

	public WebServer getWebServer() {
		return webServer;
	}

	/**
	 * launch the server
	 */
	public void startServer() {
		webServer.start();
	}

}
