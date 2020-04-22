package ssixprojet.server.packet.server;

import io.netty.buffer.ByteBuf;
import ssixprojet.server.packet.PacketServer;

public class PacketS09ChangeHealth extends PacketServer {
	private int health;

	public PacketS09ChangeHealth(int health) {
		super(0x09, 4);
		this.health = health;
	}

	@Override
	public void write(ByteBuf buf) {
		buf.writeInt(health);
	}

}
