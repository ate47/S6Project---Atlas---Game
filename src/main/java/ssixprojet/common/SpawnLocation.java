package ssixprojet.common;

public class SpawnLocation {
	private boolean outside;
	private int x, y, width, height;

	public SpawnLocation(boolean outside, int x, int y, int width, int height) {
		super();
		this.outside = outside;
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		SpawnLocation other = (SpawnLocation) obj;
		if (height != other.height)
			return false;
		if (outside != other.outside)
			return false;
		if (width != other.width)
			return false;
		if (x != other.x)
			return false;
		if (y != other.y)
			return false;
		return true;
	}

	public int getHeight() {
		return height;
	}

	public int getWidth() {
		return width;
	}

	public int getX() {
		return x;
	}

	public int getY() {
		return y;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + height;
		result = prime * result + (outside ? 1231 : 1237);
		result = prime * result + width;
		result = prime * result + x;
		result = prime * result + y;
		return result;
	}

	public boolean isOutside() {
		return outside;
	}

	public void setHeight(int height) {
		this.height = height;
	}

	public void setOutside(boolean outside) {
		this.outside = outside;
	}

	public void setWidth(int width) {
		this.width = width;
	}

	public void setX(int x) {
		this.x = x;
	}

	public void setY(int y) {
		this.y = y;
	}

	@Override
	public String toString() {
		return "SpawnLocation [outside=" + outside + ", x=" + x + ", y=" + y + ", width=" + width + ", height=" + height
				+ "]";
	}

}
