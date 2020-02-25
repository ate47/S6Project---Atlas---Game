package JEU;

public class Zombie extends Perso{

	protected IPerso tz;
	
	public Zombie() {
		// TODO Auto-generated constructor stub
		super(TYPEZ, x, y, v);
		IPerso zombie = IPerso.ZOMBIE;
		tz = zombie;
	}
	
	public IPerso getTZ() {
		return this.tz;
	}
	

	
	
}
