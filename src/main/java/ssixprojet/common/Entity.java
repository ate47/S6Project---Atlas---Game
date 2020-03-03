package ssixprojet.common;

import lombok.Getter;
import lombok.Setter;

@Getter
public class Entity {
	private boolean exist = false;
	@Setter
	private double x, y;
	private double width, height;

	public Entity(double w, double h) {
		this.width = w;
		this.height = h;
	}

	private World world;

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
		// TODO move algorithm
	}
}
