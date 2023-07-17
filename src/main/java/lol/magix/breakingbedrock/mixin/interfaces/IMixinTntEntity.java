package lol.magix.breakingbedrock.mixin.interfaces;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.TntEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(TntEntity.class)
public interface IMixinTntEntity {
    @Accessor("causingEntity")
    void setCausingEntity(LivingEntity causingEntity);
}
