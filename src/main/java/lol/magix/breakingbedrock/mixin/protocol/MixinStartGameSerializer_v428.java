package lol.magix.breakingbedrock.mixin.protocol;

import io.netty.buffer.ByteBuf;
import net.minecraft.util.math.MathHelper;
import org.cloudburstmc.protocol.bedrock.codec.v419.serializer.StartGameSerializer_v419;
import org.cloudburstmc.protocol.bedrock.codec.v428.serializer.StartGameSerializer_v428;
import org.cloudburstmc.protocol.bedrock.packet.StartGamePacket;
import org.cloudburstmc.protocol.common.util.VarInts;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(StartGameSerializer_v428.class)
public final class MixinStartGameSerializer_v428 extends StartGameSerializer_v419 {
    @Inject(method = "readSyncedPlayerMovementSettings", at = @At("HEAD"), cancellable = true, remap = false)
    public void readSyncedPlayerMovementSettings(ByteBuf buffer, StartGamePacket packet, CallbackInfo ci) {
        ci.cancel(); // Purely handle the packet here.

        var movementMode = MathHelper.clamp(VarInts.readInt(buffer), 0, 2);
        packet.setAuthoritativeMovementMode(MOVEMENT_MODES[movementMode]);
        packet.setRewindHistorySize(VarInts.readInt(buffer));
        packet.setServerAuthoritativeBlockBreaking(buffer.readBoolean());
    }
}
