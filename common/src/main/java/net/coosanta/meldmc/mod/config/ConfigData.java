package net.coosanta.meldmc.mod.config;

import net.coosanta.meldmc.mod.network.ServerConfig;

import java.nio.file.Paths;

/**
 * POJO representation of the YAML config. Fields are public to allow SnakeYAML to read/write them easily.
 */
public class ConfigData {
    // Server networking
    public int bind_port = 8080;
    public String query_address = "0.0.0.0:0";

    // Files
    public String files_directory = "./client-mods";

    // HTTPS/SSL
    public boolean use_https = true;
    public boolean auto_ssl = true;

    // Custom keystore/truststore (only used if auto_ssl is false / custom certs)
    public String keystore_path = "config/meldmc/keystore.jks";
    public String keystore_password = "";
    public String keystore_type = "PKCS12";

    public String truststore_path = "config/meldmc/truststore.jks";
    public String truststore_password = "";
    public String truststore_type = "PKCS12";

    public boolean self_signed = true;

    public ConfigData() {
    }

    public ServerConfig toServerConfig() {
        // Convert to the project's ServerConfig expected by the rest of the codebase
        return new ServerConfig(
                validPort(bind_port),
                query_address != null ? query_address : "0.0.0.0:0",
                Paths.get(files_directory != null ? files_directory : "./client-mods"),
                use_https,
                auto_ssl,
                keystore_path != null ? keystore_path : "config/meldmc/keystore.jks",
                keystore_password != null ? keystore_password : "",
                keystore_type != null ? keystore_type : "PKCS12",
                truststore_path != null ? truststore_path : "config/meldmc/truststore.jks",
                truststore_password != null ? truststore_password : "",
                truststore_type != null ? truststore_type : "PKCS12",
                self_signed
        );
    }

    private int validPort(int p) {
        if (p < 0 || p > 0xFFFF) return 8080;
        return p;
    }
}