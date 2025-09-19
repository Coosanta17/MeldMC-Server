package net.coosanta.meldmc.mod.network;

import com.sun.net.httpserver.HttpServer;
import net.coosanta.meldmc.mod.modsinfo.MeldData;

import static net.coosanta.meldmc.mod.MeldMC.LOGGER;

public abstract class AbstractMeldServer implements MeldServer {
    protected final HttpServer server;
    protected final ServerConfig config;
    protected final MeldData meldData;

    protected AbstractMeldServer(HttpServer server, ServerConfig config, MeldData meldData) {
        this.server = server;
        this.config = config;
        this.meldData = meldData;
    }

    protected void setupEndpoints() {
        server.createContext("/info", new InfoHandler(meldData));
        server.createContext("/files", new FilesHandler(config, meldData));
    }

    protected abstract String serverType();

    @Override
    public void start() {
        LOGGER.info("Starting Meld {} server on port {}", serverType(), config.port());
        server.start();
    }

    @Override
    public void stop() {
        LOGGER.info("Stopping Meld {} server.", serverType());
        server.stop(0);
    }
}
