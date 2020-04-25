package ssixprojet.common.entity;

import java.util.Iterator;

import ssixprojet.common.world.World;

public class Entity {
	private boolean exist = false;
	private double x, y;
	private double width, height;

	private World world;

	public Entity(double w, double h) {
		this.width = w;
		this.height = h;
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

	public boolean isSolid() {
		return false;
	}

	/**
	 * kill this entity
	 */
	public void kill() {
		getWorld().killEntity(this);
		this.exist = false;
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
		x = x + dx * width * getSpeed();
		y = y + dy * height * getSpeed();

		for (Iterator<Entity> it = world.getEntities().stream().filter(Entity::isSolid).iterator(); it.hasNext();) {
			Entity wall = it.next();

			if (collide(wall)) {
				x = oldX;
				y = oldY;
				return; // TODO: better algorithm
			}
		}
	}

	public boolean collide(Entity e) {
		return x < e.getX() + e.width &&
				x + width > e.getX() &&
				   y < e.getY() + e.height &&
				   y + height > e.getY();
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
		this.x = x;
		this.y = y;
	}

	public void setX(double x) {
		this.x = x;
	}

	public void setY(double y) {
		this.y = y;
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
		this.x = x;
		this.y = y;
		this.exist = true;
	}

	public void shot(Player p) {
	}
	
	@Override
	public String toString() {
		return "Entity [exist=" + exist + ", x=" + x + ", y=" + y + ", width=" + width + ", height=" + height
				+ ", world=" + (world != null) + "]";
	}

}
