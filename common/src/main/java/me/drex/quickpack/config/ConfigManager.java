package me.drex.quickpack.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class ConfigManager {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Logger LOGGER = LogManager.getLogger();

    public static Config config = new Config();

    public static void load(Path configDirectory) {
        Path configFile = configDirectory.resolve("quick-pack.json");
        if (Files.exists(configFile)) {
            try {
                String data = Files.readString(configFile);
                try {
                    config = GSON.fromJson(data, Config.class);
                } catch (JsonSyntaxException e) {
                    LOGGER.error("Failed to parse quick-pack config", e);
                }
            } catch (IOException e) {
                LOGGER.error("Failed to load quick-pack config", e);
            }
        } else {
            try {
                Files.writeString(configFile, GSON.toJson(config));
            } catch (IOException e) {
                LOGGER.error("Failed to save quick-pack config", e);
            }
        }
    }
}
