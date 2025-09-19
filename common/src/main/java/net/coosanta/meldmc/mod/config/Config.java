package net.coosanta.meldmc.mod.config;

import net.coosanta.meldmc.mod.network.ServerConfig;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Standalone YAML-backed config loader.
 * <p>
 * Usage: call Config.load() early during mod initialization (for example, in your mod main class constructor).
 */
public final class Config {
    // Default location for the YAML file
    public static final Path CONFIG_PATH = Paths.get("config", "meldmc", "config.yml");

    public static ServerConfig serverConfig;

    private Config() {
    }

    /**
     * Loads the YAML config into serverConfig. If the config file doesn't exist it will be created with defaults.
     * <p>
     * This method will throw a RuntimeException if there is an I/O error while reading or writing the config file.
     */
    public static void load() {
        try {
            if (Files.notExists(CONFIG_PATH)) {
                // Ensure parent directories exist
                Path parent = CONFIG_PATH.getParent();
                if (parent != null && Files.notExists(parent)) {
                    Files.createDirectories(parent);
                }
                // Write defaults
                ConfigData defaults = new ConfigData();
                writeYaml(defaults, CONFIG_PATH);
                serverConfig = defaults.toServerConfig();
                return;
            }

            try (InputStream in = Files.newInputStream(CONFIG_PATH)) {
                Yaml yaml = new Yaml(new Constructor(ConfigData.class, new LoaderOptions()));
                ConfigData data = yaml.loadAs(in, ConfigData.class);
                if (data == null) data = new ConfigData();
                serverConfig = data.toServerConfig();
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to load MeldMC config: " + CONFIG_PATH, e);
        }
    }

    private static void writeYaml(ConfigData data, Path path) throws IOException {
        DumperOptions options = new DumperOptions();
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        options.setPrettyFlow(true);
        options.setIndent(2);
        Yaml yaml = new Yaml(options);
        try (Writer w = new OutputStreamWriter(Files.newOutputStream(path), StandardCharsets.UTF_8)) {
            yaml.dump(data, w);
        }
    }
}
