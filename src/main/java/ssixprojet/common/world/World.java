package ssixprojet.common.world;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import ssixprojet.common.GamePhase;
import ssixprojet.common.entity.Entity;
import ssixprojet.common.entity.Player;
import ssixprojet.common.entity.PlayerType;
import ssixprojet.server.AtlasGame;

public class World {
	private static final Random RANDOM = new Random();
	private final List<Entity> entities = new ArrayList<>();
	private final List<Spawn> spawns = new ArrayList<>();
	private final int split = AtlasGame.getConfig().getChunkSplit();
	private final Chunk[] chunks = new Chunk[split * split];
	private double unit;

	public World() {
		double x = 0, y = 0;
		unit = 1. / split;
		int i, j;
		for (i = 0; i < split; i++, y += unit) {
			x = 0;
			for (j = 0; j < split; j++, x += unit)
				setChunk(i, j, new Chunk(unit, x, y));
		}

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

				Spawn newSpawn = new Spawn(sLeft, sTop, sRight - sLeft, sBottom - sTop, outside);

				if (newSpawn.getWidth() > 1 || newSpawn.getHeight() > 1) {
					System.out.println(newSpawn);
					throw new Error();
				}

				c.getSpawns().add(newSpawn);
				spawns.add(newSpawn);
			}
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		World other = (World) obj;
		if (!Arrays.equals(chunks, other.chunks))
			return false;
		if (entities == null) {
			if (other.entities != null)
				return false;
		} else if (!entities.equals(other.entities))
			return false;
		if (spawns == null) {
			if (other.spawns != null)
				return false;
		} else if (!spawns.equals(other.spawns))
			return false;
		if (split != other.split)
			return false;
		return true;
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

	public Chunk getChunk(int x, int y) {
		return x < 0 || y < 0 || x >= split || y >= split ? null : chunks[x * split + y];
	}

	public Chunk[] getChunks() {
		return chunks;
	}

	public List<Entity> getEntities() {
		return entities;
	}

	/**
	 * @return a random chunk of the map
	 */
	public Chunk getRandomChunk() {
		return chunks[RANDOM.nextInt(chunks.length)];
	}

	public List<Spawn> getSpawns() {
		return spawns;
	}

	public int getSplit() {
		return split;
	}

	public double getUnit() {
		return unit;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Arrays.hashCode(chunks);
		result = prime * result + ((entities == null) ? 0 : entities.hashCode());
		result = prime * result + ((spawns == null) ? 0 : spawns.hashCode());
		result = prime * result + split;
		return result;
	}

	/**
	 * remove an entity from this world entities
	 * 
	 * @param e
	 *            the entity
	 */
	public void killEntity(Entity e) {
		entities.remove(e);
	}

	private void setChunk(int x, int y, Chunk chunk) {
		chunks[x * split + y] = chunk;
	}

	/**
	 * add an entity to this world entities
	 * 
	 * @param e
	 *            the entity
	 */
	public void spawnEntity(Entity e) {
		if (e.getWorld() == this && !e.isExist())
			entities.add(e);
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

		Spawn s = Arrays.stream(chunks).filter(c -> !c.getSpawns().isEmpty())
				.collect(Collectors.minBy((c1, c2) -> c1.getPlayerCount() - c2.getPlayerCount())).get()
				.getRandomSpawn();

		double x = s.getRandomX(), y = s.getRandomY();

		if (x > 1 || y > 1)
			System.out.println(s);
		e.spawn(this, x, y);
	}

	/**
	 * walk across every chunk to find the first entity respecting the filter, the
	 * (0,0) direction vector will return the entity at this location or null
	 * 
	 * @param originX
	 *            the origin location x
	 * @param originY
	 *            the origin location y
	 * @param directionX
	 *            the direction vector x
	 * @param directionY
	 *            the direction vector y
	 * @param filter
	 *            the entity filter
	 * @return the first entity respecting the predicate, null otherwise
	 */
	public Entity traceLineAndGetEntity(double originX, double originY, double directionX, double directionY,
			Predicate<Entity> filter) {
		// check numbers validity
		if (!(Double.isFinite(originX) && Double.isFinite(originY) && Double.isFinite(directionX)
				&& Double.isFinite(directionY)))
			return null;

		// (0, 0) vector, get the first matching entity
		if (directionX == 0 && directionY == 0) {
			Chunk c = getChunk(getChunk(originX), getChunk(originY));
			if (c == null)
				return null;

			return c.getEntities().values().stream().filter(e -> e.isIn(originX, originY) && filter.test(e)).findAny()
					.orElse(null);
		}

		// u <- normalized direction vector
		double directionLength = Math.sqrt(directionX * directionX + directionY * directionY);
		double ux = directionX / directionLength;
		double uy = directionY / directionLength;

		
		
		
		return null;
	}

	/**
	 * run map logic
	 */
	public void tick() {
		if (AtlasGame.getAtlas().getPhase() == GamePhase.PLAYING
				&& !getEntities().stream().filter(e -> e instanceof Player).map(e -> (Player) e)
						.filter(p -> p.isConnected() && p.getType() != PlayerType.INFECTED).findAny().isPresent()) {
			AtlasGame.getAtlas().setPhase(GamePhase.SCORE);
		}
	}

	@Override
	public String toString() {
		return "World [entities=" + entities + ", spawns=" + spawns + ", split=" + split + "]";
	}
}
