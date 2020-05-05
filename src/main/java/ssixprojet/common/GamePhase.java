package ssixprojet.common;

import ssixprojet.common.entity.Player;
import ssixprojet.server.AtlasGame;
import ssixprojet.server.packet.server.PacketS0ETimeToWaitPing;

public enum GamePhase {
	WAITING(0) {
		@Override
		public void onInit() {}

		@Override
		public void tick() {}
	},
	PLAYING(1) {
		long infection;
		long ping;
		boolean infected;

		@Override
		public void onInit() {
			infected = false;
			infection = System.currentTimeMillis() + AtlasGame.getConfig().getTimeInTickBeforeInfection() * 1000 / 20;
			// wait if no one play
			if (!atlas.getWebServer().getConnectionManager().getPlayerMap().values().stream()
					.filter(Player::isConnected).findFirst().isPresent())
				atlas.setPhase(WAITING);
		}

		@Override
		public void tick() {
			if (!infected) {
				long t = System.currentTimeMillis();
				if (infection < t) {
					infected = true;
					atlas.randomInfection(AtlasGame.getConfig().getInitialInfectionPercentage());
					atlas.sendToAll(() -> new PacketS0ETimeToWaitPing(0));
				} else if (ping < t) {
					int ttw = (int) ((infection - ping) / 1000);
					atlas.sendToAll(() -> new PacketS0ETimeToWaitPing(ttw));
					ping = t + 800L; // to avoid to wait more than 1s
				}
			}
		}
	},
	SCORE(2) {
		@Override
		public void onInit() {}

		@Override
		public void tick() {}
	};

	private int id;
	protected AtlasGame atlas;

	private GamePhase(int id) {
		this.id = id;
		this.atlas = AtlasGame.getAtlas();
	}

	public int getId() {
		return id;
	}

	public void onInit() {}

	public void tick() {}
}
