package ssixprojet.common.world;

import ssixprojet.common.entity.Entity;

public class TraceAnswer {
	private Entity target;
	private double x, y;

	public TraceAnswer() {
		clear();
	}

	public Entity getTarget() {
		return target;
	}

	public double getX() {
		return x;
	}

	public double getY() {
		return y;
	}

	public void clear() {
		target = null;
	}

	public boolean isFound() {
		return target != null;
	}

	public void set(Entity target, double x, double y) {
		this.target = target;
		this.x = x;
		this.y = y;
	}
}
