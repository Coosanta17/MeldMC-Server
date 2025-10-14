package net.coosanta.meldmc.mod.modsinfo;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ClientMod {
    private final String modVersion;
    private final String hash;
    private final String modname;
    private final String modId;
    private final String authors;
    private final String description;
    private final long fileSize; // Size in bytes
    private @Nullable String url;
    private @Nullable String projectUrl;
    private @Nullable String projectId;
    private @NotNull String filename;

    @JsonCreator
    public ClientMod(
            @JsonProperty("modVersion") String modVersion,
            @JsonProperty("hash") String hash,
            @JsonProperty("filename") @NotNull String filename,
            @JsonProperty("modname") String modname,
            @JsonProperty("modId") String modId,
            @JsonProperty("authors") String authors,
            @JsonProperty("description") String description,
            @JsonProperty("fileSize") long fileSize
    ) {
        this.modVersion = modVersion;
        this.hash = hash;
        this.url = null;
        this.filename = filename;
        this.modname = modname;
        this.modId = modId;
        this.authors = authors;
        this.description = description;
        this.fileSize = fileSize;
    }

    public @NotNull String getFilename() {
        return filename;
    }

    public void setFilename(@NotNull String filename) {
        this.filename = filename;
    }

    public String getHash() {
        return hash;
    }

    public @Nullable String getUrl() {
        return url;
    }

    public void setUrl(@Nullable String url) {
        this.url = url;
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

    public long getFileSize() {
        return fileSize;
    }

    public @Nullable String getProjectUrl() {
        return projectUrl;
    }

    public void setProjectUrl(@Nullable String projectUrl) {
        this.projectUrl = projectUrl;
    }

    public @Nullable String getProjectId() {
        return projectId;
    }

    public void setProjectId(@Nullable String projectId) {
        this.projectId = projectId;
    }
}