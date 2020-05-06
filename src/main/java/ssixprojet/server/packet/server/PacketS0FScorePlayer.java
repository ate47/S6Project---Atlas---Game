package ssixprojet.server.packet.server;

import io.netty.buffer.ByteBuf;
import ssixprojet.common.config.PlayerScore;
import ssixprojet.server.packet.PacketServer;

public class PacketS0FScorePlayer extends PacketServer {
	private PlayerScore score;

	public PacketS0FScorePlayer(PlayerScore score) {
		super(0x0F, 8 * 4);
		this.score = score;
	}

	@Override
	public void write(ByteBuf buf) {
		buf.writeInt(score.survivorSortId);
		buf.writeInt(score.infectionSortId);
		buf.writeInt(score.damageGiven);
		buf.writeInt(score.damageTaken);
		buf.writeInt(score.death);
		buf.writeInt(score.infections);
		buf.writeInt(score.kills);
		buf.writeInt(score.timeAlive);
	}

}
