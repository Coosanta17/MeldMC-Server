package net.coosanta.meldmc.mod.network.packets;

import com.mojang.serialization.Codec;
import net.coosanta.meldmc.mod.modlist.ClientMod;
import net.minecraft.network.FriendlyByteBuf;

import java.util.HashMap;

public record ClientboundModlistResponsePacket(HashMap<String, ClientMod> modlistData) {
    public static final Codec<HashMap<String, ClientMod>> CODEC = Codec.unboundedMap(
            Codec.STRING, ClientMod.CODEC).xmap(HashMap::new, map -> map);

    public void encode(FriendlyByteBuf buf) {
        buf.writeMap(
                modlistData,
                FriendlyByteBuf::writeUtf,
                (buffer, value) -> buffer.writeJsonWithCodec(ClientMod.CODEC, value)
        );
    }

    public static ClientboundModlistResponsePacket decode(FriendlyByteBuf buf) {
        return null; // Never used
    }
}