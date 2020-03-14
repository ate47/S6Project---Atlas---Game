package ssixprojet.common.world;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import lombok.Getter;
import ssixprojet.common.entity.Entity;
import ssixprojet.common.entity.Player;
import ssixprojet.common.entity.PlayerType;

@Getter
public class Chunk {
	private static final Random RANDOM = new Random();
	private double unit, x, y;
	Chunk top, bottom, left, right;
	private List<Entity> entityList = new ArrayList<>();
	private List<Spawn> spawns = new ArrayList<>();

	public int getPlayerCount() {
		return (int) entityList.stream()
				.filter(e -> e instanceof Player && ((Player) e).getType() == PlayerType.SURVIVOR).count();
	}

	public Chunk(double unit, double x, double y) {
		this.unit = unit;
		this.x = x;
		this.y = y;
	}

	public Spawn getRandomSpawn() {
		return spawns.get(RANDOM.nextInt(spawns.size()));
	}
}
