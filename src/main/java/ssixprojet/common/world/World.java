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
		double cx = 0, cy = 0;
		unit = 1. / split;
		for (int i = 0; i < split; i++, cy += unit) {
			cx = 0;
			for (int j = 0; j < split; j++, cx += unit)
				setChunk(j, i, new Chunk(unit, cx, cy));
		}

		Chunk c;
		for (int x = 0; x < split - 1; x++)
			for (int y = 0; y < split - 1; y++) {
				c = getChunk(x, y);
				c.bottom = getChunk(x, y + 1);
				c.right = getChunk(x + 1, y);
			}

		for (int x = 1; x < split; x++)
			for (int y = 1; y < split; y++) {
				c = getChunk(x, y);
				c.top = getChunk(x, y - 1);
				c.left = getChunk(x - 1, y);
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

	/**
	 * walk across chunks to find an entity
	 * 
	 * @param originX
	 *            the line origin X
	 * @param originY
	 *            the line origin Y
	 * @param directionX
	 *            the direction vector X
	 * @param directionY
	 *            the direction vector Y
	 * @param filter
	 *            the entity filter
	 * @param answer
	 *            the answer to fill
	 * @return true if an entity has been found, false otherwise
	 */
	public boolean traceLineAndGetEntity(double originX, double originY, double directionX, double directionY,
			Predicate<Entity> filter, TraceAnswer answer) {
		answer.clear();

		// check numbers validity
		if (!(Double.isFinite(originX) && Double.isFinite(originY) && Double.isFinite(directionX)
				&& Double.isFinite(directionY)))
			return false;

		Chunk start = getChunk(getChunk(originX), getChunk(originY));

		// (0, 0) vector, get the first matching entity
		if (directionX == 0 && directionY == 0) {
			return (start == null) ? false
					: start.searchEntity(originX, originY, directionX, directionY, filter, answer);

		}

		// u <- normalized direction vector
		double directionLength = Math.sqrt(directionX * directionX + directionY * directionY);
		double ux = directionX / directionLength;
		double uy = directionY / directionLength;

		if (ux < 0) { // left
			if (uy > 0) { // bottom
				if (-ux < uy) { // 6
					return traceLineAndGetEntityOctant6(start, originX, originY, ux, uy, filter, answer);
				} else { // 5
					return traceLineAndGetEntityOctant5(start, originX, originY, ux, uy, filter, answer);
				}
			} else { // top
				if (-ux < -uy) { // 3
					return traceLineAndGetEntityOctant3(start, originX, originY, ux, uy, filter, answer);
				} else { // 4
					return traceLineAndGetEntityOctant4(start, originX, originY, ux, uy, filter, answer);
				}
			}
		} else {
			if (uy > 0) { // bottom
				if (ux < uy) { // 7
					return traceLineAndGetEntityOctant7(start, originX, originY, ux, uy, filter, answer);
				} else { // 8
					return traceLineAndGetEntityOctant8(start, originX, originY, ux, uy, filter, answer);
				}
			} else { // top
				if (ux < -uy) { // 2
					return traceLineAndGetEntityOctant2(start, originX, originY, ux, uy, filter, answer);
				} else { // 1
					return traceLineAndGetEntityOctant1(start, originX, originY, ux, uy, filter, answer);
				}
			}
		}
	}

	private boolean traceLineAndGetEntityOctant1(Chunk start, double originX, double originY, double ux, double uy,
			Predicate<Entity> filter, TraceAnswer answer) {
		double error;
		final double diff;

		if (uy == 0 || ux == 0) {
			error = 0;
			diff = 0;
		} else {
			diff = -uy * unit / ux;
			double h = (originX - start.getX()) * -uy / ux;
			error = start.getY() + unit - (originY + h);
		}
		for (Chunk c = start; c != null; c = c.right) {
			if (c.searchEntityNormalized(originX, originY, ux, uy, filter, answer))
				return true;

			error += diff;
			if (error > unit) {
				error -= unit;
				c = c.top;
				if (c == null)
					return false;
				else if (c.searchEntityNormalized(originX, originY, ux, uy, filter, answer))
					return true;
			}
		}

		return false;
	}

	private boolean traceLineAndGetEntityOctant2(Chunk start, double originX, double originY, double ux, double uy,
			Predicate<Entity> filter, TraceAnswer answer) {
		double error;
		final double diff;

		if (uy == 0 || ux == 0) {
			error = 0;
			diff = 0;
		} else {
			diff = ux * unit / -uy;
			error = -(originY - start.getY()) / unit * diff;
		}
		
		for (Chunk c = start; c != null; c = c.top) {
			if (c.searchEntityNormalized(originX, originY, ux, uy, filter, answer))
				return true;

			error += diff;

			if (error > unit) {
				error -= unit;
				c = c.right;
				if (c == null)
					return false;
				else if (c.searchEntityNormalized(originX, originY, ux, uy, filter, answer))
					return true;
			}

		}

		return false;
	}

	private boolean traceLineAndGetEntityOctant3(Chunk start, double originX, double originY, double ux, double uy,
			Predicate<Entity> filter, TraceAnswer answer) {
		double error;
		final double diff;

		if (uy == 0 || ux == 0) {
			error = 0;
			diff = 0;
		} else {
			diff = -ux * unit / -uy;
			error = -(originY - start.getY()) / unit * diff;
		}

		for (Chunk c = start; c != null; c = c.top) {
			if (c.searchEntityNormalized(originX, originY, ux, uy, filter, answer))
				return true;

			error += diff;

			if (error > unit) {
				error -= unit;
				c = c.left;
				if (c == null)
					return false;
				else if (c.searchEntityNormalized(originX, originY, ux, uy, filter, answer))
					return true;
			}

		}

		return false;
	}

	private boolean traceLineAndGetEntityOctant4(Chunk start, double originX, double originY, double ux, double uy,
			Predicate<Entity> filter, TraceAnswer answer) {
		double error;
		final double diff;

		if (uy == 0 || ux == 0) {
			error = 0;
			diff = 0;
		} else {
			diff = -uy * unit / -ux;
			error = -(originX - start.getX()) / unit * diff;
		}

		for (Chunk c = start; c != null; c = c.left) {
			if (c.searchEntityNormalized(originX, originY, ux, uy, filter, answer))
				return true;

			error += diff;

			if (error > unit) {
				error -= unit;
				c = c.top;
				if (c == null)
					return false;
				else if (c.searchEntityNormalized(originX, originY, ux, uy, filter, answer))
					return true;
			}

		}

		return false;
	}

	private boolean traceLineAndGetEntityOctant5(Chunk start, double originX, double originY, double ux, double uy,
			Predicate<Entity> filter, TraceAnswer answer) {
		double error;
		final double diff;

		if (uy == 0 || ux == 0) {
			error = 0;
			diff = 0;
		} else {
			diff = uy * unit / -ux;
			error = -(originX - start.getX()) / unit * diff;
		}

		for (Chunk c = start; c != null; c = c.left) {
			if (c.searchEntityNormalized(originX, originY, ux, uy, filter, answer))
				return true;

			error += diff;

			if (error > unit) {
				error -= unit;
				c = c.bottom;
				if (c == null)
					return false;
				else if (c.searchEntityNormalized(originX, originY, ux, uy, filter, answer))
					return true;
			}

		}

		return false;
	}

	private boolean traceLineAndGetEntityOctant6(Chunk start, double originX, double originY, double ux, double uy,
			Predicate<Entity> filter, TraceAnswer answer) {
		double error;
		final double diff;

		if (uy == 0 || ux == 0) {
			error = 0;
			diff = 0;
		} else {
			diff = -ux * unit / uy;
			error = (originY - start.getY()) / unit * diff;
		}

		for (Chunk c = start; c != null; c = c.bottom) {
			if (c.searchEntityNormalized(originX, originY, ux, uy, filter, answer))
				return true;

			error += diff;

			if (error > unit) {
				error -= unit;
				c = c.left;
				if (c == null)
					return false;
				else if (c.searchEntityNormalized(originX, originY, ux, uy, filter, answer))
					return true;
			}

		}

		return false;
	}

	private boolean traceLineAndGetEntityOctant7(Chunk start, double originX, double originY, double ux, double uy,
			Predicate<Entity> filter, TraceAnswer answer) {
		double error;
		final double diff;

		if (uy == 0 || ux == 0) {
			error = 0;
			diff = 0;
		} else {
			diff = ux * unit / uy;
			error = (originY - start.getY()) / unit * diff;
		}

		for (Chunk c = start; c != null; c = c.bottom) {
			if (c.searchEntityNormalized(originX, originY, ux, uy, filter, answer))
				return true;

			error += diff;

			if (error > unit) {
				error -= unit;
				c = c.right;
				if (c == null)
					return false;
				else if (c.searchEntityNormalized(originX, originY, ux, uy, filter, answer))
					return true;
			}

		}

		return false;
	}

	private boolean traceLineAndGetEntityOctant8(Chunk start, double originX, double originY, double ux, double uy,
			Predicate<Entity> filter, TraceAnswer answer) {
		double error;
		final double diff;

		if (uy == 0 || ux == 0) {
			error = 0;
			diff = 0;
		} else {
			diff = uy * unit / ux;
			double h = (originX - start.getX()) * uy / ux;
			error = (originY - h) - start.getY();
		}

		for (Chunk c = start; c != null; c = c.right) {
			if (c.searchEntityNormalized(originX, originY, ux, uy, filter, answer))
				return true;

			error += diff;

			if (error > unit) {
				error -= unit;
				c = c.bottom;
				if (c == null)
					return false;
				else if (c.searchEntityNormalized(originX, originY, ux, uy, filter, answer))
					return true;
			}

		}

		return false;
	}
}
