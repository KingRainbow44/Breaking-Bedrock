package lol.magix.breakingbedrock.mixin.interfaces;

import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(PlayerEntity.class)
public interface IMixinPlayerEntity {
    @Accessor("PLAYER_MODEL_PARTS")
    static TrackedData<Byte> PLAYER_MODEL_PARTS() {
        return null;
    }
}
