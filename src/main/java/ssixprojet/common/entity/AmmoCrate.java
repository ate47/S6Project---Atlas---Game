package ssixprojet.common.entity;

import ssixprojet.common.world.World;
import ssixprojet.server.AtlasGame;
import ssixprojet.server.packet.server.PacketS11CrateSpawn;
import ssixprojet.server.packet.server.PacketS12CrateRemove;

public class AmmoCrate extends Entity {
	private int ammos;

	private AmmoCrate(AtlasGame game, int ammos) {
		super(game.getPlayerSizeX(), game.getPlayerSizeY());
		this.ammos = ammos;
	}

	public AmmoCrate(int ammos) {
		this(AtlasGame.getAtlas(), ammos);
	}

	public int getAmmos() {
		return ammos;
	}

	@Override
	public void kill() {
		kill(true);
	}

	public void kill(boolean deleteFromStorage) {
		kill(deleteFromStorage, true);
	}

	public void kill(boolean deleteFromStorage, boolean deleteFromEntities) {
		if (deleteFromEntities)
			super.kill();
		if (deleteFromStorage)
			AtlasGame.getAtlas().getCrates().remove(getEntityId());
		AtlasGame.getAtlas().sendToAllScreens(new PacketS12CrateRemove(this));
	}

	@Override
	public void spawn(World w, double x, double y) {
		super.spawn(w, x, y);
		AtlasGame.getAtlas().sendToAllScreens(new PacketS11CrateSpawn(this));
	}
}
