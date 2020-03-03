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
	
	public void spawn(double x, double y) {
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
