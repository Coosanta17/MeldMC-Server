package net.coosanta.meldmc.mod.network;

public interface MeldServer {
    void start();

    void stop();

    default void restart() {
        stop();
        start();
    }
}
