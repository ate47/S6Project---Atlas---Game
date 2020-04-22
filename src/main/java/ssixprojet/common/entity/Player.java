package ssixprojet.common.entity;

import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import io.netty.handler.codec.http.websocketx.CloseWebSocketFrame;
import ssixprojet.common.world.World;
import ssixprojet.server.AtlasGame;
import ssixprojet.server.connection.Connection;
import ssixprojet.server.connection.ConnectionClient;
import ssixprojet.server.packet.PacketServer;
import ssixprojet.server.packet.client.PacketC04Move;
import ssixprojet.server.packet.server.PacketS03PlayerSpawn;
import ssixprojet.server.packet.server.PacketS04PlayerMove;
import ssixprojet.server.packet.server.PacketS08Shot;
import ssixprojet.utils.Vector;

public class Player extends Entity implements ConnectionClient {
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
	private int health = 100, ammos = AtlasGame.getConfig().getStartAmmo();

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
	}

	public synchronized void decrementKeepAliveCount() {
		keepAliveCount--;
	}

	public void disconnect() {
		setConnection(null);
	}

	@Override
	public Connection getConnection() {
		return connection;
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

	public synchronized void resetKeepAliveCount() {
		keepAliveCount = MAX_KEEP_ALIVE;
	}

	@Override
	public void sendPacket(PacketServer packet) {
		ByteBuf buffer = Unpooled.buffer(packet.getInitialSize() + 4);
		buffer.writeInt(packet.getPacketId());
		packet.write(buffer);
		BinaryWebSocketFrame frame = new BinaryWebSocketFrame(buffer);
		connection.getChannel().writeAndFlush(frame);
	}

	public void setConnection(Connection connection) {
		this.connection = connection;
		this.connected = this.connection != null;
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

		AtlasGame.getAtlas().sendToAllScreens(() -> new PacketS04PlayerMove(id, getX(), getY(), lookX, lookY));
	}

	public void shooting() {
		if (this.getWorld() == null)
			return;

		Vector tir = new Vector(this.lookX, this.lookY).normalized();

		double x, y, // tireur
				xt, yt, xi = 0, yi = 0, // impacte
				k = 0, d = 2.0, dt; // distance
		Entity cible = null;

		for (Entity e : this.getWorld().getEntities()) {
			if (e == this || (e instanceof Player && ((Player) e).type == type))
				continue;

			// opti : 2 bord a calcule
			if (lookX > 0) {
				x = e.getX() + e.getWidth();
			} else {
				x = e.getX();
			}
			if (lookY > 0) {
				y = e.getY() + e.getHeight();
			} else {
				y = e.getY();
			}

			if (tir.getY() != 0) {
				k = ((y - this.getY()) / tir.getY());
				xt = k * tir.getX() + this.getX();
				if (xt >= e.getX() && xt <= e.getX() + e.getWidth()) {
					dt = (y - this.getY()) * (y - this.getY()) + (xt - this.getX()) * (xt - this.getX());

					if (dt < d) {
						d = dt;
						cible = e;
						xi = xt;
						yi = y;
						continue;
					}
				}

			}

			if (tir.getX() != 0) {
				k = ((x - this.getX()) / tir.getX());
				yt = k * tir.getY() + this.getY();
				if (yt >= e.getY() && yt <= e.getY() + e.getHeight()) {
					dt = (y - this.getY()) * (y - this.getY()) + (yt - this.getY()) * (yt - this.getY());

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

		if (cible != null) {
			double xf, yf; // java est chelou
			xf = xi;
			yf = yi;
			cible.shot(this);
			AtlasGame.getAtlas().sendToAllScreens(() -> new PacketS08Shot(this.getX(), this.getY(), xf, yf));
		}

	}

	public PacketS03PlayerSpawn createPacketSpawn() {
		return new PacketS03PlayerSpawn(id, getX(), getY(), lookX, lookY, type.getId());
	}

	@Override
	public void spawn(World w, double x, double y) {
		super.spawn(w, x, y);
		AtlasGame.getAtlas().sendToAllScreens(this::createPacketSpawn);
	}

	@Override
	public void respawn(double x, double y) {
		super.respawn(x, y);
		AtlasGame.getAtlas().sendToAllScreens(this::createPacketSpawn);
	}

	public String getUsername() {
		return username;
	}

	public PlayerType getType() {
		return type;
	}

	public double getLookX() {
		return lookX;
	}

	public double getLookY() {
		return lookY;
	}

	public int getHealth() {
		return health;
	}

	public int getAmmos() {
		return ammos;
	}

	@Override
	public double getSpeed() {
		return type == PlayerType.INFECTED
				? AtlasGame.getConfig().getSpeedAccelerationPercentage() * super.getSpeed() / 100
				: super.getSpeed();
	}
}
