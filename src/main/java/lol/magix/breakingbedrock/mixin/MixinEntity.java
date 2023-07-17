package lol.magix.breakingbedrock.mixin;

import lol.magix.breakingbedrock.game.nametag.NameTagManager;
import lol.magix.breakingbedrock.objects.game.NameableEntity;
import lol.magix.breakingbedrock.network.BedrockNetworkClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.MovementType;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Entity.class)
public abstract class MixinEntity implements NameableEntity {
    @Shadow protected abstract void setFlag(int index, boolean value);

    @Shadow public abstract void setCustomNameVisible(boolean visible);

    @Shadow public abstract void setCustomName(@Nullable Text name);

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

    @Inject(method = "move", at = @At(value = "TAIL"))
    public void move(MovementType movementType, Vec3d movement, CallbackInfo ci) {
        if (!BedrockNetworkClient.connected()) {
            return;
        }

        // Update the entity's name tag.
        NameTagManager.updateEntity((Entity) (Object) this);
    }

    @Inject(method = "remove", at = @At(value = "TAIL"))
    public void remove(CallbackInfo ci) {
        if (!BedrockNetworkClient.connected()) {
            return;
        }

        // Remove the entity's name tag.
        NameTagManager.removeEntity((Entity) (Object) this);
    }

    @Unique @Override
    public void setName(String name) {
        if (!BedrockNetworkClient.connected()) {
            return;
        }

        this.setCustomNameVisible(false);
        this.setCustomName(null);

        // Set the name of the entity.
        NameTagManager.setName((Entity) (Object) this, name);
    }
}
