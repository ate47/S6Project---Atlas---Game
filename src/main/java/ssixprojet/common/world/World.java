package ssixprojet.common.world;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import lombok.Data;
import ssixprojet.common.entity.Entity;
import ssixprojet.server.AtlasGame;

@Data
public class World {
	private static final Random RANDOM = new Random();
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
				c.left = getChunk(i - 1, j);
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
	 * get the chunk that represend this location
	 * 
	 * @param location
	 *            the x or y location
	 * @return the index of the chunk
	 */
	public int getChunk(double location) {
		return (int) (location * split);
	}

	/**
	 * @return a random chunk of the map
	 */
	public Chunk getRandomChunk() {
		return chunks[RANDOM.nextInt(chunks.length)];
	}

	/**
	 * add a spawn location
	 * 
	 * @param spawn
	 *            a spawn location
	 */
	public void addSpawnLocation(double x, double y, double width, double height, boolean outside) {
		int left = getChunk(x);
		int right = getChunk(x + width);
		int top = getChunk(y);
		int bottom = getChunk(y + height);

		for (int i = left; i <= right; i++)
			for (int j = top; j <= bottom; j++) {
				Chunk c = getChunk(i, j);

				double sLeft, sRight, sTop, sBottom;

				if (i != left)
					sLeft = c.getX();
				else
					sLeft = x;

				if (i != right)
					sRight = c.getX() + c.getUnit();
				else
					sRight = x + width;

				if (j != top)
					sTop = c.getY();
				else
					sTop = y;

				if (j != bottom)
					sBottom = c.getY() + c.getUnit();
				else
					sBottom = y + height;

				spawns.add(new Spawn(sLeft, sTop, sRight - sLeft, sBottom - sTop, outside));
			}
	}

	/**
	 * add an entity to this world entities
	 * 
	 * @param e
	 *            the entity
	 */
	public void spawnEntity(Entity e) {
		if (e.getWorld() != this || !e.isExist())
			entities.add(e);
		// TODO send spawn packet to screen
	}

	/**
	 * remove an entity from this world entities
	 * 
	 * @param e
	 *            the entity
	 */
	public void killEntity(Entity e) {
		entities.remove(e);
		// TODO send spawn packet to screen
	}

	public void moveEntityChunk(Entity e) {

	}

	/**
	 * spawn an entity where there is a minimum survivor, this method call the
	 * {@link Entity#spawn(World, double, double)} method.
	 * 
	 * @param e
	 *            the entity
	 */
	public void spawnEntityAtRandomLocation(Entity e) {
		// get a random spawn on the chunk with the minimum player count
		Spawn s = Arrays.stream(chunks).collect(Collectors.minBy((c1, c2) -> c1.getPlayerCount() - c2.getPlayerCount()))
				.orElseGet(this::getRandomChunk).getRandomSpawn();
		e.spawn(this, s.getRandomX(), s.getRandomY());
	}
}
