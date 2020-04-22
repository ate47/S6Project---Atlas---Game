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
		// TODO Auto-generated method stub
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
