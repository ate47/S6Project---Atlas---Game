package ssixprojet.common.world;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;
import ssixprojet.common.entity.Entity;
import ssixprojet.server.AtlasGame;

@Data
public class World {
	private final List<Entity> entities = new ArrayList<>();
	private final List<Spawn> spawns = new ArrayList<>();
	private final int split = AtlasGame.getConfig().getChunkSplit();
	private final Chunk[][] chunks = new Chunk[split][split];

	public World() {
		double unit, x = 0, y = 0;
		unit = 1. / split;
		int i, j;
		for (i = 0; i < chunks.length; i++, y += unit)
			for (j = 0; j < chunks[i].length; j++, x += unit)
				chunks[i][j] = new Chunk(unit, x, y);

		for (i = 0; i < chunks.length - 1; i++)
			for (j = 0; j < chunks[i].length - 1; j++) {
				chunks[i][j].bottom = chunks[i][j + 1];
				chunks[i][j].right = chunks[i + 1][j];
			}

		for (i = 1; i < chunks.length; i++)
			for (j = 1; j < chunks[i].length; j++) {
				chunks[i][j].top = chunks[i][j - 1];
				chunks[i][j].left = chunks[i - 1][j];
			}
	}

	/**
	 * run map logic
	 */
	public void tick() {
		// TODO link tick method
	}

	/**
	 * add a spawn location
	 * 
	 * @param spawn a spawn location
	 */
	public void addSpawnLocation(Spawn spawn) {
		this.spawns.add(spawn);
	}

	/**
	 * add an entity to this world entities
	 * 
	 * @param e the entity
	 */
	public void spawnEntity(Entity e) {
		if (e.getWorld() != this || !e.isExist())
			entities.add(e);
		// TODO send spawn packet to screen
	}

	/**
	 * remove an entity from this world entities
	 * 
	 * @param e the entity
	 */
	public void killEntity(Entity e) {
		entities.remove(e);
		// TODO send spawn packet to screen
	}

	public void moveEntityChunk(Entity e) {

	}
}
