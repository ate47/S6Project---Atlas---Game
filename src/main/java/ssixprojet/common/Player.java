package ssixprojet.common;

import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.socket.SocketChannel;
import lombok.Getter;
import lombok.Setter;
import ssixprojet.server.packet.PacketServer;

public class Player {
	public static final int MAX_KEEP_ALIVE = 20;
	private static AtomicInteger lastId = new AtomicInteger(1);
	private final int id;
	private final UUID internalId;
	@Getter
	@Setter
	private PlayerType type = PlayerType.SURVIVOR;
	private boolean connected = false;
	private SocketChannel channel;
	private int keepAliveCount = MAX_KEEP_ALIVE;
	@Getter
	private double x, y;
	@Getter
	private int health = 100, ammos = 90; // TODO Set config

	public Player(SocketChannel channel) {
		this.id = lastId.getAndIncrement();
		this.internalId = UUID.randomUUID();
		this.channel = Objects.requireNonNull(channel);
	}

	public void connect() {
		connected = true;
	}

	public SocketChannel getChannel() {
		return channel;
	}

	public int getId() {
		return id;
	}

	public UUID getInternalId() {
		return internalId;
	}
	
	public boolean isConnected() {
		return connected;
	}

	public synchronized int getKeepAliveCount() {
		return keepAliveCount;
	}

	public synchronized void resetKeepAliveCount() {
		keepAliveCount = MAX_KEEP_ALIVE;
	}
	public synchronized void decrementKeepAliveCount() {
		keepAliveCount--;
	}

	public void sendPacket(PacketServer packet) {
		ByteBuf buffer = Unpooled.buffer(packet.getInitialSize() + 4);
		packet.write(buffer);
		channel.write(buffer);
		channel.flush();
	}
}
