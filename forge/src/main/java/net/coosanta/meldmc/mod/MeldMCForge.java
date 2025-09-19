package net.coosanta.meldmc.mod;

import net.coosanta.meldmc.mod.modsinfo.MeldData;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.loading.FMLLoader;

@Mod(MeldMC.MOD_ID)
public class MeldMCForge {
    public MeldMCForge() {
        String mcVersion = FMLLoader.versionInfo().mcVersion();
        String loaderVersion = FMLLoader.versionInfo().forgeVersion();
        MeldMC.init(mcVersion, MeldData.ModLoader.FORGE, loaderVersion);
    }
}
