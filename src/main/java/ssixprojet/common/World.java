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

	/**
	 * run map logic
	 */
	public void tick() {
		// TODO link tick method
	}

	/**
	 * add an entity to this world entities
	 * 
	 * @param e
	 *            the entity
	 */
	public void spawnEntity(Entity e) {
		if (e.getWorld() != this || !e.isExist())
			entities.add(e);
		// TODO send spawn packet to screen
	}

	/**
	 * remove an entity from this world entities
	 * 
	 * @param e
	 *            the entity
	 */
	public void killEntity(Entity e) {
		entities.remove(e);
		// TODO send spawn packet to screen
	}
}
