package ssixprojet.common.config;

public class Config {

	/**
	 * save the files at first load
	 */
	private boolean bufferiseFile = false;

	/**
	 * percentage of initial infected
	 */
	private int initialInfectionPercentage = 10;
	/**
	 * the password for the master phone
	 */
	private String passwordMaster = "ATAKE"; // Aurelien Theo Antoine Kawther Erwin
	/**
	 * web server port
	 */
	private int port = 2080;
	/**
	 * tick rate of the server (Hz)
	 */
	private int tickRate = 20;
	/**
	 * luck of the spawn of a crate
	 */
	private int spawnCrateTime = tickRate * 10; // every 5 second
	/**
	 * speed added to the infected
	 */
	private int speedAccelerationPercentage = 110;
	/**
	 * start ammo for each players
	 */
	private int startAmmo = 90;
	/**
	 * time before the end of the game in tick
	 */
	private int timeInTickBeforeEnd = tickRate * 60 * 4; // 4min
	/**
	 * time before the end of the game in tick
	 */
	private int timeInTickBeforeInfection = tickRate * 30; // 30s
	/**
	 * number of chunk inside a row (number total of chunks is chunkSplit *
	 * chunkSplit)
	 */
	private int chunkSplit = 20;

	/**
	 * time before a player can reshoot
	 */
	private long millisBeforeReshooting = 100;

	/**
	 * power of ammunition
	 */
	private int ammoPower = 21;

	/**
	 * ammos per crate
	 */
	private int crateAmmos = 30;

	public Config() {}

	public int getAmmoPower() {
		return ammoPower;
	}

	public int getChunkSplit() {
		return chunkSplit;
	}

	public int getCrateAmmos() {
		return crateAmmos;
	}

	public int getInitialInfectionPercentage() {
		return initialInfectionPercentage;
	}

	public long getMillisBeforeReshooting() {
		return millisBeforeReshooting;
	}

	public String getPasswordMaster() {
		return passwordMaster;
	}

	public int getPort() {
		return port;
	}

	public int getSpawnCrateTime() {
		return spawnCrateTime;
	}

	public int getSpeedAccelerationPercentage() {
		return speedAccelerationPercentage;
	}

	public int getStartAmmo() {
		return startAmmo;
	}

	public int getTickRate() {
		return tickRate;
	}

	public int getTimeInTickBeforeEnd() {
		return timeInTickBeforeEnd;
	}

	public int getTimeInTickBeforeInfection() {
		return timeInTickBeforeInfection;
	}

	public boolean isBufferiseFile() {
		return bufferiseFile;
	}

	public void setAmmoPower(int ammoPower) {
		this.ammoPower = ammoPower;
	}

	public void setBufferiseFile(boolean bufferiseFile) {
		this.bufferiseFile = bufferiseFile;
	}

	public void setChunkSplit(int chunkSplit) {
		this.chunkSplit = chunkSplit;
	}

	public void setCrateAmmos(int crateAmmos) {
		this.crateAmmos = crateAmmos;
	}

	public void setInitialInfectionPercentage(int initialInfectionPercentage) {
		this.initialInfectionPercentage = initialInfectionPercentage;
	}

	public void setMillisBeforeReshooting(long millisBeforeReshooting) {
		this.millisBeforeReshooting = millisBeforeReshooting;
	}

	public void setPasswordMaster(String passwordMaster) {
		this.passwordMaster = passwordMaster;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public void setSpawnCrateTime(int spawnCrateTime) {
		this.spawnCrateTime = spawnCrateTime;
	}

	public void setSpeedAccelerationPercentage(int speedAccelerationPercentage) {
		this.speedAccelerationPercentage = speedAccelerationPercentage;
	}

	public void setStartAmmo(int startAmmo) {
		this.startAmmo = startAmmo;
	}

	public void setTickRate(int tickRate) {
		this.tickRate = tickRate;
	}

	public void setTimeInTickBeforeEnd(int timeInTickBeforeEnd) {
		this.timeInTickBeforeEnd = timeInTickBeforeEnd;
	}

	public void setTimeInTickBeforeInfection(int timeInTickBeforeInfection) {
		this.timeInTickBeforeInfection = timeInTickBeforeInfection;
	}

}
