package JEU;

public class Zombie extends Perso{

	private int numZombie;
	private int nb_zombie = 0;
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
	
	public int getNB() {
		return this.nb_zombie;
	}
	
	public String toString() {
		return "Num_zombie :"+this.numZombie;
	}
	
	
}
