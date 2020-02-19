package ssixprojet.common;

import lombok.Data;
import ssixprojet.utils.Vector;

/**
 * represent a edge of the map
 */
public @Data class MapEdge {
	/**
	 * the orientation
	 */
	public enum Orientation {
		RIGHT(new Vector(1, 0)), BOTTOM(new Vector(0, 1));

		public final Vector desc;

		Orientation(Vector desc) {
			this.desc = desc;
		}

	}

	private double x, y, length;
	private Orientation orientation;

}
