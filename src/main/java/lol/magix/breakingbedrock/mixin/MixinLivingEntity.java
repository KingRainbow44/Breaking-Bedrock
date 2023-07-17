package lol.magix.breakingbedrock.mixin;

import lol.magix.breakingbedrock.network.BedrockNetworkClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class MixinLivingEntity extends Entity {
    public MixinLivingEntity(EntityType<?> type, World world) {
        super(type, world);
    }

    @Inject(method = "isImmobile", at = @At(value = "TAIL"), cancellable = true)
    public void isImmobile(CallbackInfoReturnable<Boolean> cir) {
        if (!BedrockNetworkClient.connected()) return;
        if (cir.getReturnValue()) return;

        cir.setReturnValue(this.getFlag(255));
    }
}
