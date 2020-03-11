package ssixprojet.common.world;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import ssixprojet.common.entity.Entity;

@Getter
public class Chunk {
	private double unit, x, y;
	Chunk top, bottom, left, right;
	private List<Entity> entityList = new ArrayList<>();

	public Chunk(double unit, double x, double y) {
		this.unit = unit;
		this.x = x;
		this.y = y;
	}
}
