package net.coosanta.meldmc.mod;

import net.coosanta.meldmc.mod.network.ServerConfig;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.config.ModConfigEvent;

import java.nio.file.Paths;

@Mod.EventBusSubscriber(modid = MeldMC.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class Config {
    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();

    // TODO: Use a better config builder.

    private static final ForgeConfigSpec.IntValue PORT = BUILDER
            .comment("",
                    " The port Meld will bind to and listen on.",
                    " This is to send mod information and mods to the client.",
                    " Make sure to open the port on your firewall and router.")
            .defineInRange("bind-port", 8080, 0, 65535);

    private static final ForgeConfigSpec.ConfigValue<String> QUERY_ADDRESS = BUILDER
            .comment("",
                    " The address and port clients will query for Meld mods data.",
                    " Useful for proxies or custom domains. Set to 0.0.0.0:0 for Minecraft server address and Meld port.",
                    " You can set this to any web address. omit the protocol to use default value.",
                    " Examples:",
                    " server.com:0 -> clients query server.com with Meld bind port.",
                    " 0.0.0.0:1234 -> clients query server address with a port of 1234",
                    " https://server.com:9876/meld/ -> clients query server.com at port 9876 with resource path prefix '/meld/' using HTTPS protocol.")
            .define("query-address", "0.0.0.0:0");

    private static final ForgeConfigSpec.ConfigValue<String> FILES_DIRECTORY = BUILDER
            .comment("",
                    " Directory where client mod files are stored.",
                    " The contents of this directory will be sent to the client as the mods to use when joining this server.")
            .define("files-directory", "./client-mods");

    private static final ForgeConfigSpec.BooleanValue USE_HTTPS = BUILDER
            .comment("",
                    " If the server should use HTTPS for secure connections.",
                    " It is highly recommended to secure data from being manipulated.",
                    " only disable if you know what you are doing!")
            .define("use-https", true);

    private static final ForgeConfigSpec.BooleanValue AUTO_SSL = BUILDER
            .comment("",
                    " If the server will automatically set up SSL for HTTPS connections.",
                    " Make false for custom certificates.")
            .define("auto-ssl", true);


    // TODO: test custom SSL
    private static final ForgeConfigSpec.ConfigValue<String> KEYSTORE_PATH = BUILDER
            .comment("",
                    "## The following config only applies if autoSsl is DISABLED ###",
                    "",
                    " Path to the SSL keystore file for HTTPS connections.")
            .define("keystore-path", "config/meldmc/keystore.jks");

    private static final ForgeConfigSpec.ConfigValue<String> KEYSTORE_PASSWORD = BUILDER
            .comment("",
                    " Password for the SSL keystore (if any)")
            .define("keystore-password", "");

    private static final ForgeConfigSpec.ConfigValue<String> KEYSTORE_TYPE = BUILDER
            .comment("",
                    " Type of keystore format")
            .define("keystore-type", "PKCS12");

    private static final ForgeConfigSpec.ConfigValue<String> TRUSTSTORE_PATH = BUILDER
            .comment("",
                    " Path to the SSL truststore file")
            .define("truststore-path", "config/meldmc/truststore.jks");

    private static final ForgeConfigSpec.ConfigValue<String> TRUSTSTORE_PASSWORD = BUILDER
            .comment("",
                    " Password for the SSL truststore (if any)")
            .define("truststore-password", "");

    private static final ForgeConfigSpec.ConfigValue<String> TRUSTSTORE_TYPE = BUILDER
            .comment("",
                    " Type of truststore format")
            .define("truststore-type", "PKCS12");

    private static final ForgeConfigSpec.BooleanValue SELF_SIGNED = BUILDER
            .comment("",
                    " If the SSL certificate is Self-Signed.")
            .define("self-signed", true);


    static final ForgeConfigSpec SPEC = BUILDER.build();

    public static ServerConfig serverConfig;

    @SubscribeEvent
    static void onLoad(final ModConfigEvent event) {
        serverConfig = new ServerConfig(
                PORT.get(),
                QUERY_ADDRESS.get(),
                Paths.get(FILES_DIRECTORY.get()),
                USE_HTTPS.get(),
                AUTO_SSL.get(),
                KEYSTORE_PATH.get(),
                KEYSTORE_PASSWORD.get(),
                KEYSTORE_TYPE.get(),
                TRUSTSTORE_PATH.get(),
                TRUSTSTORE_PASSWORD.get(),
                TRUSTSTORE_TYPE.get(),
                SELF_SIGNED.get()
        );
    }
}
