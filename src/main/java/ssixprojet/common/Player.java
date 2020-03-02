package ssixprojet.common;

import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import lombok.Getter;
import lombok.Setter;
import ssixprojet.server.ServerManager;
import ssixprojet.server.packet.PacketServer;
import ssixprojet.server.packet.server.PacketS02Death;

public class Player implements PacketSource {
	public static final int MAX_KEEP_ALIVE = 20;
	private static AtomicInteger lastId = new AtomicInteger(1);
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
	private double x, y;
	@Getter
	private int health = 100, ammos = ServerManager.getConfig().getStartAmmo();

	public Player(Channel channel) {
		this.id = lastId.getAndIncrement();
		this.internalId = UUID.randomUUID();
		this.channel = Objects.requireNonNull(channel);
	}

	public void connect(String name) {
		connected = true;
		this.username = name;
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
		sendPacket(new PacketS02Death(msg));
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
		channel.write(buffer);
		channel.flush();
	}
}
