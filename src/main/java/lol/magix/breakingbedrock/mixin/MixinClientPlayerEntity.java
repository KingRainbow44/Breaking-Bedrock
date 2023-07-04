package lol.magix.breakingbedrock.mixin;

import lol.magix.breakingbedrock.events.defaults.PlayerTickEvent;
import lol.magix.breakingbedrock.network.BedrockNetworkClient;
import net.minecraft.client.input.Input;
import net.minecraft.client.network.ClientPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayerEntity.class)
public final class MixinClientPlayerEntity {
    @Shadow public Input input;
    @Shadow private boolean lastOnGround;
    private long ticks;

    @Inject(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/AbstractClientPlayerEntity;tick()V"))
    public void tick(CallbackInfo callbackInfo) {
        var client = BedrockNetworkClient.getInstance();
        if (!client.isConnected()) {
            return;
        }

        client.getEventManager().call(
                new PlayerTickEvent(this.ticks++));
    }

    @Inject(method = "tickMovement", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/input/Input;tick(ZF)V"))
    public void tickMovement(CallbackInfo ci) {
        var client = BedrockNetworkClient.getInstance();
        if (!client.isConnected()) {
            return;
        }

        client.getData().setJumping(
                this.input.jumping && this.lastOnGround);
    }
}
