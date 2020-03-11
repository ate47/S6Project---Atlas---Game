package ssixprojet.common.world;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import ssixprojet.common.entity.Entity;

@Getter
public class Chunk {
	private List<Entity> entityList = new ArrayList<>();

	public Chunk() {}
}
