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

	public void respawn(double x, double y) {
		if (!exist && world != null) {
			world.getEntities().add(this);
			this.exist = true;
		}
		this.x = x;
		this.y = y;
	}

	public void spawn(World w, double x, double y) {
		this.world = w;
		w.getEntities().add(this);
		this.x = x;
		this.y = y;
		this.exist = true;
	}

	public void kill() {
		getWorld().getEntities().remove(this);
		this.exist = false;
	}

	public void move(double dx, double dy) {
		// TODO move algorithm
	}
}
