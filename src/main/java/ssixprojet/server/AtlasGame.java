package ssixprojet.server;

import java.io.File;

import lombok.Getter;
import ssixprojet.common.GameMap;
import ssixprojet.common.MapEdge;
import ssixprojet.common.MapEdge.Orientation;
import ssixprojet.common.SpawnLocation;
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

	@Getter
	private static AtlasGame atlas;

	private GameMap gameMap;
	private WebServer webServer;
	private World mainWorld;
	private double mapFactorX, mapFactorY, playerSizeX, playerSizeY;

	private double height;

	public AtlasGame() {
		atlas = this;
		Config cfg = getConfig();
		this.webServer = new WebServer(cfg.getPort(), cfg.isBufferiseFile());
		if ((gameMap = GameMap.readMap(new File(new File("config"), "map.json"))) == null)
			throw new RuntimeException("Can't load the game map");

		this.mainWorld = new World();
		mapFactorX = 1. / gameMap.getWidth();
		mapFactorY = 1. / gameMap.getHeight();

		int size = gameMap.getPlayerSize();
		playerSizeX = mapFactorX * size;
		playerSizeY = mapFactorY * size;

		// add world edges
		new Wall(mapFactorX, 1).spawn(mainWorld, 0, 0);
		new Wall(1, mapFactorY).spawn(mainWorld, 0, 0);
		new Wall(mapFactorX, 1).spawn(mainWorld, 0, 1);
		new Wall(1, mapFactorY).spawn(mainWorld, 1, 0);

		// add world walls
		for (MapEdge edge : gameMap.getEdges()) {
			if (edge.getOrientation() == Orientation.BOTTOM) {
				new Wall(mapFactorX, mapFactorY * edge.getLength()).spawn(mainWorld, mapFactorX * edge.getX(),
						mapFactorY * edge.getY());
			} else {
				new Wall(mapFactorX * edge.getLength(), mapFactorY).spawn(mainWorld, mapFactorX * edge.getX(),
						mapFactorY * edge.getY());
			}
		}

		int dsize = size / 2;

		// add spawn locations
		for (SpawnLocation location : gameMap.getSpawnLocations())
			// try if a player can fit, if not it's a useless location
			if (location.getWidth() > size && location.getHeight() > size)
				mainWorld.addSpawnLocation((location.getX() + dsize) * mapFactorX,
						(location.getY() + dsize) * mapFactorY, (location.getWidth() - size) * mapFactorX,
						(location.getHeight() - size) * mapFactorX, location.isOutside());
			else
				System.err.println("Can't add the spawn location : " + location);
	}

	/**
	 * launch the server
	 */
	public void startServer() {
		webServer.start();
	}

}
