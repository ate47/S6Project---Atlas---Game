package ssixprojet.server.connection;

import java.util.UUID;

import io.netty.channel.Channel;
import ssixprojet.server.packet.PacketServer;

public interface Connection {
	void connectPlayer(String name);

	void connectScreen();

	void connectMaster(String password);

	ConnectionClient getAttachedClient();

	Channel getChannel();

	void onClose();

	void reconnectPlayer(UUID id, String name);

	void setAttachedClient(ConnectionClient client);

	void onError(String error);

	/**
	 * send a packet to this source
	 * 
	 * @param packet
	 *            the packet to send
	 */
	void sendPacket(PacketServer packet);
}
