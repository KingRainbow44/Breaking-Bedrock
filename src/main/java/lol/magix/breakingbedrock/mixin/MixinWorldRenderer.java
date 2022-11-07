package lol.magix.breakingbedrock.mixin;

import lol.magix.breakingbedrock.network.BedrockNetworkClient;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.*;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WorldRenderer.class)
public final class MixinWorldRenderer {
    @Shadow @Final private MinecraftClient client;

    @Inject(method = "setBlockBreakingInfo", at = @At("HEAD"), cancellable = true)
    public void cancelBlockBreakingInfo(int entityId, BlockPos position, int stage, CallbackInfo callback) {
        var player = this.client.player; if (player == null) return;
        if (BedrockNetworkClient.getInstance().isConnected() && entityId == player.getId())
            callback.cancel(); // This lets the server handle the block breaking animation.
    }
}
