package ssixprojet.common.entity;

import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import io.netty.handler.codec.http.websocketx.CloseWebSocketFrame;
import ssixprojet.common.config.PlayerScore;
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
import ssixprojet.utils.Vector;

public class Player extends Entity implements ConnectionClient {
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
	private int health = 100, ammos;
	private long lastTimeShooted = 0L;
	public final PlayerScore score = new PlayerScore();

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
		setHealth(100);
		setAmmos(AtlasGame.getConfig().getStartAmmo());
		setType(PlayerType.SURVIVOR);
	}

	public PacketS03PlayerSpawn createPacketSpawn() {
		return new PacketS03PlayerSpawn(id, getX(), getY(), lookX, lookY, type.getId());
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
		return connected;
	}

	@Override
	public void kick(String msg) {
		Channel channel = connection.getChannel();
		System.out.println("Kick " + username + ": " + msg);
		CloseWebSocketFrame frame = new CloseWebSocketFrame(1000, msg);
		channel.writeAndFlush(frame);
		channel.close();
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

		Vector tir = new Vector(this.lookX, this.lookY).normalized();

		double x, y; // tireur
		double xt, yt, xi = 0, yi = 0; // impact
		double d = 2.0, dt; // distance
		double k;
		final double ox = getX() + getWidth() / 2;
		final double oy = getY() + getHeight() / 2;
		Entity cible = null;

		for (Entity e : this.getWorld().getEntities()) {
			if (e == this || (e instanceof Player && ((Player) e).type == type))
				continue;

			// opti : 2 bord a calcule
			if (lookX < 0) {
				x = e.getX() + e.getWidth();
			} else {
				x = e.getX();
			}
			if (lookY < 0) {
				y = e.getY() + e.getHeight();
			} else {
				y = e.getY();
			}

			if (tir.getY() != 0) {
				k = ((y - oy) / tir.getY());
				if (k > 0) {
					xt = k * tir.getX() + ox;
					if (xt >= e.getX() && xt <= e.getX() + e.getWidth()) {
						dt = (xt - ox) * (xt - ox) + (y - oy) * (y - oy);

						if (dt < d) {
							d = dt;
							cible = e;
							xi = xt;
							yi = y;
							continue;
						}
					}
				}
			}

			if (tir.getX() != 0) {
				k = ((x - ox) / tir.getX());
				if (k > 0) {
					yt = k * tir.getY() + oy;
					if (yt >= e.getY() && yt <= e.getY() + e.getHeight()) {
						dt = (x - ox) * (x - ox) + (yt - oy) * (yt - oy);

						if (dt < d) {
							d = dt;
							cible = e;
							xi = x;
							yi = yt;
							continue;
						}
					}
				}
			}

		}

		if (cible != null) {
			final double xf = xi;
			final double yf = yi;
			cible.shot(this);
			AtlasGame.getAtlas().sendToAllScreens(() -> new PacketS08Shot(ox, oy, xf, yf));
		}

	}

	@Override
	public void shot(Player p) {
		score.damageTaken += AMMO_POWER;
		p.score.damageGiven += AMMO_POWER;
		if (getHealth() - AMMO_POWER <= 0) {
			score.death++;
			p.score.kills++;
			infect();
		} else
			setHealth(getHealth() - AMMO_POWER);
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
			World w = getWorld();

			if (w != null) {
				score.infections += w.getEntities().stream().filter(e -> e instanceof Player).map(e -> (Player) e)
						.filter(p -> p.type == PlayerType.SURVIVOR && p.collide(this)).mapToInt(p -> {
							p.infect();
							return 1;
						}).sum();
			}
		}

		AtlasGame.getAtlas().sendToAllScreens(() -> new PacketS04PlayerMove(id, getX(), getY(), lookX, lookY));
	}
}
