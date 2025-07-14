package net.coosanta.meldmc.mod.modlist;

public class ClientMod {
    private final String hash;
    private String url;
    private ModSource source;
    private String fileName;

    public ClientMod(String hash) {
        this.hash = hash;
        this.source = ModSource.LOCAL;
        this.url = null;
        this.fileName = null;
    }

    public ClientMod(String hash, String fileName) {
        this.hash = hash;
        this.source = ModSource.LOCAL;
        this.url = null;
        this.fileName = fileName;
    }

    public String getHash() {
        return hash;
    }

    public String getUrl() {
        return url;
    }

    public ModSource getSource() {
        return source;
    }

    public void setUrl(String url) {
        this.url = url;
        if (this.url != null && this.url.contains("modrinth.com")) {
            source = ModSource.MODRINTH;
        }
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public enum ModSource {
        MODRINTH,
        LOCAL,
        UNKNOWN
    }
}
