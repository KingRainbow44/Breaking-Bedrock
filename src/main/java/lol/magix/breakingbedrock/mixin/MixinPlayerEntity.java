package lol.magix.breakingbedrock.mixin;

import lol.magix.breakingbedrock.network.BedrockNetworkClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerEntity.class)
public final class MixinPlayerEntity {
    @Inject(method = "getAttackCooldownProgressPerTick", at = @At("HEAD"), cancellable = true)
    public void getAttackCooldownProgressPerTick(CallbackInfoReturnable<Float> cir) {
        if (!BedrockNetworkClient.connected()) return;
        cir.setReturnValue((float) (1.0 / 20000));
    }

    @Inject(method = "attack", at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/entity/player/PlayerEntity;setSprinting(Z)V"
    ), cancellable = true)
    public void removeSprint(Entity target, CallbackInfo ci) {
        if (!BedrockNetworkClient.connected()) return;
        ci.cancel();
    }

    @Inject(method = "attack", at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/entity/player/PlayerEntity;setVelocity(Lnet/minecraft/util/math/Vec3d;)V"
    ), cancellable = true)
    public void removeVelocity(Entity target, CallbackInfo ci) {
        if (!BedrockNetworkClient.connected()) return;
        ci.cancel();
    }
}
