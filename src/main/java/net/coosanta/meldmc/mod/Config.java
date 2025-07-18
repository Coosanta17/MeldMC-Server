package net.coosanta.meldmc.mod;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.config.ModConfigEvent;

// An example config class. This is not required, but it's a good idea to have one to keep your config organized.
// Demonstrates how to use Forge's config APIs
// TODO: Make Config
@Mod.EventBusSubscriber(modid = MeldMC.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class Config {
    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();

    private static final ForgeConfigSpec.IntValue PORT = BUILDER
            .comment(" What port will the Meld webserver run on?\n This is to send mod information and mods to the client.\n Make sure to open the port on you firewall and router.")
            .defineInRange("port", 8080, 0, 65535);

    static final ForgeConfigSpec SPEC = BUILDER.build();

    public static int port;

    @SubscribeEvent
    static void onLoad(final ModConfigEvent event) {
        port = PORT.get();
    }
}
