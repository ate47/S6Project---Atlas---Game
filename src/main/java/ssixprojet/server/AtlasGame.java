package ssixprojet.server;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Supplier;

import ssixprojet.common.GameMap;
import ssixprojet.common.MapEdge;
import ssixprojet.common.MapEdge.Orientation;
import ssixprojet.common.Screen;
import ssixprojet.common.SpawnLocation;
import ssixprojet.common.config.Config;
import ssixprojet.common.config.ConfigManager;
import ssixprojet.common.entity.Player;
import ssixprojet.common.entity.Wall;
import ssixprojet.common.world.World;
import ssixprojet.server.command.CommandManager;
import ssixprojet.server.packet.PacketServer;
import ssixprojet.server.packet.server.PacketS07PlayerSize;

public class AtlasGame {
	private static ConfigManager configManager = new ConfigManager(new File(new File("config"), "server.json"));

	private static AtlasGame atlas;

	public static AtlasGame getAtlas() {
		return atlas;
	}

	public static Config getConfig() {
		return configManager.getConfig();
	}

	public static ConfigManager getConfigManager() {
		return configManager;
	}

	private GameMap gameMap;

	private WebServer webServer;

	private GameServer gameServer;

	private CommandManager commandManager;

	private CommandHandler commandHandler;

	private final Map<Integer, Screen> screens = new HashMap<>();

	private World mainWorld;

	private double mapFactorX, mapFactorY, playerSizeX, playerSizeY;

	private double height;

	public AtlasGame() {
		atlas = this;
		Config cfg = getConfig();
		this.webServer = new WebServer(cfg.getPort(), cfg.isBufferiseFile());
		this.gameServer = new GameServer(this);
		if ((gameMap = GameMap.readMap(new File(new File("config"), "map.json"))) == null)
			throw new RuntimeException("Can't load the game map");

		commandManager = new CommandManager(this);
		commandHandler = new CommandHandler(this);
		this.mainWorld = new World();
		mapFactorX = 1. / gameMap.getWidth();
		mapFactorY = 1. / gameMap.getHeight();

		int size = gameMap.getPlayerSize();
		playerSizeX = mapFactorX * size;
		playerSizeY = mapFactorY * size;

		// add world edges
		new Wall(mapFactorX, 1).spawn(mainWorld, 0, 0);
		new Wall(1, mapFactorY).spawn(mainWorld, 0, 0);
		new Wall(mapFactorX, 1).spawn(mainWorld, 1, 0);
		new Wall(1, mapFactorY).spawn(mainWorld, 0, 1);

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

	public CommandHandler getCommandHandler() {
		return commandHandler;
	}

	public CommandManager getCommandManager() {
		return commandManager;
	}

	public GameMap getGameMap() {
		return gameMap;
	}

	public GameServer getGameServer() {
		return gameServer;
	}

	public double getHeight() {
		return height;
	}

	public World getMainWorld() {
		return mainWorld;
	}

	public double getMapFactorX() {
		return mapFactorX;
	}

	public double getMapFactorY() {
		return mapFactorY;
	}

	public double getPlayerSizeX() {
		return playerSizeX;
	}

	public double getPlayerSizeY() {
		return playerSizeY;
	}

	public Map<Integer, Screen> getScreens() {
		return screens;
	}

	public WebServer getWebServer() {
		return webServer;
	}

	/**
	 * register a screen to the set
	 * 
	 * @param screen
	 *            the screen to register
	 */
	public void registerScreen(Screen screen) {
		synchronized (screens) {
			screens.put(screen.getInternalId(), screen);
		}

		Map<UUID, Player> map = getWebServer().getConnectionManager().getPlayerMap();
		synchronized (map) {
			map.values().stream().map(Player::createPacketSpawn).forEach(screen::sendPacket);
		}

		screen.sendPacket(new PacketS07PlayerSize(playerSizeX, playerSizeY));
	}

	public void sendToAllScreens(Supplier<PacketServer> packetSupplier) {
		synchronized (screens) {
			screens.forEach((id, screen) -> screen.sendPacket(packetSupplier.get()));
		}
	}

	/**
	 * launch the server
	 */
	public void startServer() {
		webServer.start();
		gameServer.start();
		commandHandler.start();
	}

	public void tick() {
		mainWorld.tick();
	}

	public void unregisterScreen(Screen screen) {
		synchronized (screens) {
			screens.remove(screen.getInternalId());
		}
	}

}
