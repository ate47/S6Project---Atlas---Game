package ssixprojet.common.entity;

import java.util.Iterator;
import java.util.concurrent.atomic.AtomicInteger;

import ssixprojet.common.world.Chunk;
import ssixprojet.common.world.World;

public class Entity {
	private static final Chunk[][] EMPTY_AREA = new Chunk[0][0];
	private static final AtomicInteger INCREMENT = new AtomicInteger(0);
	private boolean exist = false;
	private double x, y;
	private double width, height;
	private final int id;
	private World world;
	private Chunk[][] area = EMPTY_AREA;

	public Entity(double w, double h) {
		this.width = w;
		this.height = h;
		// avoid conflict
		x = -1 - w;
		y = -1 - h;
		id = INCREMENT.getAndIncrement();
	}

	public boolean collide(Entity e) {
		return x < e.getX() + e.width && x + width > e.getX() && y < e.getY() + e.height && y + height > e.getY();
	}

	public Chunk[][] getArea() {
		return area;
	}

	public int getEntityId() {
		return id;
	}

	public double getHeight() {
		return height;
	}

	public double getSpeed() {
		return 0.5;
	}

	public double getWidth() {
		return width;
	}

	public World getWorld() {
		return world;
	}

	public double getX() {
		return x;
	}

	public double getY() {
		return y;
	}

	public boolean isExist() {
		return exist;
	}

	public boolean isIn(double ex, double ey) {
		return x <= ex && y <= ey && x + width >= ex && y + height >= ey;
	}

	public boolean isSolid() {
		return false;
	}

	/**
	 * kill this entity
	 */
	public void kill() {
		getWorld().killEntity(this);
		this.exist = false;
		for (Chunk[] cs : area)
			for (Chunk c : cs)
				if (c != null)
					c.removeEntity(this);
		// avoid conflict
		x = -1 - width;
		y = -1 - height;
	}

	/**
	 * move the entity
	 * 
	 * @param dx
	 *            the x delta
	 * @param dy
	 *            the y delta
	 */
	public void move(double dx, double dy) {
		double oldX = x;
		double oldY = y;
		double nx = x = x + dx * width * getSpeed();
		double ny = y = y + dy * height * getSpeed();

		for (Iterator<Entity> it = world.getEntities().stream().filter(Entity::isSolid).iterator(); it.hasNext();) {
			Entity wall = it.next();

			if (collide(wall)) {
				x = oldX;
				y = oldY;
				return; // TODO: better algorithm
			}
		}
		x = oldX;
		y = oldY;
		setLocation(nx, ny);
	}

	/**
	 * respawn this entity in the same world, does nothing if the entity isn't in a
	 * world
	 * 
	 * @param x
	 *            the new x location
	 * @param y
	 *            the new y location
	 */
	public void respawn(double x, double y) {
		if (!exist && world != null) {
			world.spawnEntity(this);
			this.exist = true;
		}
		setLocation(x, y);
	}

	public void setLocation(double newx, double newy) {
		if (world == null) {
			area = EMPTY_AREA;
			this.x = -1;
			this.y = -1;
			return;
		}
		double oldX = this.x;
		double oldY = this.y;

		this.x = newx;
		this.y = newy;

		int x = world.getChunk(this.x), y_ = world.getChunk(this.y);
		for (int i = 0; i < area.length; i++, x++) {
			int y = y_;
			for (int j = 0; j < area[i].length; j++, y++) {
				Chunk oldc = area[i][j];
				// remove if this new location isn't in this chunk
				if (oldc != null && !oldc.isIn(this))
					oldc.removeEntity(this);

				Chunk newc = area[i][j] = world.getChunk(x, y);
				// add if this new chunk wasn't colliding with the old location
				if (newc != null && !newc.isIn(oldX, oldY, width, height) && newc.isIn(this)) {
					newc.addEntity(this);
				}
			}
		}
	}

	/**
	 * @param p
	 *            the shooter
	 * @return if the shot killed
	 */
	public boolean shot(Player p) {
		return false;
	}

	/**
	 * spawn an entity in a world
	 * 
	 * @param w
	 *            the world to spawn
	 * @param x
	 *            the new x location
	 * @param y
	 *            the new y location
	 */
	public void spawn(World w, double x, double y) {
		this.world = w;
		world.spawnEntity(this);
		this.exist = true;
		int n = (int) (getWidth() / w.getUnit() + 2);
		int m = (int) (getHeight() / w.getUnit() + 2);
		area = new Chunk[n][m];
		setLocation(x, y);
	}

	@Override
	public String toString() {
		return "Entity [exist=" + exist + ", x=" + x + ", y=" + y + ", width=" + width + ", height=" + height
				+ ", world=" + (world != null) + "]";
	}

}
