package ssixprojet.server;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import org.apache.commons.io.Charsets;

import ssixprojet.server.command.CommandManager;

public class CommandHandler extends Thread {
	private CommandManager manager;

	public CommandHandler(AtlasGame game) {
		super("CommandHandler");
		this.manager = game.getCommandManager();
	}

	@Override
	public void run() {
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(System.in, Charsets.UTF_8))) {
			String line;

			while ((line = reader.readLine()) != null)
				manager.executeCommand(line);

		} catch (Exception e) {}
	}
}
