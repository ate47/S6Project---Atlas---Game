package ssixprojet.server.command;

import com.mojang.brigadier.LiteralMessage;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;

public class EnumArgumentType<T extends Enum<T>> implements ArgumentType<T> {
	private final DynamicCommandExceptionType BAD_TYPE;
	private Class<T> cls;

	private EnumArgumentType(Class<T> cls) {
		this.cls = cls;

		BAD_TYPE = new DynamicCommandExceptionType(
				type -> new LiteralMessage("Bad " + cls.getSimpleName() + " " + type));
	}

	public static <T extends Enum<T>> EnumArgumentType<T> enumValue(Class<T> cls) {
		return new EnumArgumentType<T>(cls);
	}

	@Override
	public T parse(StringReader reader) throws CommandSyntaxException {
		String playerType = reader.readString();

		try {
			return Enum.valueOf(cls, playerType);
		} catch (IllegalArgumentException e) {
			throw BAD_TYPE.createWithContext(reader, playerType);
		}
	}

}
