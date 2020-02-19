package ssixprojet.utils;

import lombok.Data;

public @Data class Vector {
	public static final Vector NULL_VECTOR = new Vector(0, 0);
	private final double x, y;

	/**
	 * add operation
	 * 
	 * @param x the x location to shift
	 * @param y the y location to shift
	 * @return the vector this + (x,y)
	 */
	public Vector add(double x, double y) {
		return new Vector(this.x + x, this.y + y);
	}

	/**
	 * add operation
	 * 
	 * @param v the vector to add
	 * @return the vector this + v
	 */

	public Vector add(Vector v) {
		return add(v.x, v.y);
	}

	/**
	 * mult operation
	 * 
	 * @param r the real to mult
	 * @return the vector this * r
	 */
	public Vector mult(double r) {
		return new Vector(x * r, y * r);
	}

	/**
	 * compute ||p2 - this||, this method is slower than
	 * {@link #distanceSquared(Vector)}
	 * 
	 * @param p2 the second vector
	 * @return the distance
	 */
	public double distance(Vector p2) {
		return Math.sqrt(distanceSquared(p2));
	}

	/**
	 * compute ||p2 - this||^2, this method is faster than {@link #distance(Vector)}
	 * 
	 * @param p2 the second vector
	 * @return the distance
	 */
	public double distanceSquared(Vector p2) {
		return (p2.x - x) * (p2.x - x) + (p2.y - y) * (p2.y - y);
	}

	/**
	 * @return the length of this vector, this method is slower than
	 *         {@link #lengthSquared()}
	 */
	public double lenght() {
		return Math.sqrt(lengthSquared());
	}

	/**
	 * @return the length squared of this vector, this method is faster than
	 *         {@link #lenght()}
	 */
	public double lengthSquared() {
		return x * x + y * y;
	}

	/**
	 * @return normalize this vector, if the length is 0, return it
	 */
	public Vector normalized() {
		double ls = lengthSquared();
		if (ls == 0)
			return this;
		double l = Math.sqrt(lengthSquared());
		return new Vector(x / l, y / l);
	}

}
