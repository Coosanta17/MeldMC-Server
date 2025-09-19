package net.coosanta.meldmc.mod.modsinfo;

import java.util.Map;

public record MeldData(String mcVersion, ModLoader modLoader, String modLoaderVersion,
                       String versionId, Map<String, ClientMod> modMap) {
    public enum ModLoader {
        FABRIC,
        FORGE,
        NEOFORGE
    }
}