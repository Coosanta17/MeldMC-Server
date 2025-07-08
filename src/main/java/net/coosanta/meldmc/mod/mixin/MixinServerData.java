package net.coosanta.meldmc.mod.mixin;

import com.google.gson.JsonObject;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import net.minecraft.network.protocol.status.ServerStatus;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerStatus.class)
public class MixinServerData {
    @Shadow
    @Final
    @Mutable
    public static Codec<ServerStatus> CODEC;

    @Inject(method = "<clinit>", at = @At("RETURN"))
    private static void injectMeldSupport(CallbackInfo ci) {
        final Codec<ServerStatus> originalCodec = CODEC;

        // Create a custom codec to add the extra meldSupport field during encoding.
        CODEC = new Codec<>() {
            @Override
            public <T> com.mojang.serialization.DataResult<T> encode(ServerStatus input, com.mojang.serialization.DynamicOps<T> ops, T prefix) {
                var result = originalCodec.encode(input, ops, prefix);

                if (ops == JsonOps.INSTANCE && result.result().isPresent()) {
                    Object jsonElement = result.result().get();
                    if (jsonElement instanceof JsonObject jsonObject) {
                        jsonObject.addProperty("meldSupport", true);
                    }
                }

                return result;
            }

            @Override
            public <T> com.mojang.serialization.DataResult<com.mojang.datafixers.util.Pair<ServerStatus, T>> decode(com.mojang.serialization.DynamicOps<T> ops, T input) {
                // Use the original decoder - custom field not needed on decoding (server side only)
                return originalCodec.decode(ops, input);
            }
        };
    }
}