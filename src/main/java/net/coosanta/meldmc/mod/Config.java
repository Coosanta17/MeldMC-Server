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

    // TODO: Use better config builder.

    private static final ForgeConfigSpec.ConfigValue<String> ADDRESS = BUILDER
            .comment(" MeldMC Config",
                    "",
                    " The address Meld clients will query for Meld mods data.",
                    " Set to a custom address for proxies or custom domain names")
            .define("address", "0.0.0.0");

    private static final ForgeConfigSpec.IntValue PORT = BUILDER
            .comment("",
                    " The port Meld will listen on.",
                    " This is to send mod information and mods to the client.",
                    " Make sure to open the port on your firewall and router.")
            .defineInRange("port", 8080, 0, 65535);

    private static final ForgeConfigSpec.IntValue QUERY_PORT = BUILDER
            .comment("",
                    " The port clients will query for Meld mods data.",
                    " Set for proxies. Set to 0 if it is the same as the Meld listening port.")
            .defineInRange("queryPort", 0, 0, 65535);

    private static final ForgeConfigSpec.ConfigValue<String> FILES_DIRECTORY = BUILDER
            .comment("",
                    " Directory where client mod files are stored.",
                    " The contents of this directory will be sent to the client as the mods to use when joining this server.")
            .define("filesDirectory", "./client-mods");

    private static final ForgeConfigSpec.BooleanValue USE_HTTPS = BUILDER
            .comment("",
                    " If the server should use HTTPS for secure connections.",
                    " It is highly recommended to secure data from being manipulated.",
                    " only disable if you know what you are doing!")
            .define("useHttps", true);

    private static final ForgeConfigSpec.BooleanValue AUTO_SSL = BUILDER
            .comment("",
                    " If the server will automatically set up SSL for HTTPS connections.",
                    " Make false for custom certificates.")
            .define("autoSsl", true);


    // TODO: test custom SSL
    private static final ForgeConfigSpec.ConfigValue<String> KEYSTORE_PATH = BUILDER
            .comment("",
                    "## The following config only applies if autoSsl is DISABLED ###",
                    "",
                    " Path to the SSL keystore file for HTTPS connections.")
            .define("keyStorePath", "config/meldmc/keystore.jks");

    private static final ForgeConfigSpec.ConfigValue<String> KEYSTORE_PASSWORD = BUILDER
            .comment("",
                    " Password for the SSL keystore (if any)")
            .define("keyStorePassword", "");

    private static final ForgeConfigSpec.ConfigValue<String> KEYSTORE_TYPE = BUILDER
            .comment("",
                    " Type of keystore format")
            .define("keyStoreType", "PKCS12");

    private static final ForgeConfigSpec.ConfigValue<String> TRUSTSTORE_PATH = BUILDER
            .comment("",
                    " Path to the SSL truststore file")
            .define("trustStorePath", "config/meldmc/truststore.jks");

    private static final ForgeConfigSpec.ConfigValue<String> TRUSTSTORE_PASSWORD = BUILDER
            .comment("",
                    " Password for the SSL truststore (if any)")
            .define("trustStorePassword", "");

    private static final ForgeConfigSpec.ConfigValue<String> TRUSTSTORE_TYPE = BUILDER
            .comment("",
                    " Type of truststore format")
            .define("trustStoreType", "PKCS12");

    private static final ForgeConfigSpec.BooleanValue SELF_SIGNED = BUILDER
            .comment("",
                    " If the SSL certificate is Self-Signed.")
            .define("selfSigned", true);


    static final ForgeConfigSpec SPEC = BUILDER.build();

    public static ServerConfig serverConfig;

    @SubscribeEvent
    static void onLoad(final ModConfigEvent event) {
        serverConfig = new ServerConfig(
                ADDRESS.get(),
                PORT.get(),
                QUERY_PORT.get(),
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
