package ssixprojet.common.entity;

import java.util.Comparator;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import io.netty.handler.codec.http.websocketx.CloseWebSocketFrame;
import ssixprojet.common.config.PlayerScore;
import ssixprojet.common.world.Chunk;
import ssixprojet.common.world.TraceAnswer;
import ssixprojet.common.world.World;
import ssixprojet.server.AtlasGame;
import ssixprojet.server.connection.Connection;
import ssixprojet.server.connection.ConnectionClient;
import ssixprojet.server.packet.PacketServer;
import ssixprojet.server.packet.client.PacketC04Move;
import ssixprojet.server.packet.server.PacketS03PlayerSpawn;
import ssixprojet.server.packet.server.PacketS04PlayerMove;
import ssixprojet.server.packet.server.PacketS06PlayerType;
import ssixprojet.server.packet.server.PacketS08Shot;
import ssixprojet.server.packet.server.PacketS09ChangeHealth;
import ssixprojet.server.packet.server.PacketS0AChangeAmmos;

public class Player extends Entity implements ConnectionClient {
	public static final Comparator<Player> SCORE_INFECTED_COMPARATOR = new Comparator<Player>() {
		@Override
		public int compare(Player p1, Player p2) {
			return p1.score.compareToInfected(p2.score);
		}
	};
	public static final Comparator<Player> SCORE_PLAYER_COMPARATOR = new Comparator<Player>() {
		@Override
		public int compare(Player p1, Player p2) {
			return p1.score.compareToSurvivor(p2.score);
		}
	};
	public static final int START_AMMOS = AtlasGame.getConfig().getStartAmmo();
	public static final long TIME_BEFORE_RESHOOT = AtlasGame.getConfig().getMillisBeforeReshooting();
	public static final int AMMO_POWER = AtlasGame.getConfig().getAmmoPower();
	public static final int MAX_KEEP_ALIVE = 20;
	private static AtomicInteger lastId = new AtomicInteger(1);
	private final int id;
	private final UUID internalId;
	private String username;
	private PlayerType type = PlayerType.SURVIVOR;
	private boolean connected = false;
	private Connection connection;
	private int keepAliveCount = MAX_KEEP_ALIVE;
	private double lookX = 1, lookY;
	private int health = 100, ammos = START_AMMOS;
	private long lastTimeShooted = 0L;
	public final PlayerScore score = new PlayerScore();
	private final TraceAnswer trace = new TraceAnswer(); // put that here to avoid reallocation

	public Player(Connection connection) {
		this(connection, AtlasGame.getAtlas());
	}

	private Player(Connection connection, AtlasGame game) {
		super(game.getPlayerSizeX(), game.getPlayerSizeY());
		this.id = lastId.getAndIncrement();
		this.internalId = UUID.randomUUID();
		this.connection = Objects.requireNonNull(connection);
	}

	/**
	 * mark this player as connected
	 * 
	 * @param name
	 *            the username to take in game
	 */
	public void connect(String name) {
		connected = true;
		this.username = name;
		setHealth(health);
		setAmmos(ammos);
		setType(type);
	}

	public PacketS03PlayerSpawn createPacketSpawn() {
		return new PacketS03PlayerSpawn(id, getX(), getY(), lookX, lookY, type.getId(), username);
	}

	public synchronized void decrementKeepAliveCount() {
		keepAliveCount--;
	}

	public void disconnect() {
		setConnection(null);
	}

	public int getAmmos() {
		return ammos;
	}

	@Override
	public Connection getConnection() {
		return connection;
	}

	public int getHealth() {
		return health;
	}

	public int getId() {
		return id;
	}

	public UUID getInternalId() {
		return internalId;
	}

	public synchronized int getKeepAliveCount() {
		return keepAliveCount;
	}

	public double getLookX() {
		return lookX;
	}

	public double getLookY() {
		return lookY;
	}

	@Override
	public double getSpeed() {
		return type == PlayerType.INFECTED
				? AtlasGame.getConfig().getSpeedAccelerationPercentage() * super.getSpeed() / 100
				: super.getSpeed();
	}

	public PlayerType getType() {
		return type;
	}

	public String getUsername() {
		return username;
	}

	public void infect() {
		setHealth(100);
		setType(PlayerType.INFECTED);
		// respawn the player
		getWorld().spawnEntityAtRandomLocation(this);
	}

	public boolean isConnected() {
		return connected && connection != null;
	}

	public String getIp() {
		if (getConnection() == null)
			return "err";
		if (getConnection().getChannel() == null)
			return "err";
		return String.valueOf(getConnection().getChannel().remoteAddress());
	}

	@Override
	public void kick(String msg) {
		Channel channel = connection.getChannel();
		System.out.println("Kick " + username + ": " + msg);
		CloseWebSocketFrame frame = new CloseWebSocketFrame(1000, msg);
		channel.writeAndFlush(frame);
		channel.close();
		connection = null;
		connected = false;
	}

	@Override
	public void onDisconnect(String reason) {
		connected = false;
		System.out.println("[Player] " + username + " disconnected : " + reason);
	}

	public synchronized void resetKeepAliveCount() {
		keepAliveCount = MAX_KEEP_ALIVE;
	}

	@Override
	public void respawn(double x, double y) {
		super.respawn(x, y);
		AtlasGame.getAtlas().sendToAllScreens(this::createPacketSpawn);
	}

	@Override
	public void sendPacket(PacketServer packet) {
		if (connection == null || connection.getChannel() == null)
			return;
		ByteBuf buffer = Unpooled.buffer(packet.getInitialSize() + 4);
		buffer.writeInt(packet.getPacketId());
		packet.write(buffer);
		BinaryWebSocketFrame frame = new BinaryWebSocketFrame(buffer);
		connection.getChannel().writeAndFlush(frame);
	}

	public void setAmmos(int ammos) {
		sendPacket(new PacketS0AChangeAmmos(this.ammos = Math.max(0, ammos)));
	}

	public void setConnection(Connection connection) {
		this.connection = connection;
		this.connected = this.connection != null;
	}

	public void setHealth(int health) {
		sendPacket(new PacketS09ChangeHealth(this.health = Math.max(0, health)));
	}

	public void setType(PlayerType type) {
		if (this.type != type && type == PlayerType.INFECTED)
			score.timeAlive = (int) (System.currentTimeMillis() - AtlasGame.getAtlas().getGameStartTime());
		this.type = type;
		// update phone type
		sendPacket(new PacketS06PlayerType(type.getId(), 0));
		// update type on every screens
		AtlasGame.getAtlas().sendToAllScreens(() -> new PacketS06PlayerType(type.getId(), id));
	}

	public void shooting() {
		if (type == PlayerType.INFECTED)
			return;

		long currentTime = System.currentTimeMillis();
		if (this.getWorld() == null || lastTimeShooted + TIME_BEFORE_RESHOOT > currentTime || ammos <= 0)
			return;

		lastTimeShooted = currentTime;
		setAmmos(getAmmos() - 1);

		final double ox = getX() + getWidth() / 2;
		final double oy = getY() + getHeight() / 2;

		getWorld()
				.traceLineAndGetEntity(ox, oy, lookX, lookY,
						e -> !(e == this
								|| (e instanceof Player && (((Player) e).type == type || !((Player) e).isConnected()))),
						trace);

		if (trace.isFound()) {
			final double xf = trace.getX();
			final double yf = trace.getY();
			if (trace.getTarget().shot(this)) {
				setAmmos(Math.min(getAmmos() + (100 / AMMO_POWER) + 3, START_AMMOS));
			}
			AtlasGame.getAtlas().sendToAllScreens(() -> new PacketS08Shot(ox, oy, xf, yf));
		}

	}

	@Override
	public boolean shot(Player p) {
		score.damageTaken += AMMO_POWER;
		p.score.damageGiven += AMMO_POWER;
		if (getHealth() - AMMO_POWER <= 0) {
			score.death++;
			p.score.kills++;
			infect();
			return true;
		} else
			setHealth(getHealth() - AMMO_POWER);
		return false;
	}

	@Override
	public void spawn(World w, double x, double y) {
		super.spawn(w, x, y);
		AtlasGame.getAtlas().sendToAllScreens(this::createPacketSpawn);
	}

	/**
	 * parse a {@link PacketC04Move} packet on this player
	 * 
	 * @param movePacket
	 *            the move packet
	 */
	public void updateMove(PacketC04Move movePacket) {
		double preLookX = movePacket.getLookX();
		double preLookY = movePacket.getLookY();
		double preDeltaX = movePacket.getDeltaX();
		double preDeltaY = movePacket.getDeltaY();

		if (preLookX * preLookX + preLookY * preLookY > 1.05D) {
			kick("Bad look vector");
			return;
		}
		if (preDeltaX * preDeltaX + preDeltaY * preDeltaY > 1.05D) {
			kick("Bad move vector");
			return;
		}

		if (preLookX * preLookX + preLookY * preLookY > 0.01D) {
			lookX = preLookX;
			lookY = preLookY;
		}
		move(preDeltaX, preDeltaY);

		if (type == PlayerType.INFECTED) {
			Chunk[][] area = getArea();

			int touches = 0;

			for (int i = 0; i < area.length; i++)
				for (int j = 0; j < area[i].length; j++)
					touches += area[i][j].getEntities().values().stream().filter(e -> e instanceof Player)
							.map(e -> (Player) e)
							.filter(p -> p.isConnected() && p.type == PlayerType.SURVIVOR && p.collide(this))
							.mapToInt(p -> {
								p.infect();
								return 1;
							}).sum();

			if (touches != 0) {
				if (getHealth() < 75)
					setHealth(75);
				score.infections += touches;
			}
		}

		AtlasGame.getAtlas().sendToAllScreens(() -> new PacketS04PlayerMove(id, getX(), getY(), lookX, lookY));
	}

	public void setEnd() {
		if (this.type == PlayerType.SURVIVOR)
			score.timeAlive = (int) (AtlasGame.getAtlas().getGameEndTime() - AtlasGame.getAtlas().getGameStartTime());
	}
}
