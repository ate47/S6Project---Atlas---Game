package ssixprojet.common;

import ssixprojet.server.AtlasGame;

public enum GamePhase {
	WAITING(0) {
		@Override
		public void onInit() {}

		@Override
		public void tick() {}
	},
	PLAYING(1) {
		long infection;
		boolean infected = false;

		@Override
		public void onInit() {
			infection = System.currentTimeMillis() + AtlasGame.getConfig().getTimeInTickBeforeInfection() * 1000 / 20;
		}

		@Override
		public void tick() {
			if (!infected && infection > System.currentTimeMillis()) {
				infected = true;
				AtlasGame.getAtlas().randomInfection(AtlasGame.getConfig().getInitialInfectionPercentage());
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

	GamePhase(int id) {
		this.id = id;
	}

	public int getId() {
		return id;
	}

	public void onInit() {}

	public void tick() {}
}
