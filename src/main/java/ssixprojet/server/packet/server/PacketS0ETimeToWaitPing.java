package ssixprojet.server.packet.server;

import io.netty.buffer.ByteBuf;
import ssixprojet.server.packet.PacketServer;

public class PacketS0ETimeToWaitPing extends PacketServer {
	private int time;

	public PacketS0ETimeToWaitPing(int time) {
		super(0x0e, 4);
		this.time = time;
	}

	@Override
	public void write(ByteBuf buf) {
		buf.writeInt(time);
	}

}
