package ssixprojet.common.world;

import java.util.Random;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Spawn {
	private static final Random RANDOM = new Random();
	private double x, y, width, height;
	private boolean outside;

	public double getRandomX() {
		return RANDOM.nextDouble() * width + x;
	}

	public double getRandomY() {
		return RANDOM.nextDouble() * height + y;
	}
}
