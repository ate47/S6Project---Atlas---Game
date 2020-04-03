package ssixprojet.server.connection;

import java.util.UUID;

import io.netty.channel.Channel;

public interface Connection {
	void connectPlayer(String name);
	void connectScreen();
	ConnectionClient getAttachedClient();
	Channel getChannel();
	void onClose();
	void reconnectPlayer(UUID id);
	void setAttachedClient(ConnectionClient client);
}