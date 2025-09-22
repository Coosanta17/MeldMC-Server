package net.coosanta.meldmc.mod;

import net.coosanta.meldmc.mod.modsinfo.MeldData;
import net.minecraft.SharedConstants;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;

@Mod(MeldMC.MOD_ID)
public class MeldMCForge {
    public MeldMCForge() {
        #if MC_VER <= MC_1_21_5
        String mcVersion = SharedConstants.getCurrentVersion().getName();
        #else
        String mcVersion = SharedConstants.getCurrentVersion().name();
        #endif
        String forgeVersion = ModList.get()
                .getModContainerById("forge")
                .map(container -> container.getModInfo().getVersion().toString())
                .orElseThrow(() -> new IllegalStateException("Forge version not found"));
        MeldMC.init(mcVersion, MeldData.ModLoader.FORGE, forgeVersion);
    }
}
