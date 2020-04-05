package ssixprojet.common;

import ssixprojet.utils.Vector;

/**
 * represent a edge of the map
 */
public class MapEdge {

	/**
	 * the orientation
	 */
	public enum Orientation {
		BOTTOM(new Vector(0, 1)), RIGHT(new Vector(1, 0));

		public final Vector desc;

		Orientation(Vector desc) {
			this.desc = desc;
		}

	}

	private Orientation orientation;

	private double x, y, length;

	public MapEdge(double x, double y, double length, Orientation orientation) {
		this.x = x;
		this.y = y;
		this.length = length;
		this.orientation = orientation;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		MapEdge other = (MapEdge) obj;
		if (Double.doubleToLongBits(length) != Double.doubleToLongBits(other.length))
			return false;
		if (orientation != other.orientation)
			return false;
		if (Double.doubleToLongBits(x) != Double.doubleToLongBits(other.x))
			return false;
		if (Double.doubleToLongBits(y) != Double.doubleToLongBits(other.y))
			return false;
		return true;
	}

	public double getLength() {
		return length;
	}

	public Orientation getOrientation() {
		return orientation;
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
		temp = Double.doubleToLongBits(length);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		result = prime * result + ((orientation == null) ? 0 : orientation.hashCode());
		temp = Double.doubleToLongBits(x);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(y);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		return result;
	}

	public void setLength(double length) {
		this.length = length;
	}

	public void setOrientation(Orientation orientation) {
		this.orientation = orientation;
	}

	public void setX(double x) {
		this.x = x;
	}

	public void setY(double y) {
		this.y = y;
	}
	@Override
	public String toString() {
		return "MapEdge [x=" + x + ", y=" + y + ", length=" + length + ", orientation=" + orientation + "]";
	}

}
