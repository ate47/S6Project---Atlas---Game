package JEU;

public class Survivant extends Perso{


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
	
	
}