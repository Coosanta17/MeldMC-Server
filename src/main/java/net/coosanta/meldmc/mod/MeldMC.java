package net.coosanta.meldmc.mod;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Paths;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(MeldMC.MODID)
public class MeldMC {
    public static final String MODID = "meldmc";
    private static final Logger LOGGER = LoggerFactory.getLogger(MeldMC.class);

    private final ClientModScanner clientModScanner;

    public MeldMC(FMLJavaModLoadingContext context) {
        // Register ourselves for server and other game events we are interested in
        MinecraftForge.EVENT_BUS.register(this);

        // Register our mod's ForgeConfigSpec so that Forge can create and load the config file for us
        context.registerConfig(ModConfig.Type.COMMON, Config.SPEC);

        // Initialize the client mod scanner
        clientModScanner = new ClientModScanner();

        LOGGER.info("Initialised MeldMC");
    }

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        LOGGER.info("MeldMC Starting...");

        LOGGER.info("Starting client mod scanning process");
        clientModScanner.scanClientMods(Paths.get("client-mods"));
        LOGGER.info("Client mod scanning complete");

        LOGGER.info("MeldMC Started!");
    }
}
