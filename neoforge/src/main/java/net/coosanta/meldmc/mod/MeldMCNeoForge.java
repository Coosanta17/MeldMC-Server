package net.coosanta.meldmc.mod;

import net.coosanta.meldmc.mod.modsinfo.MeldData;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLLoader;

@Mod(MeldMC.MOD_ID)
public class MeldMCNeoForge {
    public MeldMCNeoForge() {
        String mcVersion = FMLLoader.versionInfo().mcVersion();
        String loaderVersion = FMLLoader.versionInfo().neoForgeVersion();
        MeldMC.init(mcVersion, MeldData.ModLoader.NEOFORGE, loaderVersion);
    }
}
