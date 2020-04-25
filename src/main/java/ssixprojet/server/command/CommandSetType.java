package ssixprojet.server.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;

import ssixprojet.common.entity.Player;
import ssixprojet.common.entity.PlayerType;
import ssixprojet.server.AtlasGame;

public class CommandSetType implements Command {

	@Override
	public void register(LiteralArgumentBuilder<AtlasGame> command) {
		command.then(CommandManager.argument("player", PlayerArgumentType.player())
				.then(CommandManager.argument("type", EnumArgumentType.enumValue(PlayerType.class)).executes(c -> {
					PlayerType type = c.getArgument("type", PlayerType.class);
					Player p = PlayerArgumentType.getPlayer(c, "player");
					p.setType(type);

					System.out.println("[Player] " + p.getUsername() + " type : " + type.name());
					return 0;
				}))).executes(c -> {
					System.out.println("Liste des types:");
					for (PlayerType t : PlayerType.values())
						System.out.println("- " + t.name());
					return 0;
				});
	}

}
