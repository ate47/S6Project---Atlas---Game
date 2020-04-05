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
	private double spawnCrateLuck = 1.0 / (5 * tickRate); // every 5 second
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
	public Config() {}
	public int getChunkSplit() {
		return chunkSplit;
	}
	public int getInitialInfectionPercentage() {
		return initialInfectionPercentage;
	}
	public String getPasswordMaster() {
		return passwordMaster;
	}
	public int getPort() {
		return port;
	}
	public double getSpawnCrateLuck() {
		return spawnCrateLuck;
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

	public void setBufferiseFile(boolean bufferiseFile) {
		this.bufferiseFile = bufferiseFile;
	}
	public void setChunkSplit(int chunkSplit) {
		this.chunkSplit = chunkSplit;
	}
	public void setInitialInfectionPercentage(int initialInfectionPercentage) {
		this.initialInfectionPercentage = initialInfectionPercentage;
	}
	public void setPasswordMaster(String passwordMaster) {
		this.passwordMaster = passwordMaster;
	}
	public void setPort(int port) {
		this.port = port;
	}
	public void setSpawnCrateLuck(double spawnCrateLuck) {
		this.spawnCrateLuck = spawnCrateLuck;
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
