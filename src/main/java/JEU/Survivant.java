package JEU;

public class Survivant extends Perso{

	private int numSurvivant;
	private static int NB_SURVIVANT = 0;
	protected IPerso ts;
	
	public Survivant() {
		super(TYPES, x, y, v);
		// TODO Auto-generated constructor stub
		IPerso survivant = IPerso.SURVIVANT;
		this.ts = survivant;
		numSurvivant = ++NB_SURVIVANT;
	}
	
	public IPerso getTS() {
		return this.ts;
	}
	
	public int getNB() {
		return Survivant.NB_SURVIVANT;
	}
	
	public String toString() {
		return "Num_survivant :"+this.numSurvivant;
	}
	
	
}