package net.coosanta.meldmc.mod;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.coosanta.meldmc.mod.modlist.ClientMod;
import net.coosanta.meldmc.mod.modlist.ClientModScanner;
import net.coosanta.meldmc.mod.network.MeldPacketHandler;
import net.coosanta.meldmc.mod.network.packets.ServerboundModlistRequestPacket;
import net.minecraft.client.multiplayer.ServerStatusPinger;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.network.NetworkEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Paths;
import java.util.Map;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(MeldMC.MODID)
public class MeldMC {
    public static final String MODID = "meldmc";
    private static final Logger LOGGER = LoggerFactory.getLogger(MeldMC.class);

    private final ClientModScanner clientModScanner;
    private static Map<String, ClientMod> modlistMap;

    public MeldMC(FMLJavaModLoadingContext context) {
        context.getModEventBus().register(MeldMC.class);
        MinecraftForge.EVENT_BUS.register(this);

        context.registerConfig(ModConfig.Type.COMMON, Config.SPEC);

        clientModScanner = new ClientModScanner();
    }

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        LOGGER.info("MeldMC Starting...");

        LOGGER.info("Starting client mod scanning process");
        modlistMap = clientModScanner.scanClientMods(Paths.get("client-mods"));
        LOGGER.info("Client mod scanning complete");

        LOGGER.info("MeldMC Started!");
    }

    @SubscribeEvent
    public static void commonSetup(FMLCommonSetupEvent event) {
        LOGGER.debug("Meld Common Setup called");
        event.enqueueWork(MeldPacketHandler::register);
    }

    @SubscribeEvent
    public void onCustomPayload(NetworkEvent.ServerCustomPayloadEvent event) {
        if (event.getPayload().readResourceLocation().equals(ResourceLocation.fromNamespaceAndPath("meldmc", "main"))) {
            MeldPacketHandler.handleCustomPayload(event.getPayload());
        }
    }

    public static Map<String, ClientMod> getModlistMap() {
        return modlistMap;
    }
}
