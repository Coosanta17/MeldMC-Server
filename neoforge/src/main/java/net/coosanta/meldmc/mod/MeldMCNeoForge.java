package net.coosanta.meldmc.mod;

import net.coosanta.meldmc.mod.modsinfo.MeldData;
import net.minecraft.SharedConstants;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.Mod;

@Mod(MeldMC.MOD_ID)
public class MeldMCNeoForge {
    public MeldMCNeoForge() {
        #if MC_VER <= MC_1_21_5
        String mcVersion = SharedConstants.getCurrentVersion().getName();
        #else
        String mcVersion = SharedConstants.getCurrentVersion().name();
        #endif
        String forgeVersion = ModList.get()
                .getModContainerById("neoforge")
                .map(container -> container.getModInfo().getVersion().toString())
                .orElseThrow(() -> new IllegalStateException("NeoForge version not found"));
        MeldMC.init(mcVersion, MeldData.ModLoader.NEOFORGE, forgeVersion);
    }
}
