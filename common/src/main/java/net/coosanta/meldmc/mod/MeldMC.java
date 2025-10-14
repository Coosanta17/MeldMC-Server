package net.coosanta.meldmc.mod;

import net.coosanta.meldmc.mod.config.Config;
import net.coosanta.meldmc.mod.modsinfo.ClientMod;
import net.coosanta.meldmc.mod.modsinfo.ClientModScanner;
import net.coosanta.meldmc.mod.modsinfo.MeldData;
import net.coosanta.meldmc.mod.network.MeldServer;
import net.coosanta.meldmc.mod.network.ServerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class MeldMC {
    public static final String MOD_ID = "meldmc";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    private static MeldServer meldServer;

    public static void init(String mcVersion, MeldData.ModLoader modLoader, String loaderVersion) {
        LOGGER.info("MeldMC Starting...");

        Config.load();

        LOGGER.info("Starting client mod scanning process");
        Map<String, ClientMod> modlistMap = ClientModScanner.scanClientMods(Config.serverConfig.filesDirectory());
        LOGGER.info("Client mod scanning complete");

        String versionId = switch (modLoader) {
            case FABRIC -> "fabric-loader-" + loaderVersion + "-" + mcVersion;
            case FORGE -> mcVersion + "-forge-" + loaderVersion;
            case NEOFORGE -> "neoforge-" + loaderVersion;
        };

        var meldData = new MeldData(mcVersion, modLoader, loaderVersion, versionId, modlistMap);

        try {
            meldServer = ServerFactory.createServer(Config.serverConfig, meldData);
            meldServer.start();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        LOGGER.info("MeldMC Started!");
    }

    public static void stop() {
        LOGGER.info("Stopping MeldMC...");
        meldServer.stop();
        LOGGER.info("MeldMC stopped.");
    }
}