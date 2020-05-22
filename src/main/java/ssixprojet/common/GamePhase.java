package ssixprojet.common;

import java.util.Collection;

import ssixprojet.common.entity.Player;
import ssixprojet.server.AtlasGame;
import ssixprojet.server.packet.server.PacketS0ETimeToWaitPing;
import ssixprojet.server.packet.server.PacketS0FScorePlayer;

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
		long fin;

		@Override
		public boolean isPvpEnabled() {
			return infected;
		}

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
					fin = System.currentTimeMillis() + AtlasGame.getConfig().getTimeInTickBeforeEnd() * 1000 / 20;
				} else if (ping < t) {
					int ttw = (int) ((infection - ping) / 1000);
					atlas.sendToAll(() -> new PacketS0ETimeToWaitPing(ttw));
					ping = t + 800L; // to avoid to wait more than 1s
				}
			}
			
			if(infected) {
				long t = System.currentTimeMillis();
				if(fin < t)
					atlas.setPhase(SCORE);
				else if(ping < t){
					int ttw = (int) ((fin - ping) / 1000);
					atlas.sendToAllScreens(() -> new PacketS0ETimeToWaitPing(-ttw));
					ping = t + 800L;
				}
			}
		}
	},
	SCORE(2) {
		@Override
		public void onInit() {
			Collection<Player> plr = atlas.getWebServer().getConnectionManager().getPlayerInternalMap().values();

			// set end for every player
			plr.stream().forEach(p -> p.setEnd());

			// sort the player into their score

			Player[] survivorScore = plr.stream().filter(Player::isConnected).sorted(Player.SCORE_PLAYER_COMPARATOR)
					.toArray(Player[]::new);
			Player[] infectedScore = plr.stream().filter(Player::isConnected).sorted(Player.SCORE_INFECTED_COMPARATOR)
					.toArray(Player[]::new);

			// set sort id for each players

			for (int i = 0; i < infectedScore.length; i++)
				infectedScore[i].score.infectionSortId = i;

			for (int i = 0; i < survivorScore.length; i++)
				survivorScore[i].score.survivorSortId = i;

			// send the score for each players
			plr.stream().filter(Player::isConnected).forEach(p -> p.sendPacket(new PacketS0FScorePlayer(p.score)));

			atlas.setScore(infectedScore, survivorScore);
			atlas.sendScoreScreenPacket();
		}

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

	public boolean isPvpEnabled() {
		return false;
	}

	public void onInit() {}
	
	public void tick() {}
}
