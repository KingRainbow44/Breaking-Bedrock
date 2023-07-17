package lol.magix.breakingbedrock.mixin;

import lol.magix.breakingbedrock.network.BedrockNetworkClient;
import lol.magix.breakingbedrock.objects.game.ScoreNameEntity;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRendererFactory.Context;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Objects;

@Mixin(PlayerEntityRenderer.class)
public abstract class MixinPlayerEntityRenderer extends
        LivingEntityRenderer<AbstractClientPlayerEntity, PlayerEntityModel<AbstractClientPlayerEntity>> {
    public MixinPlayerEntityRenderer(Context ctx, PlayerEntityModel<AbstractClientPlayerEntity> model, float shadowRadius) {
        super(ctx, model, shadowRadius);
    }

    @Inject(method =
            "renderLabelIfPresent(Lnet/minecraft/client/network/AbstractClientPlayerEntity;Lnet/minecraft/text/Text;Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V",
    at = @At("HEAD"), cancellable = true)
    public void renderLabelIfPresent(
            AbstractClientPlayerEntity playerEntity,
            Text text, MatrixStack matrixStack,
            VertexConsumerProvider vertexConsumerProvider, int i, CallbackInfo ci) {
        // Check if the player is connected to a Bedrock server.
        if (!BedrockNetworkClient.connected()) return;
        if (!(playerEntity instanceof ScoreNameEntity scoreName)) return;
        if (!scoreName.hasScoreName()) return;

        ci.cancel(); // Cancel original method.

        matrixStack.push();
        if (this.dispatcher.getSquaredDistanceToCamera(playerEntity) < 100.0) {
            super.renderLabelIfPresent(playerEntity, scoreName.getScoreName(), matrixStack, vertexConsumerProvider, i);
            Objects.requireNonNull(this.getTextRenderer());
            matrixStack.translate(0.0f, 9.0f * 1.15f * 0.025f, 0.0f);
        }
        super.renderLabelIfPresent(playerEntity, text, matrixStack, vertexConsumerProvider, i);
        matrixStack.pop();
    }
}
