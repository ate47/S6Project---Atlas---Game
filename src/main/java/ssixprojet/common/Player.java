package ssixprojet.common;

import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import io.netty.handler.codec.http.websocketx.CloseWebSocketFrame;
import lombok.Getter;
import lombok.Setter;
import ssixprojet.server.ServerManager;
import ssixprojet.server.packet.PacketServer;
import ssixprojet.server.packet.client.PacketC04Move;

public class Player extends Entity implements PacketSource {
	public static final int MAX_KEEP_ALIVE = 20;
	private static AtomicInteger lastId = new AtomicInteger(1);
	@Getter
	@Setter
	private World world;
	private final int id;
	private final UUID internalId;
	@Getter
	private String username;
	@Getter
	@Setter
	private PlayerType type = PlayerType.SURVIVOR;
	private boolean connected = false;
	private Channel channel;
	private int keepAliveCount = MAX_KEEP_ALIVE;
	@Getter
	private double x, y, lookX, lookY;
	@Getter
	private int health = 100, ammos = ServerManager.getConfig().getStartAmmo();

	public Player(Channel channel) {
		super(10 , 10);
		this.id = lastId.getAndIncrement();
		this.internalId = UUID.randomUUID();
		this.channel = Objects.requireNonNull(channel);
	}

	public void connect(String name) {
		connected = true;
		this.username = name;
		System.out.println(name + " connected!");
		kick("sorry, not now");
	}

	public synchronized void decrementKeepAliveCount() {
		keepAliveCount--;
	}

	public Channel getChannel() {
		return channel;
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
		channel.writeAndFlush(frame);
	}

	public void updateMove(PacketC04Move movePacket) {
		lookX = movePacket.getLookX();
		lookY = movePacket.getLookY();
		move(movePacket.getDeltaX(), movePacket.getDeltaY());
	}
}
