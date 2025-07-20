package net.coosanta.meldmc.mod;

import net.coosanta.meldmc.mod.network.ServerConfig;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.config.ModConfigEvent;

import java.nio.file.Path;
import java.nio.file.Paths;

@Mod.EventBusSubscriber(modid = MeldMC.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class Config {
    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();

    // TODO: Use better config builder.

    private static final ForgeConfigSpec.ConfigValue<String> ADDRESS = BUILDER
            .comment(" MeldMC Config",
                    "",
                    " The address Meld clients will query for mods.",
                    " Set to a custom address for proxies or custom domain names")
            .define("address", "0.0.0.0");

    private static final ForgeConfigSpec.IntValue PORT = BUILDER
            .comment("",
                    " The port Meld will listen on.",
                    " This is to send mod information and mods to the client.",
                    " Make sure to open the port on your firewall and router.")
            .defineInRange("port", 8080, 0, 65535);

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

    static final ForgeConfigSpec SPEC = BUILDER.build();

    public static ServerConfig serverConfig;

    @SubscribeEvent
    static void onLoad(final ModConfigEvent event) {
        String address = ADDRESS.get();
        int port = PORT.get();
        Path filesDirectory = Paths.get(FILES_DIRECTORY.get());
        boolean useHttps = USE_HTTPS.get();
        boolean autoSsl = AUTO_SSL.get();

        String keyStorePath = KEYSTORE_PATH.get();
        String keyStorePassword = KEYSTORE_PASSWORD.get();
        String keyStoreType = KEYSTORE_TYPE.get();
        String trustStorePath = TRUSTSTORE_PATH.get();
        String trustStorePassword = TRUSTSTORE_PASSWORD.get();
        String trustStoreType = TRUSTSTORE_TYPE.get();

        serverConfig = new ServerConfig(address, port, filesDirectory, useHttps, autoSsl, keyStorePath,
                keyStorePassword, keyStoreType, trustStorePath, trustStorePassword, trustStoreType);
    }
}
