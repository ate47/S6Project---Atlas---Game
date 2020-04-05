package ssixprojet.utils;

public class Vector {
	public static final Vector NULL_VECTOR = new Vector(0, 0);
	private final double x, y;

	public Vector(double x, double y) {
		super();
		this.x = x;
		this.y = y;
	}

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

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Vector other = (Vector) obj;
		if (Double.doubleToLongBits(x) != Double.doubleToLongBits(other.x))
			return false;
		if (Double.doubleToLongBits(y) != Double.doubleToLongBits(other.y))
			return false;
		return true;
	}

	public double getX() {
		return x;
	}

	public double getY() {
		return y;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		long temp;
		temp = Double.doubleToLongBits(x);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(y);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		return result;
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
	 * mult operation
	 * 
	 * @param r the real to mult
	 * @return the vector this * r
	 */
	public Vector mult(double r) {
		return new Vector(x * r, y * r);
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

	@Override
	public String toString() {
		return "Vector [x=" + x + ", y=" + y + "]";
	}

}
