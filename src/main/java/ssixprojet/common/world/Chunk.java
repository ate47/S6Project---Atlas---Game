package ssixprojet.common.world;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

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

	public boolean isIn(Entity e) {
		return x < e.getX() + e.getWidth() && x + unit > e.getX() && y < e.getY() + e.getHeight()
				&& y + unit > e.getY();
	}

	public void removeEntity(Entity e) {
		entities.remove(e.getEntityId());
	}
}
