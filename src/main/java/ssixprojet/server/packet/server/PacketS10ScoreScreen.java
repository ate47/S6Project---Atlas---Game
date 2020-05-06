package ssixprojet.server.packet.server;

import io.netty.buffer.ByteBuf;
import ssixprojet.common.config.PlayerScore;
import ssixprojet.common.entity.Player;
import ssixprojet.server.packet.PacketServer;

public class PacketS10ScoreScreen extends PacketServer {
	private int maxPlayer;
	private Player[] infectedScore, survivorScore;

	public PacketS10ScoreScreen(int maxPlayer, Player[] infectedScore, Player[] survivorScore) {
		super(0x10, 4/* maxPlayer */ + maxPlayer * 4 * 8 /* id, sortid, scores(6) */);

		if (maxPlayer > infectedScore.length || maxPlayer > survivorScore.length)
			throw new IllegalArgumentException(
					maxPlayer + " >= infectedScore.length || " + maxPlayer + " >= survivorScore.length");

		this.maxPlayer = maxPlayer;
		this.infectedScore = infectedScore;
		this.survivorScore = survivorScore;
	}

	@Override
	public void write(ByteBuf buf) {
		buf.writeInt(maxPlayer);

		// infectedScore
		for (int i = 0; i < maxPlayer; i++) {
			Player p = infectedScore[i];
			PlayerScore score = p.score;

			buf.writeInt(p.getId());
			buf.writeInt(score.infectionSortId);
			buf.writeInt(score.damageGiven);
			buf.writeInt(score.damageTaken);
			buf.writeInt(score.death);
			buf.writeInt(score.infections);
			buf.writeInt(score.kills);
			buf.writeInt(score.timeAlive);
		}

		// survivorScore
		for (int i = 0; i < maxPlayer; i++) {
			Player p = survivorScore[i];
			PlayerScore score = p.score;

			buf.writeInt(p.getId());
			buf.writeInt(score.survivorSortId);
			buf.writeInt(score.damageGiven);
			buf.writeInt(score.damageTaken);
			buf.writeInt(score.death);
			buf.writeInt(score.infections);
			buf.writeInt(score.kills);
			buf.writeInt(score.timeAlive);
		}
	}

}
