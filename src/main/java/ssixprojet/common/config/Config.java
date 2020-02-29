package ssixprojet.common.config;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class Config {
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
	 * luck of the spawn of a crate
	 */
	private double spawnCrateLuck = 1.0 / (5 * getTickRate()); // every 5 second
	/**
	 * speed added to the infected
	 */
	private int speedAccelerationPercentage = 110;
	/**
	 * start ammo for each players
	 */
	private int startAmmo = 90;
	/**
	 * tick rate of the server (Hz)
	 */
	private int tickRate = 20;
	/**
	 * time before the end of the game in tick
	 */
	private int timeInTickBeforeEnd = tickRate * 60 * 4; // 4min
	/**
	 * time before the end of the game in tick
	 */
	private int timeInTickBeforeInfection = tickRate * 30; // 30s

}
