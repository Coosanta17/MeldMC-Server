package net.coosanta.meldmc.mod.modlist;

import org.jetbrains.annotations.NotNull;

public class ClientMod {
    private final String hash;
    private String url;
    private @NotNull String fileName;

    public ClientMod(String hash, @NotNull String fileName) {
        this.hash = hash;
        this.url = null;
        this.fileName = fileName;
    }

    public ClientMod(String hash, String url, @NotNull String fileName) {
        this.hash = hash;
        this.url = url;
        this.fileName = fileName;
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

    public @NotNull String getFileName() {
        return fileName;
    }

    public void setFileName(@NotNull String fileName) {
        this.fileName = fileName;
    }
}
