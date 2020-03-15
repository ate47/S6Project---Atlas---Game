package ssixprojet.server.connection;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import io.netty.handler.codec.http.websocketx.CloseWebSocketFrame;
import ssixprojet.common.Screen;
import ssixprojet.common.entity.Player;
import ssixprojet.server.packet.PacketServer;
import ssixprojet.server.packet.server.PacketS02PlayerRegister;

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
				playerMap.put(plr.getInternalId(), plr);
				client = plr;
				close = PLAYER;
				// send the uuid for reconnection
				plr.sendPacket(new PacketS02PlayerRegister(plr.getInternalId()));
			}
		}

		@Override
		public void connectScreen() {
			if (checkConnected()) {
				Screen screen = new Screen(this);
				client = screen;
				close = SCREEN;
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

	}

	private Map<UUID, Player> playerMap = new HashMap<>();
	private List<Screen> screens = new ArrayList<>();

	private final ConnectionCloseOperation NONE = c -> {};
	private final ConnectionCloseOperation PLAYER = c -> ((Player) c).disconnect();
	private final ConnectionCloseOperation SCREEN = c -> {
		((Screen) c).disconnect();
		screens.remove(((Screen) c));
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

	/**
	 * @return a unmodifiable collection of the players
	 */
	public Collection<Player> getPlayers() {
		return Collections.unmodifiableCollection(playerMap.values());
	}

	/**
	 * @return a unmodifiable collection of the screens
	 */
	public Collection<Screen> getScreens() {
		return Collections.unmodifiableCollection(screens);
	}
}
