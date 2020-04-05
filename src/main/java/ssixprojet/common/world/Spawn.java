package ssixprojet.common.world;

import java.util.Random;

public class Spawn {
	private static final Random RANDOM = new Random();
	private double x, y, width, height;
	private boolean outside;

	public Spawn(double x, double y, double width, double height, boolean outside) {
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
		this.outside = outside;
	}

	
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Spawn other = (Spawn) obj;
		if (Double.doubleToLongBits(height) != Double.doubleToLongBits(other.height))
			return false;
		if (outside != other.outside)
			return false;
		if (Double.doubleToLongBits(width) != Double.doubleToLongBits(other.width))
			return false;
		if (Double.doubleToLongBits(x) != Double.doubleToLongBits(other.x))
			return false;
		if (Double.doubleToLongBits(y) != Double.doubleToLongBits(other.y))
			return false;
		return true;
	}



	public double getHeight() {
		return height;
	}



	public double getRandomX() {
		return RANDOM.nextDouble() * width + x;
	}



	public double getRandomY() {
		return RANDOM.nextDouble() * height + y;
	}



	public double getWidth() {
		return width;
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
		temp = Double.doubleToLongBits(height);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		result = prime * result + (outside ? 1231 : 1237);
		temp = Double.doubleToLongBits(width);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(x);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(y);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		return result;
	}



	public boolean isOutside() {
		return outside;
	}



	public void setHeight(double height) {
		this.height = height;
	}



	public void setOutside(boolean outside) {
		this.outside = outside;
	}



	public void setWidth(double width) {
		this.width = width;
	}



	public void setX(double x) {
		this.x = x;
	}



	public void setY(double y) {
		this.y = y;
	}

	@Override
	public String toString() {
		return "Spawn [x=" + x + ", y=" + y + ", width=" + width + ", height=" + height + ", outside=" + outside + "]";
	}
}
