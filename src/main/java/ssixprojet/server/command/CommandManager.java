package ssixprojet.server.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import ssixprojet.common.GamePhase;
import ssixprojet.server.AtlasGame;

public class CommandManager {
	private final CommandDispatcher<AtlasGame> dispatcher = new CommandDispatcher<>();
	private final AtlasGame atlas;

	public CommandManager(AtlasGame atlas) {
		this.atlas = atlas;

		registerCommand("help", bld -> bld.executes(c -> {
			System.out.println("Liste des commandes:");
			dispatcher.getSmartUsage(dispatcher.getRoot(), c.getSource())
					.forEach((node, cmd) -> System.out.println("- " + cmd));
			return 0;
		}));

		registerCommand("list", bld -> bld.executes(c -> {
			System.out.println(
					"Joueurs (" + c.getSource().getWebServer().getConnectionManager().getPlayerMap().size() + ")");
			c.getSource().getWebServer().getConnectionManager().getPlayerInternalMap().entrySet().stream()
					.map(e -> "- id: " + e.getKey() + ", nom: " + e.getValue().getUsername() + ", type: "
							+ e.getValue().getType().name() + ", ip: " + e.getValue().getIp())
					.forEach(System.out::println);
			System.out.println("Ecran(s) (" + c.getSource().getScreens().size() + ")");
			c.getSource().getScreens().entrySet().stream().map(e -> "- id: " + e.getKey()).forEach(System.out::println);
			System.out.println(
					"Maitre(s) (" + c.getSource().getWebServer().getConnectionManager().getMasters().size() + ")");
			c.getSource().getWebServer().getConnectionManager().getMasters().entrySet().stream()
					.map(e -> "- id: " + e.getKey()).forEach(System.out::println);

			return 0;
		}));

		registerCommand("kick", new CommandKick());

		registerCommand("type", new CommandSetType());

		registerCommand("infect",
				command -> command.then(argument("percentage", IntegerArgumentType.integer(0, 100)).executes(c -> {
					c.getSource().randomInfection(IntegerArgumentType.getInteger(c, "percentage"));
					return 0;
				})));

		registerCommand("phase", command -> command
				.then(argument("gamephase", EnumArgumentType.enumValue(GamePhase.class)).executes(c -> {
					GamePhase phase = c.getArgument("gamephase", GamePhase.class);
					c.getSource().setPhase(phase);
					System.out.println("New phase: " + phase);
					return 0;
				})).executes(c -> {
					System.out.println("Phase: " + c.getSource().getPhase().name());
					System.out.println("Liste des phases:");
					for (GamePhase t : GamePhase.values())
						System.out.println("- " + t.name());
					return 0;
				}));

		registerCommand("restart", command -> command.executes(c -> {
			System.out.println("Relancement...");
			c.getSource().restart();
			return 0;
		}));
	}

	public static <T> RequiredArgumentBuilder<AtlasGame, T> argument(String name, ArgumentType<T> type) {
		return RequiredArgumentBuilder.<AtlasGame, T>argument(name, type);
	}

	public CommandDispatcher<AtlasGame> getDispatcher() {
		return dispatcher;
	}

	/**
	 * execute a command
	 * 
	 * @param cmd
	 *            the command to execute
	 */
	public void executeCommand(String cmd) {
		atlas.getGameServer().registerAction(() -> {
			try {
				dispatcher.execute(cmd, atlas);
			} catch (RuntimeException e) {
				e.printStackTrace();
			} catch (CommandSyntaxException e) {
				System.err.println(e.getMessage());
			}
		});
	}

	public void registerCommand(String name, Command applier) {
		LiteralArgumentBuilder<AtlasGame> cmd = LiteralArgumentBuilder.literal(name);
		applier.register(cmd);
		dispatcher.register(cmd);
	}
}
