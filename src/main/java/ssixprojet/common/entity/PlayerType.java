package ssixprojet.common.entity;

/**
 * mark the current state of the player
 */
public enum PlayerType {
	INFECTED(0), SURVIVOR(1);

	
	private int id;
	PlayerType(int id) {
		this.id = id;
	}
	
	public int getId() {
		return id;
	}
}
