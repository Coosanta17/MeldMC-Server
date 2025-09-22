package net.coosanta.meldmc.mod;

import net.coosanta.meldmc.mod.modsinfo.MeldData;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;

public class MeldMCFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        String mcVersion = FabricLoader.getInstance().getModContainer("minecraft")
                .map(mod -> mod.getMetadata().getVersion().getFriendlyString())
                .orElseThrow(() -> new IllegalStateException("Minecraft version not found"));
        String loaderVersion = FabricLoader.getInstance().getModContainer("fabricloader")
                .map(mod -> mod.getMetadata().getVersion().getFriendlyString())
                .orElseThrow(() -> new IllegalStateException("Fabric version not found"));
        MeldMC.init(mcVersion, MeldData.ModLoader.FABRIC, loaderVersion);
    }
}
