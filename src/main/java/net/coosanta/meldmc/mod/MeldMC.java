package net.coosanta.meldmc.mod;

import net.coosanta.meldmc.mod.modlist.ClientMod;
import net.coosanta.meldmc.mod.modlist.ClientModScanner;
import net.coosanta.meldmc.mod.modlist.MeldData;
import net.coosanta.meldmc.mod.network.MeldServer;
import net.coosanta.meldmc.mod.network.ServerFactory;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLLoader;
import net.minecraftforge.versions.forge.ForgeVersion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Paths;
import java.util.Map;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(MeldMC.MODID)
public class MeldMC {
    public static final String MODID = "meldmc";
    private static final Logger LOGGER = LoggerFactory.getLogger(MeldMC.class);

    private static Map<String, ClientMod> modlistMap;
    private static MeldData meldData;
    private static MeldServer meldServer;

    public MeldMC(FMLJavaModLoadingContext context) {
        context.getModEventBus().register(MeldMC.class);
        MinecraftForge.EVENT_BUS.register(this);

        context.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        LOGGER.info("MeldMC Starting...");

        LOGGER.info("Starting client mod scanning process");
        modlistMap = ClientModScanner.scanClientMods(Paths.get("client-mods"));
        meldData = new MeldData(FMLLoader.versionInfo().mcVersion(), MeldData.ModLoader.FORGE, ForgeVersion.getVersion(), modlistMap);
        try {
            meldServer = ServerFactory.createServer(Config.serverConfig);
            meldServer.start();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        LOGGER.info("Client mod scanning complete");

        LOGGER.info("MeldMC Started!");
    }

    public static MeldData getMeldData() {
        return meldData;
    }
}
