package ssixprojet.server.packet.server;

import io.netty.buffer.ByteBuf;
import ssixprojet.server.packet.PacketServer;

public class PacketS08Shot extends PacketServer {
	private double x1, y1, x2, y2;

	public PacketS08Shot(double x1, double y1, double x2, double y2) {
		super(0x08, 4 * 8);
		this.x1 = x1;
		this.y1 = y1;
		this.x2 = x2;
		this.y2 = y2;
	}

	@Override
	public void write(ByteBuf buf) {
		buf.writeDouble(x1);
		buf.writeDouble(y1);
		buf.writeDouble(x2);
		buf.writeDouble(y2);
	}

}
