package lol.magix.breakingbedrock.mixin;

import lol.magix.breakingbedrock.network.BedrockNetworkClient;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Entity.class)
public abstract class MixinEntity {
    @Shadow protected abstract void setFlag(int index, boolean value);

    @Inject(method = "pushAwayFrom", at = @At(value = "HEAD"), cancellable = true)
    public void pushAwayFrom(Entity entity, CallbackInfo ci) {
        if (!BedrockNetworkClient.connected()) {
            return;
        }

        ci.cancel();
    }

    @Inject(method = "<init>", at = @At(value = "TAIL"))
    public void initDataTracker(CallbackInfo ci) {
        if (!BedrockNetworkClient.connected()) {
            return;
        }

        // Set NO_AI to false.
        this.setFlag(255, false);
    }
}
