package JEU;

import ssixprojet.common.PlayerType;

public class Perso {

	private PlayerType type;
	private boolean  statut; //mort ou vivant
	protected static int v; //tir ou pv
	protected static double x;
	protected static double y;
	private int num_perso; //id perso
	
	public Perso(PlayerType type, double x, double y, int v) {
		super();
		this.num_perso++;
		this.type = type;
		this.statut = true;
		Perso.x = x;
		Perso.y = y;
	}
	
	public double getX() {
		return Perso.x;
	}
	
	public double getY() {
		return Perso.y;
	}
	
	public PlayerType getType() {
		return this.type;
	}
	
	public void setX(double x) {
		Perso.x = x;
	}
	
	public void setY(double y) {
		Perso.y = y;
	}
	
	public void setType(PlayerType type) {
		this.type = type;
	}
	
	
	public boolean  getSTATUT() {
		return this.statut;
	}
	
	public void setSTATUT(boolean  s) {
		this.statut = s;
	}
	
	public int getV() {
		return Perso.v;
	}
	
	public void setV(int v) {
		Perso.v = v;
	}
	
	public int getID() {
		return this.num_perso;
	}
	
	
	//Survivant To Zombie
	public Perso SurvivantToZombie() {
		 Perso p;
		 double x = this.getX();
		 double y = this.getY();
		 p = new Zombie();
		 p.setX(x);
		 p.setY(y);
		 return p;
	}

	//ajout balles
	public void  ajout_balles(int b) {
		this.setV(b);
		
	}

}