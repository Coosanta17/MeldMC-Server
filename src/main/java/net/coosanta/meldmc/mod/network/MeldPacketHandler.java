package net.coosanta.meldmc.mod.network;

import net.coosanta.meldmc.mod.MeldMC;
import net.coosanta.meldmc.mod.modlist.ClientMod;
import net.coosanta.meldmc.mod.network.packets.ClientboundModlistResponsePacket;
import net.coosanta.meldmc.mod.network.packets.ServerboundModlistRequestPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

import java.util.HashMap;
import java.util.function.Supplier;

public class MeldPacketHandler {
    private static final SimpleChannel INSTANCE = NetworkRegistry.ChannelBuilder.named(
                    ResourceLocation.fromNamespaceAndPath(MeldMC.MODID, "main"))
            .serverAcceptedVersions(s -> true)
            .clientAcceptedVersions(s -> true)
            .networkProtocolVersion(() -> "1")
            .simpleChannel();

    public static void register() {
        int id = 0;
        INSTANCE.registerMessage(id++, ServerboundModlistRequestPacket.class,
                ServerboundModlistRequestPacket::encode,
                ServerboundModlistRequestPacket::decode,
                MeldPacketHandler::handleRequest);

        INSTANCE.registerMessage(id++, ClientboundModlistResponsePacket.class,
                ClientboundModlistResponsePacket::encode,
                ClientboundModlistResponsePacket::decode,
                MeldPacketHandler::handleResponse);
    }

    private static void handleRequest(ServerboundModlistRequestPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            var response = new ClientboundModlistResponsePacket((HashMap<String, ClientMod>) MeldMC.getModlistMap());
            INSTANCE.sendTo(response, ctx.get().getNetworkManager(), NetworkDirection.PLAY_TO_CLIENT);
        });
        ctx.get().setPacketHandled(true);
    }

    private static void handleResponse(ClientboundModlistResponsePacket msg, Supplier<NetworkEvent.Context> ctx) {
        // There is no need to handle the client bound packets. Registering requires a handler.
        ctx.get().setPacketHandled(true);
    }
}