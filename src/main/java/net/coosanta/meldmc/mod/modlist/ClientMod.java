package net.coosanta.meldmc.mod.modlist;

import org.jetbrains.annotations.NotNull;

public class ClientMod {
    private final String hash;
    private String url;
    private @NotNull String filename;

    public ClientMod(String hash, @NotNull String filename) {
        this.hash = hash;
        this.url = null;
        this.filename = filename;
    }

    public ClientMod(String hash, String url, @NotNull String filename) {
        this.hash = hash;
        this.url = url;
        this.filename = filename;
    }

    public String getHash() {
        return hash;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public @NotNull String getFilename() {
        return filename;
    }

    public void setFilename(@NotNull String filename) {
        this.filename = filename;
    }
}
