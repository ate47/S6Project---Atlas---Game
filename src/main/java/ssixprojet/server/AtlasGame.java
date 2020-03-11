package ssixprojet.server;

import java.io.File;

import lombok.Getter;
import ssixprojet.common.GameMap;
import ssixprojet.common.MapEdge;
import ssixprojet.common.MapEdge.Orientation;
import ssixprojet.common.config.Config;
import ssixprojet.common.config.ConfigManager;
import ssixprojet.common.entity.Wall;
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
		double factorX = 1. / gameMap.getWidth();
		double factorY = 1. / gameMap.getHeight();
		
		// add world edges
		new Wall(factorX, 1).spawn(mainWorld, 0, 0);
		new Wall(1, factorY).spawn(mainWorld, 0, 0);
		new Wall(factorX, 1).spawn(mainWorld, 0, 1);
		new Wall(1, factorY).spawn(mainWorld, 1, 0);
		
		// add world walls
		for (MapEdge edge : gameMap.getEdges()) {
			if (edge.getOrientation() == Orientation.BOTTOM) {
				new Wall(factorX, factorY * edge.getLength()).spawn(mainWorld, factorX * edge.getX(), factorY * edge.getY());
			} else {
				new Wall(factorX * edge.getLength(), factorY).spawn(mainWorld, factorX * edge.getX(), factorY * edge.getY());
			}
		}

		// TODO: build world
		
	}

	/**
	 * launch the server
	 */
	public void startServer() {
		webServer.start();
	}

}
