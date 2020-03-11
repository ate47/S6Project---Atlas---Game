package ssixprojet.common.world;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;
import ssixprojet.common.entity.Entity;
import ssixprojet.server.AtlasGame;

@Data
public class World {
	private List<Entity> entities = new ArrayList<>();
	private Chunk[][] chunks = new Chunk[AtlasGame.getConfig().getChunkSplit()][AtlasGame.getConfig()
			.getChunkSplit()];

	public World() {
		int i, j;
		for (i = 0; i < chunks.length; i++)
			for (j = 0; j < chunks[i].length; i++)
				chunks[i][j] = new Chunk();
	}

	/**
	 * run map logic
	 */
	public void tick() {
		// TODO link tick method
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
