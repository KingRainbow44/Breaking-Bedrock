package lol.magix.breakingbedrock.mixin.interfaces;

import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(PlayerInteractEntityC2SPacket.class)
public interface IMixinPlayerInteractEntityC2SPacket {
    @Accessor("entityId")
    int getEntityId();
}
