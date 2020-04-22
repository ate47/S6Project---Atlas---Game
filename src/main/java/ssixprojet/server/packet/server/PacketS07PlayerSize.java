package ssixprojet.server.packet.server;

import io.netty.buffer.ByteBuf;
import ssixprojet.server.packet.PacketServer;

public class PacketS07PlayerSize extends PacketServer {
	private double sizeX, sizeY;

	public PacketS07PlayerSize(double sizeX, double sizeY) {
		super(0x07, 16);
		this.sizeX = sizeX;
		this.sizeY = sizeY;
	}

	@Override
	public void write(ByteBuf buf) {
		buf.writeDouble(sizeX);
		buf.writeDouble(sizeY);
	}
}
