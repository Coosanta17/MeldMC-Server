package net.coosanta.meldmc.mod.network.packets;

import net.minecraft.network.FriendlyByteBuf;

public record ServerboundModlistRequestPacket() {
    public void encode(FriendlyByteBuf buf) {
        // Never used
    }

    public static ServerboundModlistRequestPacket decode(FriendlyByteBuf buffer) {
        return new ServerboundModlistRequestPacket();
    }
}
