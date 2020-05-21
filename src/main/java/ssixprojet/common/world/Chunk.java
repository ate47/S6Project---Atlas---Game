package ssixprojet.common.world;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.function.Predicate;

import ssixprojet.common.entity.Entity;
import ssixprojet.common.entity.Player;
import ssixprojet.common.entity.PlayerType;

public class Chunk {
	private static final Random RANDOM = new Random();
	private double unit, x, y;
	Chunk top, bottom, left, right;

	private Map<Integer, Entity> entities = new HashMap<>();

	private List<Spawn> spawns = new ArrayList<>();

	public Chunk(double unit, double x, double y) {
		this.unit = unit;
		this.x = x;
		this.y = y;
	}

	public void addEntity(Entity e) {
		entities.put(e.getEntityId(), e);
	}

	public Chunk getBottom() {
		return bottom;
	}

	public Map<Integer, Entity> getEntities() {
		return entities;
	}

	public Chunk getLeft() {
		return left;
	}

	public int getPlayerCount() {
		int count = getRealPlayerCount();

		if (top != null)
			count += top.getRealPlayerCount();

		if (bottom != null)
			count += bottom.getRealPlayerCount();

		if (right != null)
			count += right.getRealPlayerCount();

		if (left != null)
			count += left.getRealPlayerCount();

		return count;
	}

	public Spawn getRandomSpawn() {
		return spawns.get(RANDOM.nextInt(spawns.size()));
	}

	private int getRealPlayerCount() {
		return (int) entities.values().stream()
				.filter(e -> e instanceof Player && ((Player) e).getType() == PlayerType.SURVIVOR).count();
	}

	public Chunk getRight() {
		return right;
	}

	public List<Spawn> getSpawns() {
		return spawns;
	}

	public Chunk getTop() {
		return top;
	}

	public double getUnit() {
		return unit;
	}

	public double getX() {
		return x;
	}

	public double getY() {
		return y;
	}

	public boolean isIn(double ex, double ey, double w, double h) {
		return x < ex + w && x + unit > ex && y < ey + h && y + unit > ey;
	}

	public boolean isIn(Entity e) {
		return isIn(e.getX(), e.getY(), e.getWidth(), e.getHeight());
	}

	public void removeEntity(Entity e) {
		entities.remove(e.getEntityId());
	}

	/**
	 * search an entity in the chunk
	 * 
	 * @param originX
	 *            the origin x
	 * @param originY
	 *            the origin y
	 * @param ux
	 *            the direction vector x
	 * @param uy
	 *            the direction vector y
	 * @param filter
	 *            the entity filter
	 * @param answer
	 *            the answer to fill
	 * @return if an entity is found
	 */
	public boolean searchEntity(double originX, double originY, double directionX, double directionY,
			Predicate<Entity> filter, TraceAnswer answer) {
		// check numbers validity
		if (!(Double.isFinite(originX) && Double.isFinite(originY) && Double.isFinite(directionX)
				&& Double.isFinite(directionY)))
			return false;

		// (0, 0) vector, get the first matching entity
		if (directionX == 0 && directionY == 0) {
			Entity e = entities.values().stream().filter(ee -> ee.isIn(originX, originY) && filter.test(ee)).findAny()
					.orElse(null);
			if (e != null) {
				answer.set(e, originX, originY);
				return true;
			} else
				return false;
		}

		// u <- normalized direction vector
		double directionLength = Math.sqrt(directionX * directionX + directionY * directionY);
		double ux = directionX / directionLength;
		double uy = directionY / directionLength;

		return searchEntityNormalized(originX, originY, ux, uy, filter, answer);
	}

	/**
	 * search an entity in the chunk
	 * 
	 * @param originX
	 *            the origin x
	 * @param originY
	 *            the origin y
	 * @param ux
	 *            the normalized direction vector x
	 * @param uy
	 *            the normalized direction vector y
	 * @param filter
	 *            the entity filter
	 * @param answer
	 *            the answer to fill
	 * @return if an entity is found
	 */
	boolean searchEntityNormalized(double originX, double originY, double ux, double uy, Predicate<Entity> filter,
			TraceAnswer answer) {
		double distance = Double.MAX_VALUE; // a large number consider as Inf
		Entity target = null;
		double xi = 0, yi = 0;

		for (Entity e : getEntities().values()) {
			if (!filter.test(e))
				continue;
			double x, y;

			// opti : 2 bord a calcule
			if (ux < 0) {
				x = e.getX() + e.getWidth();
			} else {
				x = e.getX();
			}
			if (uy < 0) {
				y = e.getY() + e.getHeight();
			} else {
				y = e.getY();
			}

			if (uy != 0) {
				double k = ((y - originY) / uy);
				if (k > 0) {
					double xt = k * ux + originX;
					if (xt >= e.getX() && xt <= e.getX() + e.getWidth()) {
						double dt = (xt - originX) * (xt - originX) + (y - originY) * (y - originY);

						if (dt < distance) {
							distance = dt;
							target = e;
							xi = xt;
							yi = y;
							continue;
						}
					}
				}
			}

			if (ux != 0) {
				double k = ((x - originX) / ux);
				if (k > 0) {
					double yt = k * uy + originY;
					if (yt >= e.getY() && yt <= e.getY() + e.getHeight()) {
						double dt = (x - originX) * (x - originX) + (yt - originY) * (yt - originY);

						if (dt < distance) {
							distance = dt;
							target = e;
							xi = x;
							yi = yt;
							continue;
						}
					}
				}
			}

		}
		if (target != null) {
			answer.set(target, xi, yi);
			return true;
		}
		return false;
	}
}
