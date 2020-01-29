package ssixprojet.common;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.socket.SocketChannel;
import ssixprojet.server.packet.PacketServer;

public class Player {
	private static AtomicInteger lastId = new AtomicInteger(1);
	private final int id;
	private boolean connected = false;
	private SocketChannel channel;

	public Player(SocketChannel channel) {
		this.id = lastId.getAndIncrement();
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

	public boolean isConnected() {
		return connected;
	}

	public void sendPacket(PacketServer packet) {
		ByteBuf buffer = Unpooled.buffer(packet.getInitialSize() + 4);
		packet.write(buffer);
		channel.write(buffer);
		channel.flush();
	}
}
