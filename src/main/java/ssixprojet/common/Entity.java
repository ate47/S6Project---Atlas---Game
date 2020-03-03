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
	public void spawn(World w, double x, double y) {
		this.world = w;
		this.x = x;
		this.y = y;
		this.exist = true;
	}
	
	public void kill() {
		this.exist = false;
	}
	
	public void move(double dx, double dy) {
		// TODO move algorithm
	}
}
