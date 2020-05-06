package ssixprojet.server.connection;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import io.netty.handler.codec.http.websocketx.CloseWebSocketFrame;
import ssixprojet.common.GamePhase;
import ssixprojet.common.Master;
import ssixprojet.common.Screen;
import ssixprojet.common.entity.Player;
import ssixprojet.server.AtlasGame;
import ssixprojet.server.packet.PacketServer;
import ssixprojet.server.packet.server.PacketS02PlayerRegister;
import ssixprojet.server.packet.server.PacketS05PlayerDead;
import ssixprojet.server.packet.server.PacketS0BSetGamePhase;
import ssixprojet.server.packet.server.PacketS0CBadPassword;
import ssixprojet.server.packet.server.PacketS0DMasterLogged;

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

		private void sendConnectionsPacket() {
			client.sendPacket(new PacketS0BSetGamePhase(AtlasGame.getAtlas().getPhase()));
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
				if (AtlasGame.getAtlas().getPhase() != GamePhase.WAITING) {
					kick("bad phase");
					return;
				}
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
				sendConnectionsPacket();
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
				sendConnectionsPacket();
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
		public void reconnectPlayer(UUID uuid, String name) {
			if (checkConnected()) {
				Player plr;
				synchronized (playerMap) {
					plr = playerMap.get(uuid);
				}
				if (plr == null || !plr.getUsername().equals(name)) {
					connectPlayer(name);
				} else {
					System.out.println("[Player] " + plr.getUsername() + " reconnected!");
					plr.setConnection(this);
					plr.connect(name);
					AtlasGame.getAtlas().sendToAllScreens(plr::createPacketSpawn);
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

		@Override
		public void connectMaster(String password) {
			if (!password.equals(AtlasGame.getConfig().getPasswordMaster())) {
				sendPacket(new PacketS0CBadPassword());
				return;
			}

			Master master = new Master(this);
			synchronized (masters) {
				masters.put(master.getId(), master);
			}

			client = master;
			close = MASTER;
			sendConnectionsPacket();
			System.out.println("[Master] new master connected!");
			master.sendPacket(new PacketS0DMasterLogged());
		}

	}

	private Map<UUID, Player> playerMap = new HashMap<>();
	private Map<Integer, Player> playerInternalMap = new HashMap<>();
	private Map<Integer, Master> masters = new HashMap<>();
	private final ConnectionCloseOperation NONE = c -> {};
	private final ConnectionCloseOperation MASTER = c -> {
		synchronized (masters) {
			masters.remove(((Master) c).getId());
		}
	};
	private final ConnectionCloseOperation PLAYER = c -> {
		Player p = (Player) c;
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

	public Map<Integer, Master> getMasters() {
		return masters;
	}

	public Map<UUID, Player> getPlayerMap() {
		return playerMap;
	}

	public Map<Integer, Player> getPlayerInternalMap() {
		return playerInternalMap;
	}

}
