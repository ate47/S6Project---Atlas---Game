package ssixprojet.server.packet.server;

import io.netty.buffer.ByteBuf;
import ssixprojet.server.packet.PacketServer;

public class PacketS03PlayerSpawn extends PacketServer {
	private double x, y, lookX, lookY;
	private int id, type;
	private byte[] preparedName;

	public PacketS03PlayerSpawn(int id, double x, double y, double lookX, double lookY, int type, String name) {
		super(0x03, 8 * 4 + 4);

		this.id = id;
		this.x = x;
		this.y = y;
		this.lookX = lookX;
		this.lookY = lookY;
		this.type = type;
		preparedName = prepareUTF8String(name);
	}

	@Override
	public void write(ByteBuf buf) {
		buf.writeInt(id);
		buf.writeDouble(x);
		buf.writeDouble(y);
		buf.writeDouble(lookX);
		buf.writeDouble(lookY);
		buf.writeInt(type);
		writeUTF8String(buf, preparedName);
	}
}
