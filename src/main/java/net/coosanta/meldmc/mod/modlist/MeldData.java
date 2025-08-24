package net.coosanta.meldmc.mod.modlist;

import java.util.Map;

public record MeldData(String mcVersion, ModLoader modLoader, String modLoaderVersion,
                       String versionId, Map<String, ClientMod> modMap) {
    public enum ModLoader {
        VANILLA,
        FABRIC,
        QUILT,
        FORGE,
        NEOFORGE
    }
}
