package net.coosanta.meldmc.mod;

public class ClientMod {
    private final String hash;
    private String url;
    private ModSource source;

    public ClientMod(String hash) {
        this.hash = hash;
        this.source = ModSource.LOCAL;
        this.url = null;
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
        if (this.url.contains("modrinth.com")) {
            source = ModSource.MODRINTH;
        }
    }

    public enum ModSource {
        MODRINTH,
        LOCAL,
        UNKNOWN
    }
}
