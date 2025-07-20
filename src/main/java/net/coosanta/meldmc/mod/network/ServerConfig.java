package net.coosanta.meldmc.mod.network;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public record ServerConfig(String address, int port, Path filesDirectory, boolean useHttps, boolean autoSsl,
                           String keyStorePath, String keyStorePassword, String keyStoreType, String trustStorePath,
                           String trustStorePassword, String trustStoreType) {
    public static final Logger LOGGER = LoggerFactory.getLogger(ServerConfig.class);

    public ServerConfig {
        if (useHttps && !autoSsl) {
            validateManualSSLConfig();
        }

        if (autoSsl && hasManualSslSettings()) {
            LOGGER.info("Ignoring manual SSL settings because 'autoSsl' is true.");
        }
    }

    private boolean hasManualSslSettings() {
        return isNotBlank(keyStorePath()) || isNotBlank(keyStorePassword()) ||
                isNotBlank(keyStoreType()) || isNotBlank(trustStorePath()) ||
                isNotBlank(trustStorePassword()) || isNotBlank(trustStoreType());
    }

    private boolean isNotBlank(String str) {
        return str != null && !str.trim().isEmpty();
    }

    private void validateManualSSLConfig() {
        if (keyStorePath == null || keyStorePath.trim().isEmpty()) {
            throw new IllegalArgumentException("keyStorePath is required when autoSsl is false");
        }
        if (!Files.exists(Paths.get(keyStorePath))) {
            throw new IllegalArgumentException("KeyStore file does not exist: " + keyStorePath);
        }
    }

    public boolean hasKeyStorePassword() {
        return keyStorePassword != null && !keyStorePassword.trim().isEmpty();
    }

    public boolean hasTrustStorePassword() {
        return trustStorePassword != null && !trustStorePassword.trim().isEmpty();
    }
}
