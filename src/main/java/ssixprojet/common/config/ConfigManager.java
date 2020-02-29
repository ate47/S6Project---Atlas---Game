package ssixprojet.common.config;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import javax.annotation.Nonnull;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ConfigManager {
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	@NonNull
	private final File configFile;
	private Config config;

	public @Nonnull Config getConfig() {
		if (config == null)
			sync();
		return config;
	}

	/**
	 * save the config into the config file
	 */
	public void save() {
		if (config != null)
			try (FileWriter w = new FileWriter(configFile)) {
				GSON.toJson(config, w);
			} catch (IOException e) {
				e.printStackTrace();
			}
	}

	/**
	 * load the config into the config file or create a new one and save it
	 */
	public void sync() {
		try (FileReader r = new FileReader(configFile)) {
			config = GSON.fromJson(r, Config.class);
		} catch (FileNotFoundException e) {
			config = new Config();
			save();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
}
