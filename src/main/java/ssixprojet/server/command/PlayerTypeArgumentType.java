package ssixprojet.server.command;

import com.mojang.brigadier.LiteralMessage;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;

import ssixprojet.common.entity.PlayerType;

public class PlayerTypeArgumentType implements ArgumentType<PlayerType> {
	private static final DynamicCommandExceptionType BAD_TYPE = new DynamicCommandExceptionType(
			type -> new LiteralMessage("Bad player type " + type));

	public static PlayerType getPlayerType(CommandContext<?> c, String name) {
		return c.getArgument(name, PlayerType.class);
	}

	public static PlayerTypeArgumentType playerType() {
		return new PlayerTypeArgumentType();
	}

	private PlayerTypeArgumentType() {}

	@Override
	public PlayerType parse(StringReader reader) throws CommandSyntaxException {
		String playerType = reader.readString();

		try {
			return PlayerType.valueOf(playerType);
		} catch (IllegalArgumentException e) {
			throw BAD_TYPE.createWithContext(reader, playerType);
		}
	}
}
