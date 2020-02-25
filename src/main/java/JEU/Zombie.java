package JEU;

public class Zombie extends Perso{

	private int numZombie;
	private static int NB_ZOMBIE = 0;
	protected IPerso tz;
	
	public Zombie() {
		// TODO Auto-generated constructor stub
		super(TYPEZ, x, y, v);
		IPerso zombie = IPerso.ZOMBIE;
		tz = zombie;
		this.numZombie = ++NB_ZOMBIE;
	}
	
	public IPerso getTZ() {
		return this.tz;
	}
	
	public int getNB() {
		return Zombie.NB_ZOMBIE;
	}
	
	public String toString() {
		return "Num_zombie :"+this.numZombie;
	}
	
	
}
