package ssixprojet.server.connection;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import io.netty.handler.codec.http.websocketx.CloseWebSocketFrame;
import ssixprojet.common.Screen;
import ssixprojet.common.entity.Player;
import ssixprojet.server.AtlasGame;
import ssixprojet.server.packet.PacketServer;
import ssixprojet.server.packet.server.PacketS02PlayerRegister;
import ssixprojet.server.packet.server.PacketS05PlayerDead;

public class ConnectionManager {
	@FunctionalInterface
	private interface ConnectionCloseOperation {
		void onClose(ConnectionClient client);
	}

	private class ConnectionImpl implements Connection, ConnectionClient {
		Channel channel;
		ConnectionClient client;
		ConnectionCloseOperation close = NONE;

		public ConnectionImpl(Channel channel) {
			this.channel = channel;
			this.client = this;
		}

		private boolean checkConnected() {
			if (client != this) {
				client.kick("Connection already registered");
				return false;
			}
			return true;
		}

		@Override
		public void connectPlayer(String name) {
			if (checkConnected()) {
				Player plr = new Player(this);
				plr.connect(name);
				AtlasGame.getAtlas().getMainWorld().spawnEntityAtRandomLocation(plr);

				synchronized (playerMap) {
					playerMap.put(plr.getInternalId(), plr);
				}
				synchronized (playerInternalMap) {
					playerInternalMap.put(plr.getId(), plr);
				}
				client = plr;
				close = PLAYER;
				// send the uuid for reconnection
				plr.sendPacket(new PacketS02PlayerRegister(plr.getInternalId()));
				System.out.println("[Player] " + name + " connected!");
			}
		}

		@Override
		public void connectScreen() {
			if (checkConnected()) {
				Screen screen = new Screen(this);

				AtlasGame.getAtlas().registerScreen(screen);

				client = screen;
				close = SCREEN;
				System.out.println("[Screen#" + screen.getInternalId() + "] new screen connected!");
			}
		}

		@Override
		public ConnectionClient getAttachedClient() {
			return client;
		}

		@Override
		public Channel getChannel() {
			return channel;
		}

		@Override
		public Connection getConnection() {
			return this;
		}

		@Override
		public void kick(String msg) {
			CloseWebSocketFrame frame = new CloseWebSocketFrame(1000, msg);
			channel.writeAndFlush(frame);
			channel.close();
		}

		@Override
		public void onClose() {
			close.onClose(client);
			client.onDisconnect("Disconnected");
		}

		@Override
		public void reconnectPlayer(UUID uuid) {
			if (checkConnected()) {
				Player plr = playerMap.get(uuid);
				if (plr == null) {
					kick("Bad player UUID");
				} else {
					plr.setConnection(this);
					client = plr;
					close = PLAYER;
				}
			}
		}

		@Override
		public void sendPacket(PacketServer packet) {
			ByteBuf buffer = Unpooled.buffer(packet.getInitialSize() + 4);
			buffer.writeInt(packet.getPacketId());
			packet.write(buffer);
			BinaryWebSocketFrame frame = new BinaryWebSocketFrame(buffer);
			channel.writeAndFlush(frame);
		}

		@Override
		public void setAttachedClient(ConnectionClient client) {
			this.client = client;
		}

		@Override
		public void onError(String error) {
			client.onDisconnect(error);
		}

		@Override
		public void onDisconnect(String reason) {
			// connection closed without error
		}

	}

	private Map<UUID, Player> playerMap = new HashMap<>();
	private Map<Integer, Player> playerInternalMap = new HashMap<>();

	private final ConnectionCloseOperation NONE = c -> {};
	private final ConnectionCloseOperation PLAYER = c -> {
		Player p = (Player) c;
		synchronized (playerMap) {
			playerMap.remove(p.getInternalId());
		}
		synchronized (playerInternalMap) {
			playerInternalMap.remove(p.getId());
		}
		AtlasGame.getAtlas().sendToAllScreens(() -> new PacketS05PlayerDead(p.getId()));
		p.disconnect();
	};
	private final ConnectionCloseOperation SCREEN = c -> {
		AtlasGame.getAtlas().unregisterScreen((Screen) c);
		((Screen) c).disconnect();
	};

	/**
	 * create a {@link Connection} for a channel
	 * 
	 * @param channel
	 *            the channel
	 * @return the connection
	 */
	public Connection createConnection(Channel channel) {
		return new ConnectionImpl(channel);
	}

	public Map<UUID, Player> getPlayerMap() {
		return playerMap;
	}

	public Map<Integer, Player> getPlayerInternalMap() {
		return playerInternalMap;
	}

}
