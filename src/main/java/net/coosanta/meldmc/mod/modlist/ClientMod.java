package net.coosanta.meldmc.mod.modlist;

import org.jetbrains.annotations.NotNull;

public class ClientMod {
    private String modVersion;
    private final String hash;
    private String url;
    private @NotNull String filename;
    private String modname;
    private String modId;
    private String authors;
    private String description;

    public ClientMod(String modVersion, String hash, @NotNull String filename, String modname, String modId, String authors, String description) {
        this.modVersion = modVersion;
        this.hash = hash;
        this.url = null;
        this.filename = filename;
        this.modname = modname;
        this.modId = modId;
        this.authors = authors;
        this.description = description;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void setFilename(@NotNull String filename) {
        this.filename = filename;
    }

    public @NotNull String getFilename() {
        return filename;
    }

    public String getHash() {
        return hash;
    }

    public String getUrl() {
        return url;
    }

    public String getModVersion() {
        return modVersion;
    }

    public String getModname() {
        return modname;
    }

    public String getModId() {
        return modId;
    }

    public String getAuthors() {
        return authors;
    }

    public String getDescription() {
        return description;
    }
}
