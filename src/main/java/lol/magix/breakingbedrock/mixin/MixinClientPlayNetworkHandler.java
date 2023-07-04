package lol.magix.breakingbedrock.mixin;

import lol.magix.breakingbedrock.utils.EncodingUtils;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.packet.s2c.play.SynchronizeTagsS2CPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayNetworkHandler.class)
public final class MixinClientPlayNetworkHandler {
    @Inject(method = "onSynchronizeTags", at = @At("HEAD"))
    public void onSynchronizeTags(SynchronizeTagsS2CPacket packet, CallbackInfo ci) {
        var buffer = PacketByteBufs.create();
        buffer.writeMap(packet.getGroups(), (bufx, registryKey) -> bufx.writeIdentifier(registryKey.getValue()),
                (bufx, serializedGroup) -> serializedGroup.writeBuf(bufx));

        System.out.println(">>> " + EncodingUtils.base64Encode(buffer.array()));
    }
}
