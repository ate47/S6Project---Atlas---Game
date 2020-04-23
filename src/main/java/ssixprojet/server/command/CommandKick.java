package ssixprojet.server.command;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;

import ssixprojet.server.AtlasGame;

public class CommandKick implements Command {

	@Override
	public void register(LiteralArgumentBuilder<AtlasGame> command) {
		command.then(
				// id of the player
				CommandManager.argument("player", PlayerArgumentType.player()).then(

						CommandManager.argument("reason", StringArgumentType.greedyString()).executes(c -> {
							PlayerArgumentType.getPlayer(c, "player").kick(StringArgumentType.getString(c, "reason"));
							return 0;
						})

				).executes(c -> {
					PlayerArgumentType.getPlayer(c, "player").kick("no reason");
					return 0;
				})

		);
	}

}
