package ssixprojet.server.command;

import com.mojang.brigadier.LiteralMessage;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;

import ssixprojet.common.entity.Player;
import ssixprojet.server.AtlasGame;

public class PlayerArgumentType implements ArgumentType<Player> {
	private static final DynamicCommandExceptionType BAD_NAME = new DynamicCommandExceptionType(
			playerId -> new LiteralMessage("Bad player Id " + playerId));

	public static Player getPlayer(CommandContext<?> c, String name) {
		return c.getArgument(name, Player.class);
	}

	public static PlayerArgumentType player() {
		return new PlayerArgumentType();
	}

	private PlayerArgumentType() {}

	@Override
	public Player parse(StringReader reader) throws CommandSyntaxException {
		final int start = reader.getCursor();
		final int id = reader.readInt();
		if (id < 1) {
			reader.setCursor(start);
			throw CommandSyntaxException.BUILT_IN_EXCEPTIONS.integerTooLow().createWithContext(reader, id, 1);
		}

		Player result = AtlasGame.getAtlas().getWebServer().getConnectionManager().getPlayerInternalMap().get(id);

		if (result == null) {
			reader.setCursor(start);

			throw BAD_NAME.createWithContext(reader, id);
		}

		return result;
	}

}
