package ssixprojet.common.config;

public class PlayerScore {
	public int infectionSortId;
	public int survivorSortId;
	public int damageGiven = 0;
	public int damageTaken = 0;
	public int death = 0;
	public int kills = 0;
	public int infections = 0;
	public int timeAlive = 0;

	public int compareToInfected(PlayerScore score) {
		// compare infections
		if (infections != score.infections)
			return score.infections - infections;

		// compare death
		return score.death - death;
	}

	public int compareToSurvivor(PlayerScore score) {
		// compare time alive
		if (timeAlive != score.timeAlive)
			return score.timeAlive - timeAlive;

		// compare kills
		if (kills != score.kills)
			return score.kills - kills;

		// compare damageGiven
		return score.damageGiven - damageGiven;
	}

}
