package lol.magix.breakingbedrock.mixin;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import lol.magix.breakingbedrock.network.BedrockNetworkClient;
import lol.magix.breakingbedrock.network.packets.bedrock.world.LevelEventTranslator;
import lol.magix.breakingbedrock.utils.WorldUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.SortedSet;

@Mixin(WorldRenderer.class)
public final class MixinWorldRenderer {
    @Shadow @Final private Long2ObjectMap<SortedSet<BlockBreakingInfo>> blockBreakingProgressions;
    @Shadow @Final private MinecraftClient client;

    @Inject(method = "setBlockBreakingInfo", at = @At("HEAD"), cancellable = true)
    public void setBlockBreakingInfo(int entityId, BlockPos position, int stage, CallbackInfo callback) {
        var player = this.client.player; if (player == null) return;
        if (BedrockNetworkClient.getInstance().isConnected() && entityId == player.getId())
            callback.cancel(); // This lets the server handle the block breaking animation.
    }

    @Inject(method = "render", at = @At("HEAD"))
    public void render(
            MatrixStack matrices, float tickDelta, long limitTime,
            boolean renderBlockOutline, Camera camera, GameRenderer gameRenderer,
            LightmapTextureManager lightmapTextureManager, Matrix4f projectionMatrix, CallbackInfo ci
    ) {
        if (!BedrockNetworkClient.getInstance().isConnected()) {
            return;
        }

        // Manually set the block breaking progressions based on the server
        var iterator = LevelEventTranslator.BLOCK_BREAKING_INFOS.entrySet().iterator();
        while (iterator.hasNext()) {
            var entry = iterator.next();

            if ((System.currentTimeMillis() - entry.getValue().lastUpdate) >= 50) {
                entry.getValue().currentDuration += (entry.getValue().length / (float) 65535);
                entry.getValue().lastUpdate = System.currentTimeMillis();
            }

            var z = (int) (entry.getValue().currentDuration * 10F) - 1;
            z = MathHelper.clamp(z, 0, 10);
            var key = WorldUtils.asLong(entry.getKey());
            if (LevelEventTranslator.TO_REMOVE.remove(key) || z == 10) {
                iterator.remove();
                this.blockBreakingProgressions.remove(key);
                continue;
            }

            entry.getValue().blockBreakingInfo.setStage(z);
        }
    }
}
