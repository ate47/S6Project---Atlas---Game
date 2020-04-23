package ssixprojet.server.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;

import ssixprojet.server.AtlasGame;

@FunctionalInterface
public interface Command {
	void register(LiteralArgumentBuilder<AtlasGame> command);
}
