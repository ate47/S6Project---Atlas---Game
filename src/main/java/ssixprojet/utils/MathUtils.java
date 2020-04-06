package ssixprojet.utils;

public class MathUtils {
	public static double clamp(double value, double min, double max) {
		return value > min ? value < max ? value : max : min;
	}
}
