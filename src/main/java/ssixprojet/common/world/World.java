package ssixprojet.common.world;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import lombok.Data;
import ssixprojet.common.entity.Entity;
import ssixprojet.server.AtlasGame;

@Data
public class World {
	private final List<Entity> entities = new ArrayList<>();
	private final List<Spawn> spawns = new ArrayList<>();
	private final int split = AtlasGame.getConfig().getChunkSplit();
	private final Chunk[] chunks = new Chunk[split * split];

	public World() {
		double unit, x = 0, y = 0;
		unit = 1. / split;
		int i, j;
		for (i = 0; i < split; i++, y += unit)
			for (j = 0; j < split; j++, x += unit)
				setChunk(i, j, new Chunk(unit, x, y));

		Chunk c;
		for (i = 0; i < split - 1; i++)
			for (j = 0; j < split - 1; j++) {
				c = getChunk(i, j);
				c.bottom = getChunk(i, j + 1);
				c.right = getChunk(i + 1, j);
			}

		for (i = 1; i < split; i++)
			for (j = 1; j < split; j++) {
				c = getChunk(i, j);
				c.top = getChunk(i, j - 1);
				c.left = getChunk(i + 1, j);
			}
	}

	public Chunk getChunk(int x, int y) {
		return chunks[x * split + y];
	}

	private void setChunk(int x, int y, Chunk chunk) {
		chunks[x * split + y] = chunk;
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

	public void spawnEntityAtRandomLocation(Entity e) {
		// get a random spawn on the chunk with the minimum player count
		Chunk c = Arrays.stream(chunks).collect(Collectors.minBy((c1, c2) -> c1.getPlayerCount() - c2.getPlayerCount()))
				.orElse(null);
		if (c != null) {
			Spawn s = c.getRandomSpawn();
			e.spawn(this, s.getRandomX(), s.getRandomY());
		}
	}
}
