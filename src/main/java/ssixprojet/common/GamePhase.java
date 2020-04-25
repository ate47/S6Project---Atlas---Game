package ssixprojet.common;

public enum GamePhase {
	WAITING(0), PLAYING(1), SCORE(2);
	
	private int id;
	
	GamePhase(int id) {
		this.id = id;
	}
	
	public int getId() {
		return id;
	}
}
