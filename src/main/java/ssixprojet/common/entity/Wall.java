package ssixprojet.common.entity;

public class Wall extends Entity {
	public Wall(double width, double height) {
		super(width, height);
	}
	
	@Override
	public boolean isSolid() {
		return true;
	}
}
