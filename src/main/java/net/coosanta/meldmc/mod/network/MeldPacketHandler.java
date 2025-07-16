package net.coosanta.meldmc.mod.network;

import io.netty.buffer.Unpooled;
import net.coosanta.meldmc.mod.MeldMC;
import net.coosanta.meldmc.mod.modlist.ClientMod;
import net.coosanta.meldmc.mod.network.packets.ClientboundModlistResponsePacket;
import net.coosanta.meldmc.mod.network.packets.ServerboundModlistRequestPacket;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.game.ServerboundCustomPayloadPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.function.Supplier;

public class MeldPacketHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(MeldPacketHandler.class);

    private static final SimpleChannel INSTANCE = NetworkRegistry.ChannelBuilder
            .named(ResourceLocation.fromNamespaceAndPath(MeldMC.MODID, "main"))
            .serverAcceptedVersions(s -> true)
            .clientAcceptedVersions(s -> true)
            .networkProtocolVersion(() -> "1")
            .simpleChannel();

    public static void register() {
        INSTANCE.registerMessage(0x00, ServerboundModlistRequestPacket.class,
                ServerboundModlistRequestPacket::encode,
                ServerboundModlistRequestPacket::decode,
                MeldPacketHandler::handleRequest);

        INSTANCE.registerMessage(0x01, ClientboundModlistResponsePacket.class,
                ClientboundModlistResponsePacket::encode,
                ClientboundModlistResponsePacket::decode,
                MeldPacketHandler::handleResponse);
    }

    public static void handleCustomPayload(FriendlyByteBuf buffer) {
        LOGGER.debug("Received custom payload");
        int packetId = buffer.readVarInt();

        if (packetId == 0x00) {
            LOGGER.debug("Handling modlist request");
            MeldPacketHandler.handleRequest(new ServerboundModlistRequestPacket());
        }
    }

    private static void handleRequest(ServerboundModlistRequestPacket msg, Supplier<NetworkEvent.Context> ctx) {
        LOGGER.debug("Handle modlist request");
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

//    @SubscribeEvent
//    public static void onCustomPayload(NetworkEvent.ServerCustomPayloadEvent event) {
//
//
//        if (event.getPayload().readResourceLocation().equals(ResourceLocation.fromNamespaceAndPath("meldmc", "main"))) {
//            LOGGER.info("Received custom payload during STATUS phase");
//
//            // Read the packet data
//            FriendlyByteBuf buffer = event.getPayload();
//            int packetId = buffer.readUnsignedByte();
//
//            if (packetId == 0x00) {
//                LOGGER.info("Handling modlist request in STATUS phase");
////                handleModlistRequestInStatus(event);
//            }
//        }
//    }

//    private static void handleModlistRequestInStatus(NetworkEvent.ServerCustomPayloadEvent event) {
//        // Create response packet
//        var modlistMap = MeldMC.getModlistMap();
//        if (modlistMap != null) {
//            // Send response back
//            FriendlyByteBuf responseBuf = new FriendlyByteBuf(Unpooled.buffer());
//            responseBuf.writeByte(0x01); // Response packet ID
//
//            // Write the map data (same format as your existing packet)
//            responseBuf.writeMap(
//                    modlistMap,
//                    FriendlyByteBuf::writeUtf,
//                    (buf, value) -> buf.writeJsonWithCodec(ClientMod.CODEC, value)
//            );
//
//            // Send custom payload response
//            var responsePayload = new CustomPayloadPacket(
//                    new ResourceLocation("meldmc", "main"),
//                    responseBuf
//            );
//
//            event.getSource().get().getConnection().send(responsePayload);
//        }
//    }
}