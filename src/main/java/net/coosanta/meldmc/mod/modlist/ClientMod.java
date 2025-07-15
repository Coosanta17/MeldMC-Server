package net.coosanta.meldmc.mod.modlist;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.Optional;

public class ClientMod {
    private final String hash;
    private String url;
    private ModSource source;
    private String fileName;

    public static final Codec<ModSource> MOD_SOURCE_CODEC = Codec.STRING.xmap(
            s -> {
                try {
                    return ModSource.valueOf(s);
                } catch (IllegalArgumentException e) {
                    return ModSource.UNKNOWN;
                }
            },
            Enum::name
    );

    public static final Codec<ClientMod> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.STRING.fieldOf("hash").forGetter(ClientMod::getHash),
                    Codec.STRING.optionalFieldOf("url").forGetter(mod -> Optional.ofNullable(mod.getUrl())),
                    MOD_SOURCE_CODEC.optionalFieldOf("source", ModSource.LOCAL).forGetter(ClientMod::getSource),
                    Codec.STRING.optionalFieldOf("fileName").forGetter(mod -> Optional.ofNullable(mod.getFileName()))
            ).apply(instance, (hash, urlOpt, source, fileNameOpt) ->
                    new ClientMod(hash, source, urlOpt.orElse(null), fileNameOpt.orElse(null))
            )
    );

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

    public ClientMod(String hash, ModSource source, String url, String fileName) {
        this.hash = hash;
        this.source = source;
        this.url = url;
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
