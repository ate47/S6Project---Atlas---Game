package ssixprojet.server;

import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.UUID;

import ssixprojet.common.GameMap;
import ssixprojet.common.GamePhase;
import ssixprojet.common.MapEdge;
import ssixprojet.common.MapEdge.Orientation;
import ssixprojet.common.Master;
import ssixprojet.common.Screen;
import ssixprojet.common.SpawnLocation;
import ssixprojet.common.config.Config;
import ssixprojet.common.config.ConfigManager;
import ssixprojet.common.entity.AmmoCrate;
import ssixprojet.common.entity.Player;
import ssixprojet.common.entity.PlayerType;
import ssixprojet.common.entity.Wall;
import ssixprojet.common.world.World;
import ssixprojet.server.command.CommandManager;
import ssixprojet.server.packet.PacketServer;
import ssixprojet.server.packet.server.PacketS07PlayerSize;
import ssixprojet.server.packet.server.PacketS0BSetGamePhase;
import ssixprojet.server.packet.server.PacketS10ScoreScreen;

public class AtlasGame {
	private static final Player[] EMPTY_ARRAY = new Player[0];
	private static final Random RANDOM = new Random();
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

	private Map<Integer, AmmoCrate> crates = new HashMap<>();
	private Player[] infectedScore = EMPTY_ARRAY, survivorScore = EMPTY_ARRAY;

	private UUID serverUUID = UUID.randomUUID();

	private GameMap gameMap;

	private WebServer webServer;

	private GameServer gameServer;
	private final Object SCREEN_PACKET_LOCKER = new Object() {};
	private PacketServer screenPacket = null;

	private CommandManager commandManager;

	private CommandHandler commandHandler;

	private final Map<Integer, Screen> screens = new HashMap<>();

	private World mainWorld;

	private double mapFactorX, mapFactorY, playerSizeX, playerSizeY;

	private double height;

	private GamePhase phase;

	private long gameStartTime, gameEndTime;

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
		new Wall(mapFactorX, 1).spawn(mainWorld, 1 - mapFactorX, 0);
		new Wall(1, mapFactorY).spawn(mainWorld, 0, 1 - mapFactorY);

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
						(location.getHeight() - size) * mapFactorY, location.isOutside());
			else
				System.err.println("Can't add the spawn location : " + location);

		setPhase(GamePhase.WAITING);
	}

	public PacketS10ScoreScreen createScorePacket() {
		return createScorePacket0(Math.min(Math.min(10, survivorScore.length), survivorScore.length));
	}

	private PacketS10ScoreScreen createScorePacket0(int maxPlayer) {
		return new PacketS10ScoreScreen(maxPlayer, infectedScore, survivorScore);
	}

	public CommandHandler getCommandHandler() {
		return commandHandler;
	}

	public CommandManager getCommandManager() {
		return commandManager;
	}

	public Map<Integer, AmmoCrate> getCrates() {
		return crates;
	}

	public long getGameEndTime() {
		return gameEndTime;
	}

	public GameMap getGameMap() {
		return gameMap;
	}

	public GameServer getGameServer() {
		return gameServer;
	}

	public long getGameStartTime() {
		return gameStartTime;
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

	public GamePhase getPhase() {
		return phase;
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

	public UUID getServerUUID() {
		return serverUUID;
	}

	public WebServer getWebServer() {
		return webServer;
	}

	/**
	 * infect a certain percentage of players, this method guaranty at least 1
	 * infected
	 * 
	 * @param percentage
	 *            infection percentage
	 */
	public void randomInfection(int percentage) {
		Player[] players = getWebServer().getConnectionManager().getPlayerMap().values().stream()
				.filter(p -> p.isConnected() && p.getType() == PlayerType.SURVIVOR).toArray(Player[]::new);

		if (players.length == 0)
			return;

		int toInfect = Math.max(1, percentage * players.length / 100);

		Player p;
		for (int i = 0; i < toInfect; i++) {
			int chosen = i + RANDOM.nextInt(toInfect - i);

			p = players[chosen];
			players[chosen] = players[i];
			players[i] = p;

			p.infect();
		}
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
			map.values().stream().filter(Player::isConnected).map(Player::createPacketSpawn)
					.forEach(screen::sendPacket);
		}

		screen.sendPacket(new PacketS07PlayerSize(playerSizeX, playerSizeY));
		screen.sendPacket(createScorePacket());
	}

	public void restart() {
		Map<Integer, Player> map = getWebServer().getConnectionManager().getPlayerInternalMap();

		setPhase(GamePhase.WAITING);

		infectedScore = survivorScore = EMPTY_ARRAY;
		serverUUID = UUID.randomUUID();

		synchronized (map) {
			map.values().stream().filter(Player::isConnected).forEach(p -> p.kick("restarting..."));
			map.clear();
		}

		Map<UUID, Player> mapu = getWebServer().getConnectionManager().getPlayerMap();

		synchronized (mapu) {
			mapu.clear();
		}

		synchronized (crates) {
			Iterator<Entry<Integer, AmmoCrate>> it = crates.entrySet().iterator();
			while (it.hasNext()) {
				it.next().getValue().kill(false);
				it.remove();
			}
		}
	}

	public void spawnRandomCrate() {
		AmmoCrate crate = new AmmoCrate(getConfig().getCrateAmmos());
		crates.put(crate.getEntityId(), crate);
		getMainWorld().spawnEntityOutsideAtRandomLocationWithoutMin(crate);
		System.out.println("New crate spawning: (" + crate.getX() + ", " + crate.getY() + ")");
	}

	public void spawnCrate(int ammos, double x, double y) {
		AmmoCrate crate = new AmmoCrate(ammos);
		crates.put(crate.getEntityId(), crate);
		crate.spawn(getMainWorld(), x, y);
	}

	public void sendScoreScreenPacket() {
		int maxPlayer = Math.min(Math.min(10, survivorScore.length), survivorScore.length);
		sendToAllScreens(createScorePacket0(maxPlayer));
	}

	public void sendToAll(PacketServer packetSupplier) {
		sendToAllMaster(packetSupplier);
		sendToAllPlayer(packetSupplier);
		sendToAllScreens(packetSupplier);
	}

	public void sendToAllMaster(PacketServer packet) {
		Map<Integer, Master> masters = getWebServer().getConnectionManager().getMasters();

		synchronized (masters) {
			masters.values().stream().forEach(p -> p.sendPacket(packet));
		}
	}

	public void sendToAllPlayer(PacketServer packet) {
		Map<UUID, Player> players = getWebServer().getConnectionManager().getPlayerMap();

		synchronized (players) {
			players.values().stream().filter(Player::isConnected).forEach(p -> p.sendPacket(packet));
		}
	}

	public void sendToAllScreens(PacketServer packet) {
		synchronized (SCREEN_PACKET_LOCKER) {
			packet.setNext(screenPacket);
			screenPacket = packet;
		}
	}

	public void sendAllScreenPackets() {
		synchronized (SCREEN_PACKET_LOCKER) {
			if (screenPacket != null)
				synchronized (screens) {
					screens.forEach((id, screen) -> screen.sendPacket(screenPacket));
				}
			screenPacket = null;
		}
	}

	public void setPhase(GamePhase phase) {
		if (this.phase == phase)
			return;
		this.phase = phase;
		if (this.phase == GamePhase.SCORE)
			gameEndTime = System.currentTimeMillis();
		phase.onInit();
		if (this.phase == phase) {
			if (this.phase == GamePhase.PLAYING)
				gameStartTime = System.currentTimeMillis();
			sendToAll(new PacketS0BSetGamePhase(phase));
		}
	}

	public void setScore(Player[] infectedScore, Player[] survivorScore) {
		this.infectedScore = infectedScore;
		this.survivorScore = survivorScore;
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
		phase.tick();
	}

	public void unregisterScreen(Screen screen) {
		synchronized (screens) {
			screens.remove(screen.getInternalId());
		}
	}
}
