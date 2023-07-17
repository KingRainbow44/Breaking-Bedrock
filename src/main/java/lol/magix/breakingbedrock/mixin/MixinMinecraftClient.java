package lol.magix.breakingbedrock.mixin;

import lol.magix.breakingbedrock.BreakingBedrock;
import lol.magix.breakingbedrock.game.nametag.NameTagManager;
import net.minecraft.client.MinecraftClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftClient.class)
public final class MixinMinecraftClient {
    @Inject(method = "<init>", at = @At("RETURN"))
    public void init(CallbackInfo ci) {
        // Initialize the mod.
        BreakingBedrock.initialize();
    }
}
