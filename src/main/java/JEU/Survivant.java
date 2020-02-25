package JEU;

public class Survivant extends Perso{

	private int numSurvivant;
	private int nb_survivant = 0;
	protected IPerso ts;
	
	public Survivant() {
		super(TYPES, x, y, v);
		// TODO Auto-generated constructor stub
		IPerso survivant = IPerso.SURVIVANT;
		this.ts = survivant;
	}
	
	public IPerso getTS() {
		return this.ts;
	}
	
	public int getNB() {
		return this.nb_survivant;
	}
	
	public String toString() {
		return "Num_survivant :"+this.numSurvivant;
	}
	
	
}