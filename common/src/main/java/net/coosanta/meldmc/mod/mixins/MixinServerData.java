package net.coosanta.meldmc.mod.mixins;

import com.google.gson.JsonObject;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import net.coosanta.meldmc.mod.config.Config;
import net.minecraft.network.protocol.status.ServerStatus;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static net.coosanta.meldmc.mod.MeldMC.LOGGER;

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
        // TODO: Will this work on all supported Minecraft versions? Needs testing!!
        CODEC = new Codec<>() {
            @Override
            public <T> DataResult<T> encode(ServerStatus input, DynamicOps<T> ops, T prefix) {
                var result = originalCodec.encode(input, ops, prefix);

                if (ops == JsonOps.INSTANCE && result.result().isPresent()) {
                    Object jsonElement = result.result().get();
                    if (jsonElement instanceof JsonObject jsonObject) {
                        jsonObject.addProperty("meldSupport", true);
                        jsonObject.addProperty("meldAddress", Config.serverConfig.getReplacedAddress());
                        jsonObject.addProperty("meldIsHttps", Config.serverConfig.useHttps());
                        jsonObject.addProperty("meldSelfSigned", Config.serverConfig.autoSsl() || Config.serverConfig.selfSigned());
                        LOGGER.debug("Added custom Meld fields to ping packet.");
                    }
                }

                return result;
            }

            @Override
            public <T> DataResult<Pair<ServerStatus, T>> decode(DynamicOps<T> ops, T input) {
                // Use the original decoder - custom field not needed on decoding (server side only)
                return originalCodec.decode(ops, input);
            }
        };
    }
}