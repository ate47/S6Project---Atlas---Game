package ssixprojet.common;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;

@Data
public class World {
	private List<Entity> entities = new ArrayList<>();
	private GameMap map;

	public World(GameMap map) {
		this.map = map;
	}
	
	public void tick() {
		// TODO link tick method
	}
}
