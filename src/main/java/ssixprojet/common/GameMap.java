package ssixprojet.common;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;

public class GameMap {
	private static final Gson GSON = new Gson();
	/**
	 * read the map from a Json file
	 * 
	 * @param file
	 *            the Json file
	 * @return a {@link GameMap} object represented in the file, or null if a error
	 *         occured
	 */
	public static GameMap readMap(File file) {
		try (Reader r = new FileReader(file)) {
			return GSON.fromJson(r, GameMap.class);
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}
	private List<MapEdge> edges = new ArrayList<>();
	private int playerSize;

	private List<SpawnLocation> spawnLocations = new ArrayList<>();

	private int width, height;

	private GameMap() {}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		GameMap other = (GameMap) obj;
		if (edges == null) {
			if (other.edges != null)
				return false;
		} else if (!edges.equals(other.edges))
			return false;
		if (height != other.height)
			return false;
		if (playerSize != other.playerSize)
			return false;
		if (spawnLocations == null) {
			if (other.spawnLocations != null)
				return false;
		} else if (!spawnLocations.equals(other.spawnLocations))
			return false;
		if (width != other.width)
			return false;
		return true;
	}

	public List<MapEdge> getEdges() {
		return edges;
	}

	public int getHeight() {
		return height;
	}

	public int getPlayerSize() {
		return playerSize;
	}

	public List<SpawnLocation> getSpawnLocations() {
		return spawnLocations;
	}

	public int getWidth() {
		return width;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((edges == null) ? 0 : edges.hashCode());
		result = prime * result + height;
		result = prime * result + playerSize;
		result = prime * result + ((spawnLocations == null) ? 0 : spawnLocations.hashCode());
		result = prime * result + width;
		return result;
	}

	@Override
	public String toString() {
		return "GameMap [edges=" + edges + ", spawnLocations=" + spawnLocations + ", width=" + width + ", height="
				+ height + ", playerSize=" + playerSize + "]";
	}
}
