package net.coosanta.meldmc.mod;

import net.coosanta.meldmc.mod.modlist.ClientMod;
import net.coosanta.meldmc.mod.modlist.ClientModScanner;
import net.coosanta.meldmc.mod.modlist.MeldData;
import net.coosanta.meldmc.mod.network.MeldServer;
import net.coosanta.meldmc.mod.network.ServerFactory;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.server.ServerAboutToStartEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLLoader;
import net.minecraftforge.versions.forge.ForgeVersion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

@Mod(MeldMC.MODID)
public class MeldMC {
    public static final String MODID = "meldmc";
    public static final Logger LOGGER = LoggerFactory.getLogger(MODID);

    private static MeldData meldData;
    private static MeldServer meldServer;

    public MeldMC(FMLJavaModLoadingContext context) {
        context.getModEventBus().register(MeldMC.class);
        MinecraftForge.EVENT_BUS.register(this);

        context.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }

    @SubscribeEvent
    public void onServerAboutToStart(ServerAboutToStartEvent event) {
        LOGGER.info("MeldMC Starting...");

        LOGGER.info("Starting client mod scanning process");
        Map<String, ClientMod> modlistMap = ClientModScanner.scanClientMods(Config.serverConfig.filesDirectory());
        LOGGER.info("Client mod scanning complete");

        String mcVersion = FMLLoader.versionInfo().mcVersion();
        String forgeVersion = ForgeVersion.getVersion();
        meldData = new MeldData(mcVersion, MeldData.ModLoader.FORGE, forgeVersion, mcVersion + "-forge-" + forgeVersion, modlistMap);

        try {
            meldServer = ServerFactory.createServer(Config.serverConfig);
            meldServer.start();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        LOGGER.info("MeldMC Started!");
    }

    @SubscribeEvent
    public void onServerStopping(ServerStoppingEvent event) {
        LOGGER.debug("Stopping MeldMC...");
        meldServer.stop();
        LOGGER.debug("MeldMC stopped.");
    }

    public static MeldData getMeldData() {
        return meldData;
    }
}
